package net.varunramesh.hnefatafl.simulator;

/**
 * Enum that represents the possible piece types.
 */
public enum Piece {
    ATTACKER,
    DEFENDER,
    KING,
    KNIGHT,
    COMMANDER;

    public boolean hostileTo(Piece other) {
        if(this == ATTACKER)
            return other == DEFENDER || other == KING;
        else
            return other == ATTACKER;
    }
}
