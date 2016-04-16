package net.varunramesh.hnefatafl.simulator;

import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import java.io.Serializable;

/**
 * Created by varunramesh on 4/15/16.
 */
public final class History implements Serializable {
    public final PStack<Board> boards;
    public final PStack<Action> actions;

    /**
     * Initialize a History object with a single board.
     * @param start The initial board configuration.
     */
    public History(Board start) {
        boards = ConsPStack.<Board>empty().plus(start);
        actions = ConsPStack.empty();
    }

    /**
     * Initialize a History object by providing both the list of boards and actions.
     * @param boards
     * @param actions
     */
    public History(PStack<Board> boards, PStack<Action> actions) {
        this.boards = boards;
        this.actions = actions;
    }

    /**
     * Advance history by one timestep, returning the new History object.
     * @param action The action that was taken on this timstep.
     * @param next The board that resulted from the last action.
     * @return The new history object containing the new action and board state.
     */
    public History advance(Action action, Board next) {
        return new History(boards.plus(next), actions.plus(action));
    }
}
