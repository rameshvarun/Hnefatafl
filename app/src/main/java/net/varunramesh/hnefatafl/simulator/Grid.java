package net.varunramesh.hnefatafl.simulator;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Immutable class that represents a square grid.
 */
public final class Grid implements Serializable {
    public final Piece[] pieces;
    public final int size;

    public Grid(int size) {
        this.size = size;
        this.pieces = new Piece[size * size];
    }

    private Grid(int size, Piece[] pieces) {
        this.size = size;
        this.pieces = pieces;
    }

    public Set<Position> getPositionsOfPiece(Piece type) {
        Set<Position> positions = new HashSet<>();
        for (int i = 0; i < pieces.length; ++i) {
            if (type.equals(pieces[i]))
                positions.add(indexToPosition(i));
        }
        return positions;
    }

    /** The cached entry set. This is null until the first call to entries. */
    private transient Set<Map.Entry<Position, Piece>> entries = null;

    public Set<Map.Entry<Position, Piece>> getEntries() {
        if (entries == null) {
            // First, construct a mutable set, and add in all of the (Position, Piece) tuples.
            Set<Map.Entry<Position, Piece>> mutableEntries = new HashSet<>();
            for (int i = 0; i < pieces.length; ++i) {
                if (pieces[i] != null)
                    mutableEntries.add(new AbstractMap.SimpleImmutableEntry<Position, Piece>(
                            indexToPosition(i), pieces[i]));
            }

            // Make the set unmodifiable now.
            entries = Collections.unmodifiableSet(mutableEntries);
        }

        return entries;
    }

    public int getNumberOfPieces() {
        int count = 0;
        for (int i = 0; i < pieces.length; ++i) {
            if (pieces[i] != null) ++count;
        }
        return count;
    }

    public Position indexToPosition(int i) {
        return new Position(i / size, i % size);
    }

    public int positionToIndex(Position pos) {
        return pos.getX()*size + pos.getY();
    }

    public Grid add(Map<Position, Piece> map) {
        Piece[] newPieces = Arrays.copyOf(pieces, pieces.length);
        for (Map.Entry<Position, Piece> entry : map.entrySet()) {
            newPieces[positionToIndex(entry.getKey())] = entry.getValue();
        }
        return new Grid(size, newPieces);
    }

    public Piece get(Position pos) {
        assert inBounds(pos) : "pos must be in bounds.";
        return pieces[positionToIndex(pos)];
    }

    public boolean pieceAt(Position pos) {
        assert inBounds(pos) : "pos must be in bounds.";
        return pieces[positionToIndex(pos)] != null;
    }

    public boolean inBounds(Position pos) {
        return pos.getX() >= 0 && pos.getX() < size && pos.getY() >= 0 && pos.getY() < size;
    }

    public Grid remove(Position pos) {
        assert inBounds(pos) : "pos must be in bounds.";
        Piece[] newPieces = Arrays.copyOf(pieces, pieces.length);
        newPieces[positionToIndex(pos)] = null;
        return new Grid(size, newPieces);
    }

    public Grid add(Position pos, Piece piece) {
        assert inBounds(pos) : "pos must be in bounds.";
        Piece[] newPieces = Arrays.copyOf(pieces, pieces.length);
        newPieces[positionToIndex(pos)] = piece;
        return new Grid(size, newPieces);
    }
}
