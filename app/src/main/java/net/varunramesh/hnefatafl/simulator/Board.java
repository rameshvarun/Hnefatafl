package net.varunramesh.hnefatafl.simulator;

import android.support.annotation.NonNull;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.MapPSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

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

    /** Get the number of pieces currently on the board */
    public int getNumberOfPieces() {
        return pieces.size();
    }

    public Board step(Action action, EventHandler eventHandler) {
        assert action.getPlayer() == currentPlayer;

        // TODO: Probably verify move and complain if it's illegal.
        // for now, assume that the move was given by us.

        // Move the piece.
        Piece piece = pieces.get(action.getFrom());
        PMap<Position, Piece> newPieces = pieces.minus(action.getFrom());
        newPieces = pieces.plus(action.getTo(), piece);
        eventHandler.MovePiece(action.getFrom(), action.getTo());

        // TODO: Handle captures

        return new Board(newPieces, Utils.otherPlayer(currentPlayer));
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

    /** Check if a position is inside the board */
    public boolean contains(Position position) {
        return position.getX() >= 0 && position.getY() >= 0 &&
                position.getX() < 11 && position.getY() < 11;
    }

    /** Returns true if the square is a King-Only Square */
    public boolean isKingOnlySquare(Position pos) {
        return CENTER_SQUARE.equals(pos) || getRefugeeSquares().contains(pos);
    }

    /** Get all of the actions that the piece at the given position can take */
    public Set<Action> getActions(Position position) {
        assert pieces.containsKey(position);

        Set<Action> actions = new HashSet<>();
        Piece piece = pieces.get(position);

        // If the current player does not own the piece, return
        // the empty set.
        if(!PlayerOwnsPiece(currentPlayer, piece)) return actions;

        for(Direction dir : Direction.values()) {
            for(Position pos = position.getNeighbor(dir); contains(pos); pos = pos.getNeighbor(dir)) {
                if(pieces.containsKey(pos)) break;
                else {
                    if(piece.getType() != Piece.Type.KING && isKingOnlySquare(pos))
                        continue;
                    actions.add(new Action(currentPlayer, position, pos));
                }
            }
        }
        return actions;
    }

    /** Get all of the actions that the given player can take. */
    public Set<Action> getActions(Player player) {
        Set<Action> actions = new HashSet<>();
        for(Map.Entry<Position, Piece> piece : pieces.entrySet()) {
            if (PlayerOwnsPiece(player, piece.getValue()))
                actions.addAll(getActions(piece.getKey()));
        }
        return actions;
    }

    /** Get all of the actions that the curent player can take. */
    public Set<Action> getActions() {
        return getActions(currentPlayer);
    }

    public Set<Map.Entry<Position, Piece>> getPieces() {
        return pieces.entrySet();
    }

    /* Static Methods and Variables */
    public static final int BOARD_SIZE = 11;
    public static PMap<Position, Piece> START_CONFIGURATION = createStartConfiguration();

    public static Position CENTER_SQUARE = new Position(5, 5);
    public static final PSet<Position> REFUGEE_SQUARES = getRefugeeSquares();

    /** Constructs the starting configuration of pieces on the board */
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

    private static PSet<Position> getRefugeeSquares() {
        Set<Position> squares = new HashSet<>();
        squares.add(new Position(0, 0));
        squares.add(new Position(10, 0));
        squares.add(new Position(0, 10));
        squares.add(new Position(10, 10));
        return HashTreePSet.from(squares);
    }

    public static boolean PlayerOwnsPiece(Player player, Piece.Type piece) {
        return (player == Player.ATTACKER && piece == Piece.Type.ATTACKER)
                || (player == Player.DEFENDER && (piece == Piece.Type.DEFENDER || piece == Piece.Type.KING));
    }

    public static boolean PlayerOwnsPiece(Player player, Piece piece) {
        return PlayerOwnsPiece(player, piece.getType());
    }
}
