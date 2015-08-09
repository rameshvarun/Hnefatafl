package net.varunramesh.hnefatafl.simulator;

/**
 * Created by Varun on 8/7/2015.
 */
public class Utils {
    public static Player otherPlayer(Player currentPlayer) {
        if (currentPlayer == Player.ATTACKER) return Player.DEFENDER;
        else return Player.ATTACKER;
    }

    public static Direction oppositeDirection(Direction dir) {
        switch (dir) {
            case UP:
                return Direction.DOWN;
            case DOWN:
                return Direction.UP;
            case LEFT:
                return Direction.RIGHT;
            case RIGHT:
                return Direction.LEFT;
        }
        throw new UnsupportedOperationException("Unkown Direction.");
    }
}
