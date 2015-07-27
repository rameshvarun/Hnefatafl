package net.varunramesh.hnefatafl.simulator;

import android.support.annotation.NonNull;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An immutable object that represents the state of
 */
public final class Board implements Saveable {
    /* Instance Variables */
    private final PMap<Position, Piece> pieces;
    private final Player currentPlayer;

    /** Default contructor that creates a game board in the starting configuration. */
    public Board() {
        pieces = START_CONFIGURATION; // Set pieces to start configuration.
        currentPlayer = Player.ATTACKER; // Attacker goes first.
    }

    /** Load a Board Object from JSON */
    public Board(JsonElement element) {
        assert element.isJsonObject();
        JsonArray pieces = element.getAsJsonObject()
                .get("pieces").getAsJsonArray();

        // Load pieces.
        Map<Position, Piece> mutPieces = new HashMap<>();
        for(JsonElement piece : pieces) {
            mutPieces.put(
                    new Position(piece.getAsJsonArray().get(0)),
                    new Piece(piece.getAsJsonArray().get(1))
            );
        }
        this.pieces = HashTreePMap.from(mutPieces);

        this.currentPlayer = Player.valueOf(
                element.getAsJsonObject().get("currentPlayer").getAsString());
    }

    /** Instantiate a board with the given values */
    public Board(PMap<Position, Piece> pieces, Player currentPlayer) {
        this.pieces = pieces;
        this.currentPlayer = currentPlayer;
    }

    public int getNumberOfPieces() {
        return pieces.size();
    }


    @Override
    public JsonElement toJson() {
        JsonObject board = new JsonObject();

        // Serialize Piece locations.
        JsonArray pieces = new JsonArray();
        for(Map.Entry<Position, Piece> piece : this.pieces.entrySet()) {
            JsonArray entry = new JsonArray();
            entry.add(piece.getKey().toJson());
            entry.add(piece.getValue().toJson());
            pieces.add(entry);
        }
        board.add("pieces", pieces);

        // Save currentPlayer.
        board.add("currentPlayer", new JsonPrimitive(currentPlayer.toString()));

        return board;
    }

    /* Static Methods and Variables */
    public static final int BOARD_SIZE = 11;
    public static PMap<Position, Piece> START_CONFIGURATION = createStartConfiguration();

    private static PMap<Position, Piece> createStartConfiguration() {
        Map<Position, Piece> pieces = new HashMap<Position, Piece>();

        // King position
        pieces.put(new Position(5, 5), new Piece(Piece.Type.KING));

        // Defender locations
        pieces.put(new Position(7, 5), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(6, 5), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(4, 5), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(3, 5), new Piece(Piece.Type.DEFENDER));

        pieces.put(new Position(5, 7), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(5, 6), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(5, 4), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(5, 3), new Piece(Piece.Type.DEFENDER));

        pieces.put(new Position(4, 4), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(4, 6), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(6, 4), new Piece(Piece.Type.DEFENDER));
        pieces.put(new Position(6, 6), new Piece(Piece.Type.DEFENDER));

        // Attacker locations
        pieces.put(new Position(0, 3), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 4), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 5), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 6), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(0, 7), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(1, 5), new Piece(Piece.Type.ATTACKER));

        pieces.put(new Position(10, 3), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 4), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 5), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 6), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(10, 7), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(9, 5), new Piece(Piece.Type.ATTACKER));

        pieces.put(new Position(3, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(4, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(6, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(7, 0), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 1), new Piece(Piece.Type.ATTACKER));

        pieces.put(new Position(3, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(4, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(6, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(7, 10), new Piece(Piece.Type.ATTACKER));
        pieces.put(new Position(5, 9), new Piece(Piece.Type.ATTACKER));

        return HashTreePMap.from(pieces);
    }

    public Set<Action> getActions(Position position) {
        assert pieces.containsKey(position);

        Set<Action> actions = new HashSet<>();
        Piece piece = pieces.get(position);

        // If the current player is the attacker and the piece is a defender,
        // return an empty set.
        if(currentPlayer == Player.ATTACKER &&
                (piece.getType() == Piece.Type.KING ||
                        piece.getType() == Piece.Type.ATTACKER)) {
            return actions;
        }

        // If the current player is the defender and the peice is a defender,
        // return the empty set.
        if(currentPlayer == Player.DEFENDER &&
                (piece.getType() == Piece.Type.ATTACKER)) {
            return actions;
        }

        throw new UnsupportedOperationException();
    }

    public Set<Map.Entry<Position, Piece>> getPieces() {
        return pieces.entrySet();
    }
}
