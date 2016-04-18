package net.varunramesh.hnefatafl.simulator.rulesets;

import android.util.LruCache;

import com.annimon.stream.Stream;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.Direction;
import net.varunramesh.hnefatafl.simulator.EventHandler;
import net.varunramesh.hnefatafl.simulator.Grid;
import net.varunramesh.hnefatafl.simulator.History;
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

        return new Board(new Grid(BOARD_SIZE).add(pieces), Player.ATTACKER, Winner.UNDETERMINED, BOARD_SIZE);
    }

    @Override
    public Board step(History history, Action action, EventHandler eventHandler) {
        Board currentBoard = history.getCurrentBoard();

        // Basic assertions about the current game state.
        assert currentBoard.getWinner().equals(Winner.UNDETERMINED) : "A winner has not yet been set";
        assert action.getPlayer().equals(currentBoard.getCurrentPlayer()) : "The provided action is for the currently active player.";
        assert action != null : "Action is non-null.";

        // TODO: Probably verify move and complain if it's illegal.
        // for now, assume that the move was given by us, and is thus valid.

        // Move the piece.
        Grid pieces = currentBoard.getPieces();
        Piece piece = pieces.get(action.getFrom());
        Grid newPieces = pieces.remove(action.getFrom());
        newPieces = newPieces.add(action.getTo(), piece);
        if(eventHandler != null) eventHandler.movePiece(action.getFrom(), action.getTo());

        // Look to see if any adjacent opposing piece has been sandwiched.
        for(Direction dir : directions) {
            final Position pos = action.getTo().getNeighbor(dir);
            if(newPieces.inBounds(pos) && newPieces.pieceAt(pos)) {
                Piece adjacentPiece = newPieces.get(pos);
                if(adjacentPiece.hostileTo(piece) && isCaptured(currentBoard, newPieces, pos, action.getTo())) {
                    newPieces = newPieces.remove(pos);
                    if(eventHandler != null) eventHandler.removePiece(pos);
                }
            }
        }

        // Check to see if someone has won.
        Winner winner = Winner.UNDETERMINED;
        Board tempBoard = new Board(newPieces, currentBoard.getCurrentPlayer().other(), winner, currentBoard.getBoardSize());
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

        return new Board(newPieces, currentBoard.getCurrentPlayer().other(), winner, currentBoard.getBoardSize());
    }

    @Override
    public Set<Action> getActions(History history) {
        return getActions(history.getCurrentBoard());
    }

    @Override
    public int getAISearchDepth() {
        return 2;
    }


    private Set<Action> getActions(Board currentBoard) {
        // If the game is over, return the empty set.
        Set<Action> actions = new HashSet<>();
        if(currentBoard.isOver()) return actions;

        // Add all of the actions for pieces that the current player owns.
        Player currentPlayer = currentBoard.getCurrentPlayer();
        for(Map.Entry<Position, Piece> piece : currentBoard.getPieces().getEntries()) {
            if (currentPlayer.ownsPiece(piece.getValue()))
                addActionsForPiece(currentBoard, piece.getKey(), actions);
        }

        return actions;
    }

    private static Direction[] directions = Direction.values();

    /** Helper function: Get all of the actions that the piece at the given position can take. Add
     * it to the provided set. */
    private void addActionsForPiece(Board board, Position position, Set<Action> actions) {
        Grid grid = board.getPieces();
        Player currentPlayer = board.getCurrentPlayer();

        assert grid.pieceAt(position) : "The board must have a piece at the requested position.";

        Piece piece = grid.get(position);
        assert currentPlayer.ownsPiece(piece) : "The current player must own the piece that we are finding moves for.";

        for(Direction dir : directions) {
            for(Position pos = position.getNeighbor(dir); grid.inBounds(pos); pos = pos.getNeighbor(dir)) {
                if(grid.pieceAt(pos)) break;
                else {
                    if(piece != Piece.KING && isKingOnlySquare(pos))
                        continue;
                    actions.add(new Action(currentPlayer, position, pos));
                }
            }
        }
    }

    /** Returns true if the square is a King-Only Square. Hard-coded for performance. */
    public boolean isKingOnlySquare(Position pos) {
        return (pos.getX() == 5 && pos.getY() == 5) ||
                (pos.getX() == 0 && pos.getY() == 0) ||
                (pos.getX() == 0 && pos.getY() == 10) ||
                (pos.getX() == 10 && pos.getY() == 0) ||
                (pos.getX() == 10 && pos.getY() == 10);
    }

    public boolean kingInRefugeeSquare(Board board) {
        Grid pieces = board.getPieces();
        return Stream.of(board.getCornerSquares()).anyMatch((Position pos) -> {
            return pieces.inBounds(pos) && pieces.pieceAt(pos) && pieces.get(pos) == Piece.KING;
        });
    }

    public static boolean isCaptured(Board board, Grid pieces, Position defendingPos, Position attackingPos) {
        final Piece piece = pieces.get(defendingPos);
        switch (piece) {
            case KING:
                return Stream.of(Direction.values()).allMatch((Direction dir) -> {
                    // The King is only sandwiched when all four s
                    Position adjacent = defendingPos.getNeighbor(dir);
                    return pieces.inBounds(adjacent) && pieces.pieceAt(adjacent)  && pieces.get(adjacent).hostileTo(piece);
                });
            case ATTACKER: {
                Position oppositePos = defendingPos.getNeighbor(defendingPos.directionTo(attackingPos).opposite());
                return board.getCenterSquare().equals(oppositePos)
                        || board.getCornerSquares().contains(oppositePos)
                        || (pieces.inBounds(oppositePos) && pieces.pieceAt(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
            case DEFENDER: {
                Position oppositePos = defendingPos.getNeighbor(defendingPos.directionTo(attackingPos).opposite());
                return board.getCornerSquares().contains(oppositePos)
                        || (pieces.inBounds(oppositePos) && pieces.pieceAt(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
        }
        return true;
    }
}
