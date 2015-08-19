package net.varunramesh.hnefatafl.game;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.GameHelper;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.simulator.GameState;
import net.varunramesh.hnefatafl.simulator.GameType;
import net.varunramesh.hnefatafl.simulator.Persister;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Winner;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Implementation of a persister, in which turns are sent through the Google Play Games Turn-based API.
 */
final class OnlinePersister implements Persister {
    private final Context context;
    private final String matchId;
    private final GameHelper gameHelper;

    public OnlinePersister(Context context, String matchId, GameHelper gameHelper) {
        this.context = context;
        this.matchId = matchId;
        this.gameHelper = gameHelper;
    }

    @Override
    public void persist(GameState state) {
        Assert.assertNotNull("The GameHelper should be present.", gameHelper);
        GameType.OnlineMatch gameType = (GameType.OnlineMatch) state.getType();

        if (state.currentBoard().isOver()) {
            ParticipantResult attackerResult;
            ParticipantResult defenderResult;
            Winner winner = state.currentBoard().getWinner();

            if (winner == Winner.DRAW) {
                attackerResult = new ParticipantResult(gameType.getAttackerParticipantId(), ParticipantResult.MATCH_RESULT_TIE,
                        ParticipantResult.PLACING_UNINITIALIZED);
                defenderResult = new ParticipantResult(gameType.getDefenderParticipantId(), ParticipantResult.MATCH_RESULT_TIE,
                        ParticipantResult.PLACING_UNINITIALIZED);
            } else {
                attackerResult = new ParticipantResult(gameType.getAttackerParticipantId(),
                        (winner == Winner.ATTACKER) ? ParticipantResult.MATCH_RESULT_WIN : ParticipantResult.MATCH_RESULT_LOSS,
                        ParticipantResult.PLACING_UNINITIALIZED);
                defenderResult = new ParticipantResult(gameType.getDefenderParticipantId(),
                        (winner == Winner.DEFENDER) ? ParticipantResult.MATCH_RESULT_WIN : ParticipantResult.MATCH_RESULT_LOSS,
                        ParticipantResult.PLACING_UNINITIALIZED);
            }

            Games.TurnBasedMultiplayer.finishMatch(gameHelper.getApiClient(),
                    matchId, SerializationUtils.serialize(state),
                    attackerResult, defenderResult);
        } else {
            String nextParticipant = (state.currentBoard().getCurrentPlayer() == Player.ATTACKER) ?
                    gameType.getAttackerParticipantId() : gameType.getDefenderParticipantId();

            Games.TurnBasedMultiplayer.takeTurn(gameHelper.getApiClient(),
                    matchId, SerializationUtils.serialize(state),
                    nextParticipant).setResultCallback((TurnBasedMultiplayer.UpdateMatchResult updateMatchResult) -> {
                Toast.makeText(context, "Turn Sent", Toast.LENGTH_SHORT).show();
            });
        }
    }
}