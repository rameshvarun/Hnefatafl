package net.varunramesh.hnefatafl.simulator;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.io.Serializable;

/**
 * Created by varunramesh on 7/22/15.
 */
public final class Position implements Saveable {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position(JsonElement json) {
        assert json.isJsonArray();
        JsonArray array = json.getAsJsonArray();
        assert array.size() == 2;

        this.x = array.get(0).getAsInt();
        this.y = array.get(1).getAsInt();
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public Position getNeighbor(Direction dir) {
        switch (dir) {
            case UP:
                return new Position(x, y + 1);
            case DOWN:
                return new Position(x, y - 1);
            case LEFT:
                return new Position(x - 1, y);
            case RIGHT:
                return new Position(x + 1, y);
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        if (x != position.x) return false;
        return y == position.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive(x));
        array.add(new JsonPrimitive(y));
        return array;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
