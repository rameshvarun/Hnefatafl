package net.varunramesh.hnefatafl.ai;

import android.util.Log;
import android.util.Pair;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;

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
    public static final int DEFAULT_EARCH_DEPTH = 2;

    /** The player that the AI represents. */
    public final Player player;

    /** The number of Plys to search forward */
    private final int searchDepth;

    /**
     * Variable used for counting how many leaf nodes of the game tree we visit. It is set to zero
     * before each search, and read after the search.
     * */
    private int leaves = 0;

    public MinimaxStrategy(Player player) {
        this(player, DEFAULT_EARCH_DEPTH);
    }

    public MinimaxStrategy(Player player, int searchDepth) {
        this.player = player;
        this.searchDepth = searchDepth;
    }


    @Override
    public Action decide(Board board, Set<Action> actions) {
        Assert.assertEquals("AI player is current player.", board.getCurrentPlayer(), player);
        Assert.assertTrue("We need some options to pick from.", board.getActions().size() > 0);

        // Clear leaves count.
        leaves = 0;

        Log.d(TAG, "Starting a Minimax search with depth " + searchDepth + ".");
        Action action = max(board, searchDepth, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY).action;
        Log.d(TAG, "MinimaxStrategy searched " + leaves + " possible game states.");

        // Should never happen, but to prevent crashes.
        if (action == null) {
            Log.wtf(TAG, "MinimaxStrategy returned null action. Falling back to RandomStrategy.");
            return new RandomStrategy().decide(board, actions);
        }

        return action;
    }

    /** Evaluate how good the board for us */
    public float eval(Board board) {
        ++leaves; // Evaling a board means we have reached a leaf.

        // If the game already has a winner, then return -infinity or infinity
        if(board.isOver())
            return board.getWinner() == player ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;

        float score = 0.0f;

        // Start by counting how many pieces each player has.
        for (Map.Entry<Position, Piece> piece : board.getPieces()) {
            if(Board.PlayerOwnsPiece(player, piece.getValue()))
                score += 1.0f;
            else
                score -= 1.0f;
        }

        // Find the shortest distance between the King and any corner of the board.
        for(Map.Entry<Position, Piece> king : board.getPieces(Piece.KING)) {
            int topLeftDist = king.getKey().distanceTo(new Position(0, 0));
            int topRightDist = king.getKey().distanceTo(new Position(board.getBoardSize() - 1, 0));
            int bottomLeftDist = king.getKey().distanceTo(new Position(0, board.getBoardSize() - 1));
            int bottomRightDist = king.getKey().distanceTo(new Position(board.getBoardSize() - 1, board.getBoardSize() - 1));

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

    public Result max(Board board, int depth, float alpha, float beta) {
        Assert.assertEquals("AI player is current player.", board.getCurrentPlayer(), player);
        if(board.isOver() || depth == 0) return new Result(null, eval(board));

        Result max = null;
        for(Action action : board.getActions()) {
            Result result = min(board.step(action), depth - 1, alpha, beta);
            if(max == null || result.score > max.score)
                max = new Result(action, result.score);

            if(ALPHA_BETA_PRUNING) {
                alpha = Math.max(alpha, result.score);
                if(beta <= alpha) break;
            }
        }
        return max;
    }

    public Result min(Board board, int depth, float alpha, float beta) {
        Assert.assertFalse("AI Player is not current player.", board.getCurrentPlayer() == player);
        if(board.isOver() || depth == 0) return new Result(null, eval(board));

        Result min = null;
        for(Action action : board.getActions()) {
            Result result = max(board.step(action), depth - 1, alpha, beta);
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