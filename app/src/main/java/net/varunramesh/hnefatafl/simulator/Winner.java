package net.varunramesh.hnefatafl.simulator;

/**
 * Enum to represent the winner of a game state
 */
public enum Winner {
    /** The winner of the game has not been determined yet */
    UNDETERMINED,
    /** The attacking player has won the game */
    ATTACKER,
    /** The defending player has won the game */
    DEFENDER,
    /** The game has ended in a tie / draw */
    DRAW;

    public static Winner fromPlayer(Player player) {
        if (player.equals(Player.ATTACKER)) return ATTACKER;
        else return DEFENDER;
    }

    public Player toPlayer() {
        switch (this) {
            case ATTACKER:
                return Player.ATTACKER;
            case DEFENDER:
                return Player.DEFENDER;
        }
        throw new UnsupportedOperationException(this.toString() + " cannot be converted to a Player.");
    }
}
