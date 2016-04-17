package net.varunramesh.hnefatafl.simulator;

import junit.framework.Assert;

import org.pcollections.ConsPStack;
import org.pcollections.HashTreePMap;
import org.pcollections.PStack;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by varunramesh on 4/15/16.
 */
public final class History implements Serializable {
    private PStack<Board> boards;
    private PStack<Action> actions;

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
    private History(PStack<Board> boards, PStack<Action> actions) {
        this.boards = boards;
        this.actions = actions;
    }

    /** Returns the current board. */
    public Board getCurrentBoard() { return boards.get(0); }
    public Action getLastAction() { return actions.get(0); }

    public PStack<Board> getBoards() { return boards; }
    public PStack<Action> getActions() { return actions; }

    /** Returns true if no moves have been made yet */
    public boolean isFirstMove() { return actions.size() == 0; }

    /**
     * Advance history by one timestep, returning the new History object.
     * @param action The action that was taken on this timstep.
     * @param next The board that resulted from the last action.
     * @return The new history object containing the new action and board state.
     */
    public History advance(Action action, Board next) {
        return new History(boards.plus(next), actions.plus(action));
    }

    /**
     * Get a history object that represents the previous history before this one.
     * @return
     */
    public History previous() {
        Assert.assertFalse("This should not be the first move.", isFirstMove());
        return new History(boards.subList(1), actions.subList(1));
    }

    /** Serialize the object to a stream. */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(new LinkedList<Board>(boards));
        out.writeObject(new LinkedList<Action>(actions));
    }

    /** Deserialize the object from a stream */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.boards = ConsPStack.from((LinkedList<Board>) stream.readObject());
        this.actions = ConsPStack.from((LinkedList<Action>) stream.readObject());
    }
}
