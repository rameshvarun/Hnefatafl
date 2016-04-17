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

    /** The entire history of the match, containing boards and actions that have been taken. */
    private History history;

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
        this.history = new History(ruleset.getStartingConfiguration());
        this.uuid = UUID.randomUUID();
        this.ruleset = ruleset;

        this.createdDate = new Date();
        this.lastMoveDate = new Date();
    }

    /** Get the list of boards. */
    public History getHistory() { return history; }

    /**
     * Returns the current board.
     * @return The board object.
     */
    public Board getCurrentBoard() { return history.getCurrentBoard(); }

    /** Returns true if no moves have been made yet */
    public boolean isFirstMove() { return history.isFirstMove(); }


    /**
     * Push an action and it's resulting board onto the game's history. If this GameState object
     * has a persister set, then try to persist the state.
     * @param action The action that the current player has just finalized.
     * @param board The new state of the board that resulted from that action.
     */
    public void advance(Action action, Board board) {
        // Push the new board, and the action that got us there.
        this.history = history.advance(action, board);

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