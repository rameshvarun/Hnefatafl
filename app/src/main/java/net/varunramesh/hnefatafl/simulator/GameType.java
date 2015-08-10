package net.varunramesh.hnefatafl.simulator;

import java.io.Serializable;

/** A GameType instance, which is essentially a tagged union. */
public abstract class GameType implements Serializable {
    abstract public String type();

    public static final class PassAndPlay extends GameType {
        public String type() { return "PassAndPlay"; }
    }
    public static final class PlayerVsAI extends GameType {
        private final Player humanPlayer;
        public String type() { return "PlayerVsAI"; }
        public PlayerVsAI(Player humanPlayer) {
            this.humanPlayer = humanPlayer;
        }
        public Player getHumanPlayer() { return humanPlayer; }
        public Player getAIPlayer() { return Utils.otherPlayer(humanPlayer); };
    }
}