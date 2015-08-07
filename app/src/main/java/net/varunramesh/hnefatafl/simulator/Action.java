package net.varunramesh.hnefatafl.simulator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents an action that a player can take.
 */
public class Action implements Saveable {
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

    public Action(JsonElement element) {
        assert element.isJsonArray();
        JsonArray array = element.getAsJsonArray();

        this.player = Player.valueOf(array.get(0).getAsString());
        this.from = new Position(array.get(1));
        this.to = new Position(array.get(2));
    }

    // Getters
    public Player getPlayer() { return player; }
    public Position getFrom() { return from; }
    public Position getTo() { return to; }

    @Override
    public JsonElement toJson() {
        JsonArray action = new JsonArray();
        action.add(new JsonPrimitive(player.toString()));
        action.add(from.toJson());
        action.add(to.toJson());
        return action;
    }

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
