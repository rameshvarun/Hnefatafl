package net.varunramesh.hnefatafl.simulator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by varunramesh on 7/22/15.
 */
public class GameState implements Serializable {
    /* Instance Variables */
    private final GameType type;
    private final Deque<Board> boards = new ArrayDeque<Board>();
    private final Deque<Action> actions = new ArrayDeque<Action>();

    public GameState(GameType type) {
        this.type = type;
        boards.push(new Board());
    }

    /** Returns true if no moves have been made yet */
    public boolean isFirstMove() { return actions.size() == 0; }

    /** The current configuration of the board */
    public Board currentBoard() { return boards.peek(); }

    public void pushBoard(Action action, Board board) {
        actions.push(action);
        boards.push(board);
    }

    public GameType getType() { return type; }
}