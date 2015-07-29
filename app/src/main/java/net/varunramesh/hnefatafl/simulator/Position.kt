package net.varunramesh.hnefatafl.simulator

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

import java.io.Serializable

/**
 * Created by varunramesh on 7/22/15.
 */
public data class Position(val x: Int, val y: Int) : Saveable {
    /** Deserialize the position from a JSON Element */
    public constructor(json: JsonElement) : this(
            json.getAsJsonArray().get(0).getAsInt(),
            json.getAsJsonArray().get(1).getAsInt()) {
    }

    /** Get the neigboring position in a specific direction */
    public fun getNeighbor(dir: Direction): Position {
        when (dir) {
            Direction.UP -> return Position(x, y + 1)
            Direction.DOWN -> return Position(x, y - 1)
            Direction.LEFT -> return Position(x - 1, y)
            Direction.RIGHT -> return Position(x + 1, y)
        }
    }

    /** Serialize this position to a JsonElement */
    override fun toJson(): JsonElement {
        val array = JsonArray()
        array.add(JsonPrimitive(x))
        array.add(JsonPrimitive(y))
        return array
    }
}
