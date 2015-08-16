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
import android.widget.TextView;

import com.alertdialogpro.AlertDialogPro;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gc.materialdesign.widgets.Dialog;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.R;
import net.varunramesh.hnefatafl.SavedGame;
import net.varunramesh.hnefatafl.game.HnefataflGame;
import net.varunramesh.hnefatafl.simulator.GameState;
import net.varunramesh.hnefatafl.simulator.GameType;
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
public class PlayerActivity extends AndroidApplication {
    private static final String TAG = "PlayerActivity";

    private static final String EXTRAS_GAMESTATE = "GameState";
    private static final String EXTRAS_MATCH = "TurnBasedMatch";

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
                if(winner == Winner.ATTACKER) title = "The attackers win!";
                else title = "The defenders win!";
            } else if(gameState.getType() instanceof GameType.PlayerVsAI) {
                GameType.PlayerVsAI pvai = (GameType.PlayerVsAI) gameState.getType();
                if(pvai.getHumanPlayer() == winner.toPlayer()) title = "You Have Won the Game!";
                else title = "You Have Lost the Game :(";
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

    public GameState gameState;

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

        // Create a handler for recieving messages from other threads.
        handler = new Handler() {
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
                        throw new UnsupportedOperationException();
                    case MESSAGE_SHOW_WINNER:
                        showWinnerDialog((Winner)msg.obj);
                        break;
                }
            }
        };

        // Specify how to persist the game state.
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

        // Create the game view.
        final HnefataflGame game = new HnefataflGame(gameState, handler);
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

        // Load Match object
        final ImageManager imageManager = ImageManager.create(this);

        final LinearLayout online_match_view = (LinearLayout)findViewById(R.id.online_match_view);

        if (extras.containsKey(EXTRAS_MATCH)) {
            currentPlayer.setVisibility(View.GONE);

            GameType.OnlineMatch gameType = (GameType.OnlineMatch)gameState.getType();

            TurnBasedMatch match = extras.getParcelable(EXTRAS_MATCH);
            Optional<Participant> attacker = Stream.of(match.getParticipants())
                    .filter((Participant p) -> p.getParticipantId().equals(gameType.getAttackerParticipantId()))
                    .findFirst();

            Optional<Participant> defender = Stream.of(match.getParticipants())
                    .filter((Participant p) -> p.getParticipantId().equals(gameType.getDefenderParticipantId()))
                    .findFirst();

            if (attacker.isPresent()) {
                TextView attackername = (TextView)findViewById(R.id.attackername);
                attackername.setText(attacker.get().getDisplayName());

                ImageView attackericon = (ImageView)findViewById(R.id.attackericon);
                imageManager.loadImage(attackericon, attacker.get().getIconImageUri());
            }

            if (defender.isPresent()) {
                TextView defendername = (TextView)findViewById(R.id.defendername);
                defendername.setText(defender.get().getDisplayName());

                ImageView defendericon = (ImageView)findViewById(R.id.defendericon);
                imageManager.loadImage(defendericon, defender.get().getIconImageUri());
            }
        } else {
            online_match_view.setVisibility(View.GONE);
        }

        hideMoveConfirmation();
    }
}
