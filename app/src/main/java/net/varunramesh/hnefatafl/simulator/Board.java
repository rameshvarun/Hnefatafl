package net.varunramesh.hnefatafl.simulator;

import com.annimon.stream.Stream;

import junit.framework.Assert;

import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An immutable object that represents the state of
 */
public final class Board implements Serializable {
    /* Static Methods and Variables */
    private static final String TAG = "Board";

    /* Instance Variables. Would Be final if not for the need for custom serialization logic. */
    private PMap<Position, Piece> pieces;
    private Player currentPlayer;
    private Winner winner;
    private int boardSize;

    /** Serialize the object to a stream. */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(boardSize);
        out.writeObject(currentPlayer);
        out.writeObject(winner);
        out.writeInt(pieces.size());
        for(Map.Entry<Position, Piece> entry : pieces.entrySet()) {
            out.writeObject(entry.getKey());
            out.writeObject(entry.getValue());
        }
    }

    /** Deserialize the object from a stream */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.boardSize = stream.readInt();
        this.currentPlayer = (Player)stream.readObject();
        this.winner = (Winner)stream.readObject();

        Map<Position, Piece> pieces = new HashMap<Position, Piece>();
        int size = stream.readInt();
        for(int i = 0; i < size; ++i) {
            Position key = (Position)stream.readObject();
            Piece value = (Piece)stream.readObject();
            pieces.put(key, value);
        }
        this.pieces = HashTreePMap.from(pieces);
    }

    /** Instantiate a board with the given values */
    public Board(PMap<Position, Piece> pieces, Player currentPlayer, Winner winner, int boardSize) {
        this.pieces = pieces;
        this.currentPlayer = currentPlayer;
        this.winner = winner;
        this.boardSize = boardSize;
    }

    /** Get the number of pieces currently on the board */
    public int getNumberOfPieces() { return pieces.size(); }

    /** Get the size of the board. Can safely assume that the board is allways a square */
    public int getBoardSize() { return boardSize; }

    /** Return the Winner of the game. */
    public Winner getWinner() { return winner; }

    /** Get the player who should be taking a turn */
    public Player getCurrentPlayer() { return currentPlayer; }

    /** Return an immutable map of all of the piece locations */
    public PMap<Position, Piece> getPieces() { return pieces; }

    /** Return true if the winner of the match has been determined. */
    public boolean isOver() { return winner != Winner.UNDETERMINED; }

    /** Check if a position is inside the board */
    public boolean contains(Position position) {
        return position.getX() >= 0 && position.getY() >= 0 &&
                position.getX() < boardSize && position.getY() < boardSize;
    }

    /** Get all of the positions of the pieces of a certain type */
    public Set<Position> getPositionsOfPiece(Piece type) {
        Set<Position> pieces = new HashSet<>();
        for (Map.Entry<Position, Piece> piece : getPieces().entrySet()) {
            if (piece.getValue().equals(type))
                pieces.add(piece.getKey());
        }
        return pieces;
    }

    /** Return the center square. */
    public Position getCenterSquare() {
        return new Position(boardSize / 2, boardSize / 2);
    }

    /** Return the corner squares */
    public Set<Position> getCornerSquares() {
        Set<Position> squares = new HashSet<>();
        squares.add(new Position(0, 0));
        squares.add(new Position(boardSize - 1, 0));
        squares.add(new Position(0, boardSize - 1));
        squares.add(new Position(boardSize - 1, boardSize - 1));
        return squares;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board board = (Board) o;

        if (boardSize != board.boardSize) return false;
        if (!pieces.equals(board.pieces)) return false;
        if (currentPlayer != board.currentPlayer) return false;
        return winner == board.winner;

    }

    @Override
    public int hashCode() {
        int result = pieces.hashCode();
        result = 31 * result + currentPlayer.hashCode();
        result = 31 * result + winner.hashCode();
        result = 31 * result + boardSize;
        return result;
    }
}
