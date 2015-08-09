package net.varunramesh.hnefatafl.simulator;

import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.pcollections.PStack;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;

/**
 * Created by varunramesh on 7/22/15.
 */
public class GameState implements Saveable {
    /* Instance Variables */
    private final GameType type;
    private final Deque<Board> boards = new ArrayDeque<Board>();
    private final Deque<Action> actions = new ArrayDeque<Action>();

    @Override
    public JsonElement toJson() {
        JsonObject state = new JsonObject();
        state.add("type", new JsonPrimitive(type.toString()));

        // Save the board history.
        JsonArray boardsJson = new JsonArray();
        for(Board board : boards) boardsJson.add(board.toJson());
        state.add("boards", boardsJson);

        // Save the action history.
        JsonArray actionsJson = new JsonArray();
        for(Action action : actions) actionsJson.add(action.toJson());
        state.add("actions", actionsJson);

        return state;
    }

    public enum GameType {
        PASS_AND_PLAY,
        PLAYER_VS_AI,
        ONLINE
    }

    public GameState(GameType type) {
        this.type = type;
        boards.push(new Board());
    }

    public GameState(JsonElement element) {
        assert element.isJsonObject();
        JsonObject state = element.getAsJsonObject();
        this.type = GameType.valueOf(state.get("type").getAsString());

        // Load the board history.
        JsonArray boardsJson = state.get("boards").getAsJsonArray();
        for(JsonElement boardJson : boardsJson)
            boards.push(new Board(boardJson));

        // Load the action history.
        JsonArray actionsJson = state.get("actions").getAsJsonArray();
        for(JsonElement actionJson : actionsJson)
            actions.push(new Action(actionJson));
    }

    /** Returns true if no moves have been made yet */
    public boolean isFirstMove() { return actions.size() == 0; }

    /** The current configuration of the board */
    public Board currentBoard() { return boards.peek(); }

    public void pushBoard(Action action, Board board) {
        actions.push(action);
        boards.push(board);
    }
}