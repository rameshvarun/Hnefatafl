package net.varunramesh.hnefatafl.simulator;

import java.io.Serializable;

/** A GameType instance, which is essentially a tagged union. */
public abstract class GameType implements Serializable {
    public static final class PassAndPlay extends GameType {}
    public static final class PlayerVsAI extends GameType {
        private final Player humanPlayer;
        public PlayerVsAI(Player humanPlayer) {
            this.humanPlayer = humanPlayer;
        }
        public Player getHumanPlayer() { return humanPlayer; }
        public Player getAIPlayer() { return Utils.otherPlayer(humanPlayer); };
    }
    public static final class OnlineMatch extends GameType {
        private final String attackerParticipantId;
        private final String defenderParticipantId;
        public OnlineMatch(String attackerParticipantId, String defenderParticipantId) {
            this.attackerParticipantId = attackerParticipantId;
            this.defenderParticipantId = defenderParticipantId;
        }
        public String getAttackerParticipantId() { return attackerParticipantId; }
        public String getDefenderParticipantId() { return defenderParticipantId; }
    }
}