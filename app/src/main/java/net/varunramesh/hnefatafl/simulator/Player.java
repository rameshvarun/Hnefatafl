package net.varunramesh.hnefatafl.simulator;

/**
 * Created by varunramesh on 7/22/15.
 */
public enum Player {
    ATTACKER, DEFENDER;

    /** Return the ENUM representing the other player */
    public Player other() {
        if (this == Player.ATTACKER) return Player.DEFENDER;
        else return Player.ATTACKER;
    }

    /** Return true if the given piece is owned by this player. */
    public boolean ownsPiece(Piece piece) {
        return (this == ATTACKER && (piece == Piece.ATTACKER || piece == Piece.COMMANDER))
                || (this == DEFENDER && (piece == Piece.DEFENDER || piece == Piece.KING || piece == Piece.KNIGHT));
    }
}
