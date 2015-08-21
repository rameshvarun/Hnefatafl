package net.varunramesh.hnefatafl.simulator;

import com.annimon.stream.function.Consumer;

import net.varunramesh.hnefatafl.simulator.rulesets.Ruleset;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

/**
 * Created by varunramesh on 7/22/15.
 */
public class GameState implements Serializable {
    /** A GameType instance */
    private final GameType type;

    /** The entire board history of the match */
    private final List<Board> boards = new LinkedList<>();

    /** The action history of the match */
    private final List<Action> actions = new LinkedList<>();

    /** The Ruleset that this match uses */
    private final Ruleset ruleset;
    public Ruleset getRuleset() { return ruleset; }

    /** A unique id to reference this game */
    private final UUID uuid;
    public UUID getUUID() { return uuid; }

    /** The date when the game was created */
    private final Date createdDate;
    public Date getCreatedDate() { return createdDate; }

    /** The date when the last move was made. */
    private Date lastMoveDate;
    public Date getLastMoveDate() { return lastMoveDate; }

    /** A function that can be used to save the GameState */
    private transient Persister persister;
    public void setPersister(Persister persister) { this.persister = persister; }

    public GameState(GameType type, Ruleset ruleset) {
        this.type = type;
        this.boards.add(ruleset.getStartingConfiguration());
        this.uuid = UUID.randomUUID();
        this.ruleset = ruleset;

        this.createdDate = new Date();
        this.lastMoveDate = new Date();
    }

    /** Returns true if no moves have been made yet */
    public boolean isFirstMove() { return actions.size() == 0; }

    /** The current configuration of the board */
    public Board currentBoard() { return boards.get(boards.size() - 1); }

    /** Get the list of boards */
    public List<Board> getBoards() { return new LinkedList<>(boards); }

    /** Get the list of actions */
    public List<Action> getActions() { return new LinkedList<>(actions); }

    /**
     * Push an action and it's resulting board onto the game's history. If this GameState object
     * has a persister set, then try to persist the state.
     * @param action The action that the current player has just finalized.
     * @param board The new state of the board that resulted from that action.
     */
    public void pushBoard(Action action, Board board) {
        // Push the new board, and the action that got us there.
        this.actions.add(action);
        this.boards.add(board);

        // Save the current time.
        this.lastMoveDate = new Date();

        // Try to persist the game state.
        if(this.persister != null) {
            this.persister.persist(this);
        }
    }

    /** Get the GameType of this GameState */
    public GameType getType() { return type; }
}