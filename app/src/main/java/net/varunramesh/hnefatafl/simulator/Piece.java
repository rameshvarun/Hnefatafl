package net.varunramesh.hnefatafl.simulator;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import java.io.Serializable;

import static net.varunramesh.hnefatafl.simulator.Piece.Type.*;

/**
 * Created by varunramesh on 7/22/15.
 */
public final class Piece implements Saveable {
    public static enum Type {
        ATTACKER,
        DEFENDER,
        KING
    }

    private final Type type;

    Piece(Type type) {
        this.type = type;
    }

    Piece(JsonElement json) {
        assert json.isJsonPrimitive();
        this.type = Type.valueOf(json.getAsString());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(type.toString());
    }

    public Type getType() { return type; }
}
