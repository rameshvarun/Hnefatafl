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

    public static final class Result {
        public final Action action;
        public final float score;
        public Result(Action action, float score) {
            this.action = action;
            this.score = score;
        }
    }

    /** The number of Plys to search forward */
    public static final int DEFAULT_EARCH_DEPTH = 2;

    public final Player player;
    private final int searchDepth;
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
        Action action = max(board, searchDepth, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY).action;

        if (action == null) {
            Log.e("MinimaxStrategy", "MinimaxStrategy returned null action. Falling back to RandomStrategy.");
            return new RandomStrategy().decide(board, actions);
        }

        return action;
    }

    /** Evaluate how good the board for us */
    public float eval(Board board) {
        if(board.isOver())
            return board.getWinner() == player ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;

        float score = 0.0f;
        for (Map.Entry<Position, Piece> piece : board.getPieces()) {
            if(Board.PlayerOwnsPiece(player, piece.getValue()))
                score += 1.0f;
            else
                score -= 1.0f;
        }
        return score;
    }

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