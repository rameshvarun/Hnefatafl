package net.varunramesh.hnefatafl.simulator;

import com.annimon.stream.Stream;

import junit.framework.Assert;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An immutable object that represents the state of
 */
public final class Board implements Serializable {
    /* Instance Variables. Would Be final if not for the need for custom serialization. */
    private PMap<Position, Piece> pieces;
    private Player currentPlayer;
    private Player winner;
    private int boardSize;

    /** Default contructor that creates a game board in the starting configuration. */
    public Board() {
        boardSize = 11;
        pieces = START_CONFIGURATION; // Set pieces to start configuration.
        currentPlayer = Player.ATTACKER; // Attacker goes first.
        winner = null; // Start out with no winner.
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(boardSize);
        out.writeObject(currentPlayer);
        out.writeObject(winner);
        out.writeInt(pieces.size());
        for(Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.boardSize = stream.readInt();
        this.currentPlayer = (Player)stream.readObject();
        this.winner = (Player)stream.readObject();

        Map<Position, Piece> pieces = new HashMap<Position, Piece>();
        int size = stream.readInt();
        for(int i = 0; i < size; ++i) {
            Position key = (Position)stream.readObject();
            Piece value = (Piece)stream.readObject();
            pieces.put(key, value);
        }
        this.pieces = HashTreePMap.from(pieces);
    }

    /** Instantiate a board with the given values */
    public Board(PMap<Position, Piece> pieces, Player currentPlayer, Player winner, int boardSize) {
        this.pieces = pieces;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
        this.boardSize = boardSize;
    }

    /** Get the number of pieces currently on the board */
    public int getNumberOfPieces() { return pieces.size(); }
    /** Get the size of the board. Can safely assume that the board is allways a square */
    public int getBoardSize() { return boardSize; }

    public boolean kingInRefugeeSquare() {
        return Stream.of(getRefugeeSquares()).anyMatch((Position pos) -> {
            return pieces.containsKey(pos) && pieces.get(pos) == Piece.KING;
        });
    }

    /** Step the game forward by one action. Rules are implemented based off of http://aagenielsen.dk/fetlar_rules_en.php */
    public Board step(Action action, EventHandler eventHandler) {
        Assert.assertNull("A winner has not yet been set", winner);
        Assert.assertEquals("The provided action is for the currently active player.", action.getPlayer(), currentPlayer);
        Assert.assertNotNull("Action is non-null.", action);

        // TODO: Probably verify move and complain if it's illegal.
        // for now, assume that the move was given by us, and is thus valid.

        // Move the piece.
        Piece piece = pieces.get(action.getFrom());
        PMap<Position, Piece> newPieces = pieces.minus(action.getFrom());
        newPieces = newPieces.plus(action.getTo(), piece);
        if(eventHandler != null) eventHandler.movePiece(action.getFrom(), action.getTo());

        // Look to see if any adjacent opposing piece has been sandwiched.
        for(Direction dir : Direction.values()) {
            final Position pos = action.getTo().getNeighbor(dir);
            if(newPieces.containsKey(pos)) {
                Piece adjacentPiece = newPieces.get(pos);
                if(adjacentPiece.hostileTo(piece) && Board.isCaptured(newPieces, pos, action.getTo())) {
                    newPieces = newPieces.minus(pos);
                    if(eventHandler != null) eventHandler.removePiece(pos);
                }
            }
        }

        // Check to see if someone has won.
        Player winner = null;
        Board tempBoard = new Board(newPieces, Utils.otherPlayer(currentPlayer), null, boardSize);
        if (tempBoard.kingInRefugeeSquare()) {
            // If the King is in a refugee square, the defenders win.
            winner = Player.DEFENDER;
        } else if (tempBoard.getPieces(Piece.KING).size() == 0){
            // If the King has been captured, then the attackers win.
            winner = Player.ATTACKER;
        } else {
            // If the next board would result in that player having no actions, then
            // the current player has won.
            if(tempBoard.getActions().size() == 0) {
                winner = currentPlayer;
            }
        }

        if(winner != null && eventHandler != null)
            eventHandler.setWinner(winner);

        return new Board(newPieces, Utils.otherPlayer(currentPlayer), winner, boardSize);
    }
    public Board step(Action action) { return step(action, null); }

    public boolean isOver() { return winner != null; }
    public Player getWinner() { return winner; }

    public static boolean isCaptured(PMap<Position, Piece> pieces, Position defendingPos, Position attackingPos) {
        final Piece piece = pieces.get(defendingPos);
        switch (piece) {
            case KING:
                return Stream.of(Direction.values()).allMatch((Direction dir) -> {
                    // The King is only sandwiched when all four s
                    Position adjacent = defendingPos.getNeighbor(dir);
                    return pieces.containsKey(adjacent) && pieces.get(adjacent).hostileTo(piece);
                });
            case ATTACKER: {
                Position oppositePos = defendingPos.getNeighbor(Utils.oppositeDirection(defendingPos.directionTo(attackingPos)));
                return CENTER_SQUARE.equals(oppositePos)
                        || getRefugeeSquares().contains(oppositePos)
                        || (pieces.containsKey(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
            case DEFENDER: {
                Position oppositePos = defendingPos.getNeighbor(Utils.oppositeDirection(defendingPos.directionTo(attackingPos)));
                return getRefugeeSquares().contains(oppositePos)
                        || (pieces.containsKey(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
        }
        return true;
    }

    /** Check if a position is inside the board */
    public boolean contains(Position position) {
        return position.getX() >= 0 && position.getY() >= 0 &&
                position.getX() < 11 && position.getY() < 11;
    }

    /** Returns true if the square is a King-Only Square */
    public boolean isKingOnlySquare(Position pos) {
        return CENTER_SQUARE.equals(pos) || getRefugeeSquares().contains(pos);
    }

    /** Get all of the actions that the piece at the given position can take */
    public Set<Action> getActions(Position position) {
        Assert.assertTrue(pieces.containsKey(position));
        Set<Action> actions = new HashSet<>();
        if(winner != null) return actions;

        Piece piece = pieces.get(position);

        // If the current player does not own the piece, return
        // the empty set.
        if(!PlayerOwnsPiece(currentPlayer, piece)) return actions;

        for(Direction dir : Direction.values()) {
            for(Position pos = position.getNeighbor(dir); contains(pos); pos = pos.getNeighbor(dir)) {
                if(pieces.containsKey(pos)) break;
                else {
                    if(piece != Piece.KING && isKingOnlySquare(pos))
                        continue;
                    actions.add(new Action(currentPlayer, position, pos));
                }
            }
        }
        return actions;
    }

    /** Get all of the actions that the given player can take. */
    public Set<Action> getActions(Player player) {
        Set<Action> actions = new HashSet<>();
        if(winner != null) return actions;

        for(Map.Entry<Position, Piece> piece : pieces.entrySet()) {
            if (PlayerOwnsPiece(player, piece.getValue()))
                actions.addAll(getActions(piece.getKey()));
        }
        return actions;
    }

    /** Get all of the actions that the curent player can take. */
    public Set<Action> getActions() {
        return getActions(currentPlayer);
    }

    public Player getCurrentPlayer() { return currentPlayer; }

    public Set<Map.Entry<Position, Piece>> getPieces() {
        return pieces.entrySet();
    }

    public Set<Map.Entry<Position, Piece>> getPieces(Piece type) {
        Set<Map.Entry<Position, Piece>> pieces = new HashSet<>();
        for (Map.Entry<Position, Piece> piece : getPieces()) {
            if (piece.getValue().equals(type))
                pieces.add(piece);
        }
        return pieces;
    }

    /* Static Methods and Variables */
    private static final String TAG = "Board";
    public static final int BOARD_SIZE = 11;
    public static PMap<Position, Piece> START_CONFIGURATION = createStartConfiguration();

    public static Position CENTER_SQUARE = new Position(5, 5);
    public static final PSet<Position> REFUGEE_SQUARES = getRefugeeSquares();

    /** Constructs the starting configuration of pieces on the board */
    private static PMap<Position, Piece> createStartConfiguration() {
        Map<Position, Piece> pieces = new HashMap<Position, Piece>();

        // King position
        pieces.put(new Position(5, 5), Piece.KING);

        // Defender locations
        pieces.put(new Position(7, 5), Piece.DEFENDER);
        pieces.put(new Position(6, 5), Piece.DEFENDER);
        pieces.put(new Position(4, 5), Piece.DEFENDER);
        pieces.put(new Position(3, 5), Piece.DEFENDER);

        pieces.put(new Position(5, 7), Piece.DEFENDER);
        pieces.put(new Position(5, 6), Piece.DEFENDER);
        pieces.put(new Position(5, 4), Piece.DEFENDER);
        pieces.put(new Position(5, 3), Piece.DEFENDER);

        pieces.put(new Position(4, 4), Piece.DEFENDER);
        pieces.put(new Position(4, 6), Piece.DEFENDER);
        pieces.put(new Position(6, 4), Piece.DEFENDER);
        pieces.put(new Position(6, 6), Piece.DEFENDER);

        // Attacker locations
        pieces.put(new Position(0, 3), Piece.ATTACKER);
        pieces.put(new Position(0, 4), Piece.ATTACKER);
        pieces.put(new Position(0, 5), Piece.ATTACKER);
        pieces.put(new Position(0, 6), Piece.ATTACKER);
        pieces.put(new Position(0, 7), Piece.ATTACKER);
        pieces.put(new Position(1, 5), Piece.ATTACKER);

        pieces.put(new Position(10, 3), Piece.ATTACKER);
        pieces.put(new Position(10, 4), Piece.ATTACKER);
        pieces.put(new Position(10, 5), Piece.ATTACKER);
        pieces.put(new Position(10, 6), Piece.ATTACKER);
        pieces.put(new Position(10, 7), Piece.ATTACKER);
        pieces.put(new Position(9, 5), Piece.ATTACKER);

        pieces.put(new Position(3, 0), Piece.ATTACKER);
        pieces.put(new Position(4, 0), Piece.ATTACKER);
        pieces.put(new Position(5, 0), Piece.ATTACKER);
        pieces.put(new Position(6, 0), Piece.ATTACKER);
        pieces.put(new Position(7, 0), Piece.ATTACKER);
        pieces.put(new Position(5, 1), Piece.ATTACKER);

        pieces.put(new Position(3, 10), Piece.ATTACKER);
        pieces.put(new Position(4, 10), Piece.ATTACKER);
        pieces.put(new Position(5, 10), Piece.ATTACKER);
        pieces.put(new Position(6, 10), Piece.ATTACKER);
        pieces.put(new Position(7, 10), Piece.ATTACKER);
        pieces.put(new Position(5, 9), Piece.ATTACKER);

        return HashTreePMap.from(pieces);
    }

    private static PSet<Position> getRefugeeSquares() {
        Set<Position> squares = new HashSet<>();
        squares.add(new Position(0, 0));
        squares.add(new Position(10, 0));
        squares.add(new Position(0, 10));
        squares.add(new Position(10, 10));
        return HashTreePSet.from(squares);
    }

    public static boolean PlayerOwnsPiece(Player player, Piece piece) {
        return (player == Player.ATTACKER && piece == Piece.ATTACKER)
                || (player == Player.DEFENDER && (piece == Piece.DEFENDER || piece == Piece.KING));
    }
}
