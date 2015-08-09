package net.varunramesh.hnefatafl.ai;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;

import java.util.Set;

/**
 * Created by varunramesh on 7/27/15.
 */
public interface AIStrategy {
    Action decide(Board board, Set<Action> actions);
}
