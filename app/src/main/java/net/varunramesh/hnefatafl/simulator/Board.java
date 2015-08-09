package net.varunramesh.hnefatafl.simulator;

import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Stream;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
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

    /** Default contructor that creates a game board in the starting configuration. */
    public Board() {
        pieces = START_CONFIGURATION; // Set pieces to start configuration.
        currentPlayer = Player.ATTACKER; // Attacker goes first.
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(currentPlayer);
        out.writeInt(pieces.size());
        for(Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.currentPlayer = (Player)stream.readObject();

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
    public Board(PMap<Position, Piece> pieces, Player currentPlayer) {
        this.pieces = pieces;
        this.currentPlayer = currentPlayer;
    }

    /** Get the number of pieces currently on the board */
    public int getNumberOfPieces() {
        return pieces.size();
    }

    /** Step the game forward by one action. Rules are implemented based off of http://aagenielsen.dk/fetlar_rules_en.php */
    public Board step(Action action, EventHandler eventHandler) {
        assert action.getPlayer() == currentPlayer;
        assert action != null;

        // TODO: Probably verify move and complain if it's illegal.
        // for now, assume that the move was given by us, and is thus valid.

        // Move the piece.
        Piece piece = pieces.get(action.getFrom());
        PMap<Position, Piece> newPieces = pieces.minus(action.getFrom());
        newPieces = newPieces.plus(action.getTo(), piece);
        if(eventHandler != null) eventHandler.MovePiece(action.getFrom(), action.getTo());

        // Look to see if any adjacent opposing piece has been sandwiched.
        for(Direction dir : Direction.values()) {
            final Position pos = action.getTo().getNeighbor(dir);
            if(newPieces.containsKey(pos)) {
                Piece adjacentPiece = newPieces.get(pos);
                if(adjacentPiece.hostileTo(piece) && Board.isCaptured(newPieces, pos, action.getTo())) {
                    newPieces = newPieces.minus(pos);
                    if(eventHandler != null) eventHandler.RemovePiece(pos);
                }
            }
        }

        return new Board(newPieces, Utils.otherPlayer(currentPlayer));
    }
    public Board step(Action action) { return step(action, null); }

    public static boolean isCaptured(PMap<Position, Piece> pieces, Position defendingPos, Position attackingPos) {
        final Piece piece = pieces.get(defendingPos);
        switch (piece.getType()) {
            case KING:
                return Stream.of(Direction.values()).allMatch((Direction dir) -> {
                    // The King is only sandwiched when all four s
                    Position adjacent = defendingPos.getNeighbor(dir);
                    return pieces.containsKey(adjacent) && pieces.get(adjacent).hostileTo(piece);
                });
            case ATTACKER: {
                /*return Stream.of(Direction.values()).anyMatch((Direction dir) -> {
                    Position firstPos = defendingPos.getNeighbor(dir);
                    Position secondPos = defendingPos.getNeighbor(Utils.oppositeDirection(dir));

                    // Attackers can be trapped between the throne, a refugee square, and an hostile piece.
                    boolean firstPosHostile = CENTER_SQUARE.equals(firstPos)
                            || getRefugeeSquares().contains(firstPos)
                            || (pieces.containsKey(firstPos) && pieces.get(firstPos).hostileTo(piece));
                    boolean secondPosHostile = CENTER_SQUARE.equals(secondPos)
                            || getRefugeeSquares().contains(secondPos)
                            || (pieces.containsKey(secondPos) && pieces.get(secondPos).hostileTo(piece));

                    return firstPosHostile && secondPosHostile;
                });*/
                Position oppositePos = defendingPos.getNeighbor(Utils.oppositeDirection(defendingPos.directionTo(attackingPos)));
                return CENTER_SQUARE.equals(oppositePos)
                        || getRefugeeSquares().contains(oppositePos)
                        || (pieces.containsKey(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
            case DEFENDER: {
                /*return Stream.of(Direction.values()).anyMatch((Direction dir) -> {
                    Position firstPos = defendingPos.getNeighbor(dir);
                    Position secondPos = defendingPos.getNeighbor(Utils.oppositeDirection(dir));

                    // Attackers can be trapped between a refugee square, and a defender.
                    boolean firstPosHostile = getRefugeeSquares().contains(firstPos)
                            || (pieces.containsKey(firstPos) && pieces.get(firstPos).hostileTo(piece));
                    boolean secondPosHostile = getRefugeeSquares().contains(secondPos)
                            || (pieces.containsKey(secondPos) && pieces.get(secondPos).hostileTo(piece));

                    return firstPosHostile && secondPosHostile;
                });*/
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
        assert pieces.containsKey(position);

        Set<Action> actions = new HashSet<>();
        Piece piece = pieces.get(position);

        // If the current player does not own the piece, return
        // the empty set.
        if(!PlayerOwnsPiece(currentPlayer, piece)) return actions;

        for(Direction dir : Direction.values()) {
            for(Position pos = position.getNeighbor(dir); contains(pos); pos = pos.getNeighbor(dir)) {
                if(pieces.containsKey(pos)) break;
                else {
                    if(piece.getType() != Piece.Type.KING && isKingOnlySquare(pos))
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
        pieces.put(new Position(5, 5), new Piece(Piece.Type.KING));

        // Defender locations
        pieces.put(new Position(7, 5), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(6, 5), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(4, 5), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(3, 5), new Piece(Piece.Type.DEFENDER));

        pieces.put(new Position(5, 7), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(5, 6), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(5, 4), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(5, 3), new Piece(Piece.Type.DEFENDER));

        pieces.put(new Position(4, 4), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(4, 6), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(6, 4), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(6, 6), new Piece(Piece.Type.DEFENDER));

        // Attacker locations
        pieces.put(new Position(0, 3), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 4), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 5), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 6), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 7), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(1, 5), new Piece(Piece.Type.ATTACKER));

        pieces.put(new Position(10, 3), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 4), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 5), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 6), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 7), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(9, 5), new Piece(Piece.Type.ATTACKER));

        pieces.put(new Position(3, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(4, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(6, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(7, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 1), new Piece(Piece.Type.ATTACKER));

        pieces.put(new Position(3, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(4, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(6, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(7, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 9), new Piece(Piece.Type.ATTACKER));

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

    public static boolean PlayerOwnsPiece(Player player, Piece.Type piece) {
        return (player == Player.ATTACKER && piece == Piece.Type.ATTACKER)
                || (player == Player.DEFENDER && (piece == Piece.Type.DEFENDER || piece == Piece.Type.KING));
    }

    public static boolean PlayerOwnsPiece(Player player, Piece piece) {
        return PlayerOwnsPiece(player, piece.getType());
    }
}
