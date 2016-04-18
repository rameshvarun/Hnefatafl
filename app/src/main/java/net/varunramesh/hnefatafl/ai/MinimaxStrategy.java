package net.varunramesh.hnefatafl.ai;

import android.support.v7.internal.widget.ActivityChooserModel;
import android.util.Log;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.History;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Winner;
import net.varunramesh.hnefatafl.simulator.rulesets.Ruleset;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements a bot that uses the Minimax strategy to chooses moves.
 */
public class MinimaxStrategy implements AIStrategy {
    private final String TAG = "MinimaxStrategy";

    /** Helper class the represents a (action, score) tuple */
    public static final class Result {
        public final Action action;
        public final float score;
        public Result(Action action, float score) {
            this.action = action;
            this.score = score;
        }
    }

    /** The default value for searchDepth */
    public static final int DEFAULT_SEARCH_DEPTH = 2;

    /** The player that the AI represents. */
    private final Player player;

    /** The number of Plys to search forward */
    private final int searchDepth;

    /**
     * Variable used for counting how many leaf nodes of the game tree we visit. It is set to zero
     * before each search, and read after the search.
     * */
    private int leaves = 0;

    private final Ruleset ruleset;

    public MinimaxStrategy(Ruleset ruleset, Player player) {
        this(ruleset, player, ruleset.getAISearchDepth());
    }

    public MinimaxStrategy(Ruleset ruleset, Player player, int searchDepth) {
        this.ruleset = ruleset;
        this.player = player;
        this.searchDepth = searchDepth;
    }


    @Override
    public Action decide(History history, Set<Action> actions) {
        Board currentBoard = history.getCurrentBoard();

        assert currentBoard.getCurrentPlayer().equals(player) : "AI player is current player.";
        assert ruleset.getActions(history).size() > 0 : "We need some options to pick from.";

        // Clear leaves count.
        leaves = 0;

        System.out.println(TAG + ": Starting a Minimax search with depth " + searchDepth + ".");
        Action action = max(history, searchDepth, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY).action;
        System.out.println(TAG + ": MinimaxStrategy searched " + leaves + " possible game states.");

        // Should never happen, but to prevent crashes.
        if (action == null) {
            System.err.print(TAG + ": MinimaxStrategy returned null action. Falling back to RandomStrategy.");
            return new RandomStrategy().decide(history, actions);
        }

        return action;
    }

    /** Evaluate how good the board for us */
    public float eval(Board board) {
        ++leaves; // Evaling a board means we have reached a leaf.

        // If the game already has a winner, then return -infinity or infinity
        if(board.isOver()) {
            if(board.getWinner() == Winner.DRAW) return 0;
            else return board.getWinner() == Winner.fromPlayer(player)
                    ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }

        float score = 0.0f;

        // Start by counting how many pieces each player has.
        for (Map.Entry<Position, Piece> piece : board.getPieces().entrySet()) {
            if(player.ownsPiece(piece.getValue()))
                score += 1.0f;
            else
                score -= 1.0f;
        }

        // Find the shortest distance between the King and any corner of the board.
        for(Position kingPos : board.getPositionsOfPiece(Piece.KING)) {
            int topLeftDist = kingPos.distanceTo(new Position(0, 0));
            int topRightDist = kingPos.distanceTo(new Position(board.getBoardSize() - 1, 0));
            int bottomLeftDist = kingPos.distanceTo(new Position(0, board.getBoardSize() - 1));
            int bottomRightDist = kingPos.distanceTo(new Position(board.getBoardSize() - 1, board.getBoardSize() - 1));

            int shortestDist = Math.min(
                    Math.min(topLeftDist, topRightDist),
                    Math.min(bottomLeftDist, bottomRightDist)
            );

            // For the attacking player, the larger the distance, the higher the score. For the
            // defending player, the shorter the distance, the higher the score.
            if (player == Player.ATTACKER)
                score += 0.5f*shortestDist;
            else
                score -= 0.5f*shortestDist;
        }

        return score;
    }

    /** Whether or not to use ALPHA_BETA_PRUNING to prune search paths */
    public static final boolean ALPHA_BETA_PRUNING = true;

    public Result max(History history, int depth, float alpha, float beta) {
        Board board = history.getCurrentBoard();
        assert board.getCurrentPlayer().equals(player) : "AI player is current player.";
        if(board.isOver() || depth == 0) return new Result(null, eval(board));

        Result max = null;
        for(Action action : ruleset.getActions(history)) {
            // Simulate a step, and then recurse.
            Result result = min(history.advance(action, ruleset.step(history, action, null)),
                    depth - 1, alpha, beta);

            if(max == null || result.score > max.score)
                max = new Result(action, result.score);

            if(ALPHA_BETA_PRUNING) {
                alpha = Math.max(alpha, result.score);
                if(beta <= alpha) break;
            }
        }
        return max;
    }

    public Result min(History history, int depth, float alpha, float beta) {
        Board board = history.getCurrentBoard();
        assert !board.getCurrentPlayer().equals(player) : "AI Player is not current player.";
        if(board.isOver() || depth == 0) return new Result(null, eval(board));

        Result min = null;
        for(Action action : ruleset.getActions(history)) {
            // Simulate a step, and then recurse.
            Result result = max(history.advance(action, ruleset.step(history, action, null)),
                    depth - 1, alpha, beta);

            if(min == null || result.score < min.score)
                min = new Result(action, result.score);

            if(ALPHA_BETA_PRUNING) {
                beta = Math.min(beta, result.score);
                if(beta <= alpha) break;
            }
        }
        return min;
    }
}