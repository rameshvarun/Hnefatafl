package net.varunramesh.hnefatafl.simulator.rulesets;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.EventHandler;

import java.util.List;
import java.util.Set;

/**
 * Created by Varun on 8/17/2015.
 */
public interface Ruleset {
    /** Get the starting configuration of the board */
    Board getStartingConfiguration();

    /**
     * Step the game forward by one action, returning a new Board without modifying the original one.
     * We need to get the entire history, as some rulesets call for draws after perpetual repetition.
     * @param history The history of the game state, as a list of boards.
     * @param action The action to simulate.
     * @param eventHandler Optional EventHandler to send events to.
     * @return The new board state.
     */
    Board step(List<Board> history, Action action, EventHandler eventHandler);

    /**
     * Get a set of all of the valid actions that the current player can take.
     * @param history The history of the game state, as a list of boards.
     * @return The set of legal actions.
     */
    Set<Action> getActions(List<Board> history);
}
