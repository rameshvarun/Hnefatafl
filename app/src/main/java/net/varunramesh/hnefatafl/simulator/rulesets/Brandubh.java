package net.varunramesh.hnefatafl.simulator.rulesets;

import com.annimon.stream.Stream;

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Varun on 8/18/2015.
 */
public class Brandubh implements Ruleset, Serializable {
    private static final String TAG = "Brandubh";
    public static final int BOARD_SIZE = 7;

    @Override
    public Board getStartingConfiguration() {
        // King position
        Grid grid = new Grid(BOARD_SIZE).add(new Position(3, 3), Piece.KING)

        // Defender locations
        .add(new Position(2, 3), Piece.DEFENDER)
        .add(new Position(4, 3), Piece.DEFENDER)
        .add(new Position(3, 2), Piece.DEFENDER)
        .add(new Position(3, 4), Piece.DEFENDER)

        // Attacker locations
        .add(new Position(0, 3), Piece.ATTACKER)
        .add(new Position(1, 3), Piece.ATTACKER)
        .add(new Position(5, 3), Piece.ATTACKER)
        .add(new Position(6, 3), Piece.ATTACKER)

        .add(new Position(3, 0), Piece.ATTACKER)
        .add(new Position(3, 1), Piece.ATTACKER)
        .add(new Position(3, 5), Piece.ATTACKER)
        .add(new Position(3, 6), Piece.ATTACKER);

        return new Board(grid, Player.ATTACKER, Winner.UNDETERMINED, BOARD_SIZE);
    }

    @Override
    public String getRulesetName() {
        return "Brandubh";
    }

    @Override
    public String getRulesHTML() {
        return "file:///android_asset/rules/brandubh.html";
    }

    @Override
    public Board step(History history, Action action, EventHandler eventHandler) {
        Board currentBoard = history.getCurrentBoard();

        // Basic assertions about the current game state.
        assert currentBoard.getWinner().equals(Winner.UNDETERMINED) : "A winner has not yet been set";
        assert action.getPlayer().equals(currentBoard.getCurrentPlayer()) : "The provided action is for the currently active player.";
        assert action != null : "Action is non-null.";

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
        Board tempBoard = new Board(newPieces, currentBoard.getCurrentPlayer().other(), winner,
                currentBoard.getBoardSize());
        if (kingInRefugeeSquare(tempBoard)) {
            // If the King is in a refugee square, the defenders win.
            winner = Winner.DEFENDER;
        } else if (newPieces.getPositionsOfPiece(Piece.KING).size() == 0){
            // If the King has been captured, then the attackers win.
            winner = Winner.ATTACKER;
        } else {
            // If the next board would result in that player having no actions, then
            // the current player has won.
            if(getActionsForBoard(tempBoard).size() == 0) {
                winner = Winner.fromPlayer(currentBoard.getCurrentPlayer());
            }
        }

        if(winner != Winner.UNDETERMINED && eventHandler != null)
            eventHandler.setWinner(winner);

        return new Board(newPieces, currentBoard.getCurrentPlayer().other(), winner, currentBoard.getBoardSize());
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
                if (board.getCenterSquare().equals(defendingPos)) {
                    /* If the King is on the throne, then it must be sandwiched on all four sides by a hostile piece. */
                    return defendingPos.getNeighborStream()
                            .allMatch((Position adjacent) -> pieces.pieceAt(adjacent)
                                    && pieces.get(adjacent).hostileTo(piece));
                } else if (defendingPos.isNeighbor(board.getCenterSquare())) {
                    /* If the King is adjacent to the throne, then it must be surrounded on three sides. */
                    return defendingPos.getNeighborStream()
                            .filter((Position adjacent) -> pieces.pieceAt(adjacent)
                                    && pieces.get(adjacent).hostileTo(piece))
                            .count() == 3;
                }

                /* If the King is anywhere else on the board, treat him like a regular defender, and
                 * fall through to the next case. */
            case DEFENDER: {
                Position oppositePos = defendingPos.getNeighbor(defendingPos.directionTo(attackingPos).opposite());
                return board.getCornerSquares().contains(oppositePos)
                        || (oppositePos.equals(board.getCenterSquare()) && !pieces.pieceAt(board.getCenterSquare()))
                        || (pieces.inBounds(oppositePos) && pieces.pieceAt(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }
            case ATTACKER: {
                Position oppositePos = defendingPos.getNeighbor(defendingPos.directionTo(attackingPos).opposite());
                return board.getCenterSquare().equals(oppositePos)
                        || board.getCornerSquares().contains(oppositePos)
                        || (oppositePos.equals(board.getCenterSquare()) && !pieces.pieceAt(board.getCenterSquare()))
                        || (pieces.inBounds(oppositePos) && pieces.pieceAt(oppositePos) && pieces.get(oppositePos).hostileTo(piece));
            }

        }
        return true;
    }

    @Override
    public List<Action> getActions(History history) {
        return getActionsForBoard(history.getCurrentBoard());
    }

    private List<Action> getActionsForBoard(Board currentBoard) {
        // If the game is over, return the empty set.
        List<Action> actions = new ArrayList<>();
        if(currentBoard.isOver()) return actions;

        // Add all of the actions for pieces that the current player owns.
        Player currentPlayer = currentBoard.getCurrentPlayer();
        for(Map.Entry<Position, Piece> piece : currentBoard.getPieces().getEntries()) {
            if (currentPlayer.ownsPiece(piece.getValue()))
                addActionsForPiece(currentBoard, piece.getKey(), actions);
        }

        return Collections.unmodifiableList(actions);
    }

    /** Returns true if the square is a King-Only Square. Hard-coded for performance. */
    public boolean isKingOnlySquare(Position pos) {
        return (pos.getX() == 3 && pos.getY() == 3) ||
                (pos.getX() == 0 && pos.getY() == 0) ||
                (pos.getX() == 0 && pos.getY() == 6) ||
                (pos.getX() == 6 && pos.getY() == 0) ||
                (pos.getX() == 6 && pos.getY() == 6);
    }

    private static final Direction[] directions = Direction.values();

    /** Helper function: Get all of the actions that the piece at the given position can take. Add
     * it to the provided set. */
    private void addActionsForPiece(Board board, Position position, List<Action> actions) {
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

    @Override
    public int getAISearchDepth() {
        return 4;
    }
}
