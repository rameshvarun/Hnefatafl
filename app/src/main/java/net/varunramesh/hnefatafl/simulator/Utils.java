package net.varunramesh.hnefatafl.simulator;

/**
 * Created by Varun on 8/7/2015.
 */
public class Utils {
    public static Player otherPlayer(Player currentPlayer) {
        if (currentPlayer == Player.ATTACKER) return Player.DEFENDER;
        else return Player.ATTACKER;
    }
}
