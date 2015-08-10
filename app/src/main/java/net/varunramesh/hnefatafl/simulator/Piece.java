package net.varunramesh.hnefatafl.simulator;

import java.io.Serializable;

import static net.varunramesh.hnefatafl.simulator.Piece.Type.*;

/**
 * Created by varunramesh on 7/22/15.
 */
public final class Piece implements Serializable {
    public boolean hostileTo(Piece other) {
        if(type == ATTACKER)
            return other.getType() == DEFENDER || other.getType() == KING;
        else
            return other.getType() == ATTACKER;
    }

    public static enum Type {
        ATTACKER,
        DEFENDER,
        KING
    }

    private final Type type;

    Piece(Type type) {
        this.type = type;
    }

    public Type getType() { return type; }
}
