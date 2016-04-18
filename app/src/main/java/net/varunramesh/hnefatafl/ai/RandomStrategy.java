package net.varunramesh.hnefatafl.ai;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.History;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Implements a bot that randomly chooses moves.
 */
public class RandomStrategy implements AIStrategy {
    private final Random random = new Random();

    @Override
    public Action decide(History history, Set<Action> actions) {
        assert actions.size() > 0 : "Actions set must not be empty.";
        List<Action> list = new ArrayList<>(actions);
        return list.get(random.nextInt(actions.size()));
    }
}
