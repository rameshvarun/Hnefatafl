package net.varunramesh.hnefatafl.ai;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class UCTStrategy implements AIStrategy {
	private final String TAG = "UCTStrategy";

	public static final class Record {
        public int wins;
        public int draws;
        public int losses;

        public synchronized double getWinPercentage() {
        	return ((double)wins)/(wins + draws + losses);
        }

        public synchronized double getLossPercentage() {
        	return ((double)losses)/(wins + draws + losses);
        }

        public synchronized int getPlays() {
        	return wins + draws + losses;
        }

        public synchronized void addWin() { ++wins; }
        public synchronized void addDraw() { ++draws; }
        public synchronized void addLoss() { ++losses; }
    }

    /** The player that the AI represents. */
    private final Player player;
    private final Ruleset ruleset;

    public UCTStrategy(Ruleset ruleset, Player player) {
        this.ruleset = ruleset;
        this.player = player;
    }

    private final ConcurrentHashMap<Board, Record> records = new ConcurrentHashMap<>();
    private final AtomicInteger plays = new AtomicInteger();

    @Override
    public Action decide(History history, Set<Action> actions) {
        Board currentBoard = history.getCurrentBoard();
        Assert.assertEquals("AI player is current player.", currentBoard.getCurrentPlayer(), player);
        Assert.assertTrue("We need some options to pick from.", ruleset.getActions(history).size() > 0);

        // Reset record tracking.
        records.clear();
        plays.set(0);

        return new RandomStrategy().decide(history, actions);
    }

}