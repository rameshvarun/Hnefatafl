package net.varunramesh.hnefatafl.simulator;

import java.io.Serializable;

/**
 * Created by varunramesh on 7/22/15.
 */
public final class Piece implements Serializable {
    public static enum Type {
        ATTACKER,
        DEFENDER,
        KING
    }

    private final Type type;

    Piece(Type type) {
        this.type = type;
    }
}
