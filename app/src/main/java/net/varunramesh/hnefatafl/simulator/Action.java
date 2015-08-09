package net.varunramesh.hnefatafl.simulator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.io.Serializable;

/**
 * Represents an action that a player can take.
 */
public class Action implements Serializable {
    /** The player that is making this move. */
    private final Player player;

    /** The position that the piece is moving from. */
    private final Position from;

    /** The position that the piece is moving to. */
    private final Position to;

    public Action(Player player, Position from, Position to) {
        this.player = player;
        this.from = from;
        this.to = to;
    }

    // Getters
    public Player getPlayer() { return player; }
    public Position getFrom() { return from; }
    public Position getTo() { return to; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;
        return player == action.player &&
                from.equals(action.from) &&
                to.equals(action.to);
    }

    @Override
    public int hashCode() {
        int result = player.hashCode();
        result = 31 * result + from.hashCode();
        result = 31 * result + to.hashCode();
        return result;
    }
}
