package net.varunramesh.hnefatafl.ai;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;

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
    public Action decide(Board board, Set<Action> actions) {
        assert actions.size() > 0;
        List<Action> list = new ArrayList<>(actions);
        return list.get(random.nextInt(actions.size()));
    }
}
