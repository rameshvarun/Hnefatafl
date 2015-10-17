package net.varunramesh.hnefatafl.game;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alertdialogpro.AlertDialogPro;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.gc.materialdesign.widgets.Dialog;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.ParticipantResult;
import com.google.android.gms.games.multiplayer.turnbased.OnTurnBasedMatchUpdateReceivedListener;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMultiplayer;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.example.games.basegameutils.GameHelper;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.R;
import net.varunramesh.hnefatafl.SavedGame;
import net.varunramesh.hnefatafl.game.HnefataflGame;
import net.varunramesh.hnefatafl.simulator.GameState;
import net.varunramesh.hnefatafl.simulator.GameType;
import net.varunramesh.hnefatafl.simulator.Persister;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Winner;

import org.apache.commons.lang3.SerializationUtils;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import io.realm.Realm;

/**
 * Created by Varun on 7/23/2015.
 */
public class PlayerActivity extends AndroidApplication implements GameHelper.GameHelperListener,
        OnTurnBasedMatchUpdateReceivedListener {
    private static final String TAG = "PlayerActivity";

    private static final String EXTRAS_GAMESTATE = "GameState";
    private static final String EXTRAS_MATCH = "TurnBasedMatch";

    // The game helper object. We can't inherit BaseGameActivity.
    private Optional<GameHelper> gameHelper = Optional.empty();

    private Optional<TurnBasedMatch> match;
    private GameState gameState;
    private HnefataflGame game;

    /** Create an intent that will launch the Player for a given online match. */
    public static Intent createIntent(Context context, TurnBasedMatch match) {
        Intent intent = new Intent(context, PlayerActivity.class);
        GameState gameState = SerializationUtils.deserialize(match.getData());
        intent.putExtra(EXTRAS_GAMESTATE, gameState);
        intent.putExtra(EXTRAS_MATCH, match);
        return intent;
    }

    /** Create an intent that will launch the Player for a local game (AI or Pass & Play) */
    public static Intent createIntent(Context context, GameState gameState) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRAS_GAMESTATE, gameState);
        return intent;
    }

    private Animation bottomUp;
    public void showMoveConfirmButtons() {
        Log.d("PlayerActivity", "Showing confirmation buttons...");

        // Load in Animation Resources
        if(bottomUp == null)
            bottomUp  = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);

        LinearLayout move_confirm_view = (LinearLayout)findViewById(R.id.move_confirm_view);
        move_confirm_view.startAnimation(bottomUp);

        ImageView ok_button = (ImageView)findViewById(R.id.ok_button);
    }

    private Animation bottomDown;
    public void hideMoveConfirmation() {
        // Load in Animation Resources
        if(bottomDown == null)
            bottomDown = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_down);

        LinearLayout move_confirm_view = (LinearLayout)findViewById(R.id.move_confirm_view);
        move_confirm_view.startAnimation(bottomDown);
    }

    private void showWinnerDialog(Winner winner) {
        Assert.assertTrue("The winner has been determined", winner != Winner.UNDETERMINED);

        String title = "";
        if (winner == Winner.DRAW) title = "Tie Game!";
        else {
            if(gameState.getType() instanceof GameType.PassAndPlay) {
                if (winner == Winner.ATTACKER) title = "The attackers win!";
                else title = "The defenders win!";
            } else if(gameState.getType() instanceof GameType.PlayerVsAI) {
                GameType.PlayerVsAI pvai = (GameType.PlayerVsAI) gameState.getType();
                if (pvai.getHumanPlayer() == winner.toPlayer()) title = "You Have Won the Game!";
                else title = "You Have Lost the Game :(";
            } else if (gameState.getType() instanceof GameType.OnlineMatch) {
                GameType.OnlineMatch gameType = (GameType.OnlineMatch) gameState.getType();

                String myPlayerId = Games.Players.getCurrentPlayerId(gameHelper.get().getApiClient());
                String myParticipantId = match.get().getParticipantId(myPlayerId);

                boolean won = (winner.toPlayer() == Player.ATTACKER &&
                                myParticipantId.equals(gameType.getAttackerParticipantId()))
                        || (winner.toPlayer() == Player.DEFENDER &&
                                myParticipantId.equals(gameType.getDefenderParticipantId()));

                if (won) title = "You Have Won the Game!";
                else title = "You Have Lost the Game!";
            } else {
                throw new UnsupportedOperationException("Unknown GameType: " + gameState.getType().getClass().getName());
            }
        }

        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(this);
        builder.setTitle(title)
                .setPositiveButton("Return to Menu", (DialogInterface sideDialog, int which) -> {
                    NavUtils.navigateUpFromSameTask(this);
                }).show();
    }

    public static final int MESSAGE_SHOW_CONFIRMATION = 1;
    public static final int MESSAGE_HIDE_CONFIRMATION = 2;
    public static final int MESSAGE_UPDATE_CURRENT_PLAYER = 3;
    public static final int MESSAGE_SHOW_WINNER = 4;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity activity = this;

        // Make this activity fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        // Load GameState from bundle extras.
        Bundle extras = getIntent().getExtras();
        Assert.assertTrue(extras.containsKey("GameState"));
        gameState = (GameState)(extras.getSerializable("GameState"));

        // Load online match from extras.
        match = Optional.ofNullable(extras.getParcelable(EXTRAS_MATCH));

        // Create a handler for recieving messages from other threads.
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case MESSAGE_SHOW_CONFIRMATION:
                        showMoveConfirmButtons();
                        break;
                    case MESSAGE_HIDE_CONFIRMATION:
                        hideMoveConfirmation();
                        break;
                    case MESSAGE_UPDATE_CURRENT_PLAYER:
                        updateCurrentPlayer((Player) msg.obj);
                        break;
                    case MESSAGE_SHOW_WINNER:
                        showWinnerDialog((Winner)msg.obj);
                        break;
                }
            }
        };

        // Setup game helper if this is an online match.
        if(match.isPresent()) {
            gameHelper = Optional.of(new GameHelper(this, BaseGameActivity.CLIENT_GAMES));
            gameHelper.get().enableDebugLog(true);
            gameHelper.get().setup(this);
        }

        // Specify how to persist the game state.
        if(match.isPresent()) {
            gameState.setPersister(new OnlinePersister(this, match.get().getMatchId(), gameHelper.get()));
        } else {
            gameState.setPersister((GameState state) -> {
                // Ream IO instance
                Realm realm = Realm.getInstance(activity);

                Log.d(TAG, "Persisting game state: " + state.getUUID());

                realm.beginTransaction();
                SavedGame game = realm.where(SavedGame.class).equalTo("id", state.getUUID().toString()).findFirst();
                if (game == null) {
                    game = realm.createObject(SavedGame.class);
                    game.setId(state.getUUID().toString());
                    game.setCreatedDate(state.getCreatedDate());
                    game.setGameType(state.getType());
                    Log.d(TAG, "Existing state not found. Creating new one.");
                }

                game.setLastMoveDate(state.getLastMoveDate());

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    ObjectOutput out = new ObjectOutputStream(bos);
                    out.writeObject(state);
                    game.setData(bos.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    realm.commitTransaction();
                }
            });
        }

        // Create the game view.
        game = new HnefataflGame(gameState, handler, match);
        View gameView = initializeForView(game, config);

        // Add the game view to the framelayout
        setContentView(R.layout.game_screen);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        frameLayout.addView(gameView, 0);

        // Install Norse font
        final TextView currentPlayer = (TextView) findViewById(R.id.currentPlayer);
        Typeface tf = Typeface.createFromAsset(getAssets(), "Norse-Bold.otf");
        currentPlayer.setTypeface(tf);

        // Setup Cancel / Confirm Handlers
        final ImageView cancel_button = (ImageView)findViewById(R.id.cancel_button);
        cancel_button.setOnClickListener((View v) -> {
            game.postMessage(HnefataflGame.MESSAGE_CANCEL_MOVE);
        });
        final ImageView ok_button = (ImageView)findViewById(R.id.ok_button);
        ok_button.setOnClickListener((View v) -> {
            game.postMessage(HnefataflGame.MESSAGE_CONFIRM_MOVE);
        });


        final ImageManager imageManager = ImageManager.create(this);
        final LinearLayout online_match_view = (LinearLayout)findViewById(R.id.online_match_view);

        // Check if this is an online game
        if (match.isPresent()) {
            currentPlayer.setVisibility(View.GONE);

            GameType.OnlineMatch gameType = (GameType.OnlineMatch)gameState.getType();

            Optional<Participant> attacker = Stream.of(match.get().getParticipants())
                    .filter((Participant p) -> p.getParticipantId().equals(gameType.getAttackerParticipantId()))
                    .findFirst();

            Optional<Participant> defender = Stream.of(match.get().getParticipants())
                    .filter((Participant p) -> p.getParticipantId().equals(gameType.getDefenderParticipantId()))
                    .findFirst();

            // Set the attacker name and icon.
            if (attacker.isPresent()) {
                TextView attackername = (TextView)findViewById(R.id.attackername);
                attackername.setText(attacker.get().getDisplayName());

                ImageView attackericon = (ImageView)findViewById(R.id.attackericon);
                imageManager.loadImage(attackericon, attacker.get().getIconImageUri());
            }

            // Set the defender name and icon.
            if (defender.isPresent()) {
                TextView defendername = (TextView)findViewById(R.id.defendername);
                defendername.setText(defender.get().getDisplayName());

                ImageView defendericon = (ImageView)findViewById(R.id.defendericon);
                imageManager.loadImage(defendericon, defender.get().getIconImageUri());
            }
        } else {
            // This is a local game, so hide the online match view.
            online_match_view.setVisibility(View.GONE);
        }

        // Select the current player
        updateCurrentPlayer(gameState.currentBoard().getCurrentPlayer());

        hideMoveConfirmation();
    }

    /** Update the UI indicators for the current player. */
    void updateCurrentPlayer(Player player) {
        final LinearLayout attackercard = (LinearLayout)findViewById(R.id.attacker_card);
        final LinearLayout defendercard = (LinearLayout)findViewById(R.id.defender_card);
        final TextView currentPlayer = (TextView) findViewById(R.id.currentPlayer);

        if(player == Player.ATTACKER) {
            attackercard.setBackgroundColor(getResources().getColor(R.color.current_player));
            defendercard.setBackgroundColor(getResources().getColor(R.color.transparent));
            currentPlayer.setText("Attacker's Turn");
        } else {
            attackercard.setBackgroundColor(getResources().getColor(R.color.transparent));
            defendercard.setBackgroundColor(getResources().getColor(R.color.current_player));
            currentPlayer.setText("Defender's Turn");
        }

        final ProgressBarCircularIndeterminate ailoading =
                (ProgressBarCircularIndeterminate)findViewById(R.id.ailoading);
        if(gameState.getType() instanceof GameType.PlayerVsAI) {
            GameType.PlayerVsAI gameType = (GameType.PlayerVsAI) gameState.getType();
            if (gameType.getAIPlayer().equals(player)) {
                ailoading.setVisibility(View.VISIBLE);
            } else {
                ailoading.setVisibility(View.GONE);
            }
        } else {
            ailoading.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSignInFailed() {
        Log.e(TAG, "Sign in has failed.");
        Toast.makeText(this, "Failed to sign into Google Play Games Services.", Toast.LENGTH_SHORT).show();
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public void onSignInSucceeded() {
        Log.d(TAG, "Sign in has succeed.");

        // Subscribe to updates in an online match.
        if(match.isPresent() && gameHelper.isPresent() && gameHelper.get().isSignedIn()) {
            Assert.assertTrue("Game Helper is present.", gameHelper.isPresent());
            Games.TurnBasedMultiplayer.registerMatchUpdateListener(gameHelper.get().getApiClient(), this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(gameHelper.isPresent())
            gameHelper.get().onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(gameHelper.isPresent())
            gameHelper.get().onStop();
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        if(gameHelper.isPresent())
            gameHelper.get().onActivityResult(request, response, data);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Subscribe to updates in an online match
        if(match.isPresent() && gameHelper.isPresent() && gameHelper.get().isSignedIn()) {
            Assert.assertTrue("Game Helper is present.", gameHelper.isPresent());
            Games.TurnBasedMultiplayer.registerMatchUpdateListener(gameHelper.get().getApiClient(), this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Subscribe to updates in an online match
        if(match.isPresent()) {
            Assert.assertTrue("Game Helper is present.", gameHelper.isPresent());
            Games.TurnBasedMultiplayer.unregisterMatchUpdateListener(gameHelper.get().getApiClient());
        }
    }

    /** Invoked when a new update to a match arrives **/
    @Override
    public void onTurnBasedMatchReceived(TurnBasedMatch turnBasedMatch) {
        GameState gameState = SerializationUtils.deserialize(turnBasedMatch.getData());
        gameState.setPersister(new OnlinePersister(this, turnBasedMatch.getMatchId(), gameHelper.get()));
        game.updateMatch(turnBasedMatch, gameState);
    }

    /** Invoked when a match has been removed from the local device **/
    @Override
    public void onTurnBasedMatchRemoved(String s) {
        // TODO: Unclear what to do here.
    }
}
