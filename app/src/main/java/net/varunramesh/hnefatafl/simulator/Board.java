package net.varunramesh.hnefatafl.simulator;

import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by varunramesh on 7/22/15.
 */
public final class Board implements Serializable {
    public static final int BOARD_SIZE = 11;
    public static PMap<Position, Piece> START_CONFIGURATION = createStartConfiguration();

    private final PMap<Position, Piece> pieces;

    public Board() {
        pieces = START_CONFIGURATION;
    }

    public Board(PMap<Position, Piece> pieces) {
        this.pieces = pieces;
    }

    public int getNumberOfPieces() {
        return pieces.size();
    }

    private static PMap<Position, Piece> createStartConfiguration() {
        Map<Position, Piece> pieces = new HashMap<Position, Piece>();
        pieces.put(new Position(5, 5), new Piece(Piece.Type.KING));
        return HashTreePMap.from(pieces);
    }
}
