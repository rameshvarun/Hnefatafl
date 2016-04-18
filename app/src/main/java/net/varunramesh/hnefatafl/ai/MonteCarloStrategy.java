package net.varunramesh.hnefatafl.ai;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.History;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Winner;
import net.varunramesh.hnefatafl.simulator.rulesets.Ruleset;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by varunramesh on 4/18/16.
 */
public class MonteCarloStrategy implements AIStrategy {
    /** The default value for timeout. */
    public static final double DEFAULT_TIMEOUT = 5.0;

    /** The amount of time that the AI should spend making depth charges. */
    private final double timeout;

    private final Ruleset ruleset;

    /** The player that the AI represents. */
    private final Player player;

    public MonteCarloStrategy(Ruleset ruleset, Player player) {
        this(ruleset, player, DEFAULT_TIMEOUT);
    }

    public MonteCarloStrategy(Ruleset ruleset, Player player, double timeout) {
        this.ruleset = ruleset;
        this.player = player;
        this.timeout = timeout;
    }

    private final Random random = new Random();

    @Override
    public Action decide(History history, List<Action> actionsSet) {
        Board currentBoard = history.getCurrentBoard();
        assert currentBoard.getCurrentPlayer().equals(player) : "AI player is current player.";
        assert ruleset.getActions(history).size() > 0 : "We need some options to pick from.";

        Action[] actions = (Action[]) actionsSet.toArray(new Action[actionsSet.size()]);
        assert actions.length > 0;
        if (actions.length < 2) return actions[0];

        // We keep stastics for the depth charges that follow from each action.
        int[] wins = new int[actions.length];
        int[] draws = new int[actions.length];
        int[] losses = new int[actions.length];

        int totalCharges = 0;
        int currentAction = 0;

        long deadline = System.currentTimeMillis() + (long)(timeout * 1000);
        while (System.currentTimeMillis() < deadline) {
            // Perform a depth charge, assuming that from the current state, we take the expected action.
            History current = history.advance(actions[currentAction],
                    ruleset.step(history, actions[currentAction], null));
            while (!current.getCurrentBoard().isOver()) {
                List<Action> probeActionsSet = ruleset.getActions(current);
                Action[] probeActions = probeActionsSet.toArray(new Action[probeActionsSet.size()]);
                Action probeAction = probeActions[random.nextInt(probeActions.length)];

                current = history.advance(probeAction, ruleset.step(current, probeAction, null));
            }

            assert current.getCurrentBoard().isOver(): "The match is over.";
            if (current.getCurrentBoard().getWinner().equals(Winner.DRAW)) draws[currentAction]++;
            else if (current.getCurrentBoard().getWinner().equals(Winner.fromPlayer(player))) wins[currentAction]++;
            else losses[currentAction]++;

            currentAction = (currentAction + 1) % actions.length; // Advance to the next action.
            totalCharges += 1; // Record that we've finished one total change.
        }
        System.out.println("MonteCarloStrategy: " + totalCharges + " depth charges simulated.");

        Action bestAction = null;
        float bestWinPercentage = 0;
        float bestLossPercentage = 0;
        float bestScore = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < actions.length; ++i) {
            float winPercentage = (float)wins[i] / (wins[i] + draws[i] + losses[i]);
            float lossPercentage = (float)losses[i] / (wins[i] + draws[i] + losses[i]);
            float score = winPercentage - lossPercentage;

            if (score > bestScore) {
                bestAction = actions[i];
                bestScore = score;
                bestWinPercentage = winPercentage;
                bestLossPercentage = lossPercentage;
            }
        }

        System.out.println("MonteCarloStrategy: Win Percentage - " + bestWinPercentage + ", Loss Percentage - " + bestLossPercentage);

        return bestAction;
    }
}
