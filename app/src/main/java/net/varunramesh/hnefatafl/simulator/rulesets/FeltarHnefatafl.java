package net.varunramesh.hnefatafl.simulator.rulesets;

import com.annimon.stream.Stream;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.Direction;
import net.varunramesh.hnefatafl.simulator.EventHandler;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Winner;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Varun on 8/17/2015.
 */
public class FeltarHnefatafl implements Ruleset, Serializable {
    private static final String TAG = "FeltarHnefatafl";
    public static final int BOARD_SIZE = 11;

    @Override
    public Board getStartingConfiguration() {
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

        return new Board(HashTreePMap.from(pieces), Player.ATTACKER, Winner.UNDETERMINED, BOARD_SIZE);
    }

    @Override
    public Board step(List<Board> history, Action action, EventHandler eventHandler) {
        Board currentBoard = history.get(history.size() - 1);

        // Basic assertions about the current game state.
        Assert.assertEquals("A winner has not yet been set", currentBoard.getWinner(), Winner.UNDETERMINED);
        Assert.assertEquals("The provided action is for the currently active player.", action.getPlayer(), currentBoard.getCurrentPlayer());
        Assert.assertNotNull("Action is non-null.", action);

        // TODO: Probably verify move and complain if it's illegal.
        // for now, assume that the move was given by us, and is thus valid.

        // Move the piece.
        PMap<Position, Piece> pieces = currentBoard.getPieces();
        Piece piece = pieces.get(action.getFrom());
        PMap<Position, Piece> newPieces = pieces.minus(action.getFrom());
        newPieces = newPieces.plus(action.getTo(), piece);
        if(eventHandler != null) eventHandler.movePiece(action.getFrom(), action.getTo());

        // Look to see if any adjacent opposing piece has been sandwiched.
        for(Direction dir : Direction.values()) {
            final Position pos = action.getTo().getNeighbor(dir);
            if(newPieces.containsKey(pos)) {
                Piece adjacentPiece = newPieces.get(pos);
                if(adjacentPiece.hostileTo(piece) && isCaptured(currentBoard, newPieces, pos, action.getTo())) {
                    newPieces = newPieces.minus(pos);
                    if(eventHandler != null) eventHandler.removePiece(pos);
                }
            }
        }

        // Check to see if someone has won.
        Winner winner = Winner.UNDETERMINED;
        Board tempBoard = new Board(newPieces, currentBoard.getCurrentPlayer().other(), winner, BOARD_SIZE);
        if (kingInRefugeeSquare(tempBoard)) {
            // If the King is in a refugee square, the defenders win.
            winner = Winner.DEFENDER;
        } else if (tempBoard.getPositionsOfPiece(Piece.KING).size() == 0){
            // If the King has been captured, then the attackers win.
            winner = Winner.ATTACKER;
        } else {
            // If the next board would result in that player having no actions, then
            // the current player has won.
            if(getActions(tempBoard).size() == 0) {
                winner = Winner.fromPlayer(currentBoard.getCurrentPlayer());
            }
        }

        if(winner != Winner.UNDETERMINED && eventHandler != null)
            eventHandler.setWinner(winner);

        return new Board(newPieces, currentBoard.getCurrentPlayer().other(), winner, BOARD_SIZE);
    }

    @Override
    public Set<Action> getActions(List<Board> history) {
       return getActions(history.get(history.size() - 1));
    }

    private Set<Action> getActions(Board currentBoard) {
        // If the game is over, return the empty set.
        Set<Action> actions = new HashSet<>();
        if(currentBoard.isOver()) return actions;

        // Add all of the actions for pieces that the current player owns.
        Player currentPlayer = currentBoard.getCurrentPlayer();
        for(Map.Entry<Position, Piece> piece : currentBoard.getPieces().entrySet()) {
            if (currentPlayer.ownsPiece(piece.getValue()))
                actions.addAll(getActionsForPiece(currentBoard, piece.getKey()));
        }
        return actions;
    }

    /** Helper function: Get all of the actions that the piece at the given position can take */
    private Set<Action> getActionsForPiece(Board board, Position position) {
        PMap<Position, Piece> pieces = board.getPieces();
        Player currentPlayer = board.getCurrentPlayer();

        Assert.assertTrue(pieces.containsKey(position));

        Piece piece = pieces.get(position);
        Assert.assertTrue(currentPlayer.ownsPiece(piece));

        Set<Action> actions = new HashSet<>();
        for(Direction dir : Direction.values()) {
            for(Position pos = position.getNeighbor(dir); board.contains(pos); pos = pos.getNeighbor(dir)) {
                if(pieces.containsKey(pos)) break;
                else {
                    if(piece != Piece.KING && isKingOnlySquare(board, pos))
                        continue;
                    actions.add(new Action(currentPlayer, position, pos));
                }
            }
        }
        return actions;
    }

    /** Returns true if the square is a King-Only Square */
    public boolean isKingOnlySquare(Board board, Position pos) {
        return board.getCenterSquare().equals(pos) || board.getCornerSquares().contains(pos);
    }

    public boolean kingInRefugeeSquare(Board board) {
        PMap<Position, Piece> pieces = board.getPieces();
        return Stream.of(board.getCornerSquares()).anyMatch((Position pos) -> {
            return pieces.containsKey(pos) && pieces.get(pos) == Piece.KING;
        });
    }

    public static boolean isCaptured(Board board, PMap<Position, Piece> pieces, Position defendingPos, Position attackingPos) {
        final Piece piece = pieces.get(defendingPos);
        switch (piece) {
            case KING:
                return Stream.of(Direction.values()).allMatch((Direction dir) -> {
                    // The King is only sandwiched when all four s
                    Position adjacent = defendingPos.getNeighbor(dir);
                    return pieces.containsKey(adjacent) && pieces.get(adjacent).hostileTo(piece);
                });
            case ATTACKER: {
                Position oppositePos = defendingPos.getNeighbor(defendingPos.directionTo(attackingPos).opposite());
                return board.getCenterSquare().equals(oppositePos)
                        || board.getCornerSquares().contains(oppositePos)
                        || (pieces.containsKey(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
            case DEFENDER: {
                Position oppositePos = defendingPos.getNeighbor(defendingPos.directionTo(attackingPos).opposite());
                return board.getCornerSquares().contains(oppositePos)
                        || (pieces.containsKey(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
        }
        return true;
    }
}
