package net.varunramesh.hnefatafl.simulator.rulesets;

import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Winner;

import org.pcollections.HashTreePMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Varun on 8/18/2015.
 */
public class Brandubh extends FeltarHnefatafl {
    private static final String TAG = "Brandubh";
    public static final int BOARD_SIZE = 7;

    @Override
    public Board getStartingConfiguration() {
        Map<Position, Piece> pieces = new HashMap<Position, Piece>();

        // King position
        pieces.put(new Position(3, 3), Piece.KING);

        // Defender locations
        pieces.put(new Position(2, 3), Piece.DEFENDER);
        pieces.put(new Position(4, 3), Piece.DEFENDER);
        pieces.put(new Position(3, 2), Piece.DEFENDER);
        pieces.put(new Position(3, 4), Piece.DEFENDER);

        // Attacker locations
        pieces.put(new Position(0, 3), Piece.ATTACKER);
        pieces.put(new Position(1, 3), Piece.ATTACKER);
        pieces.put(new Position(5, 3), Piece.ATTACKER);
        pieces.put(new Position(6, 3), Piece.ATTACKER);

        pieces.put(new Position(3, 0), Piece.ATTACKER);
        pieces.put(new Position(3, 1), Piece.ATTACKER);
        pieces.put(new Position(3, 5), Piece.ATTACKER);
        pieces.put(new Position(3, 6), Piece.ATTACKER);

        return new Board(HashTreePMap.from(pieces), Player.ATTACKER, Winner.UNDETERMINED, BOARD_SIZE);
    }

    @Override
    public boolean isKingOnlySquare(Position pos) {
        return (pos.getX() == 3 && pos.getY() == 3) ||
                (pos.getX() == 0 && pos.getX() == 0) ||
                (pos.getX() == 0 && pos.getX() == 6) ||
                (pos.getX() == 6 && pos.getX() == 0) ||
                (pos.getX() == 6 && pos.getX() == 6);
    }

    @Override
    public int getAISearchDepth() {
        return 4;
    }
}
