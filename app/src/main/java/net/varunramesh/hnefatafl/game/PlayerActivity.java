package net.varunramesh.hnefatafl.game;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.gc.materialdesign.widgets.Dialog;
import com.gc.materialdesign.widgets.SnackBar;

import net.varunramesh.hnefatafl.R;
import net.varunramesh.hnefatafl.game.HnefataflGame;
import net.varunramesh.hnefatafl.game.livereload.AssetServer;
import net.varunramesh.hnefatafl.simulator.GameState;
import net.varunramesh.hnefatafl.simulator.Player;

import java.io.IOException;

/**
 * Created by Varun on 7/23/2015.
 */
public class PlayerActivity extends AndroidApplication {
    private static final boolean DEBUG = true;
    private final AssetServer assetServer;

    public PlayerActivity() {
        super();

        if(DEBUG) {
            assetServer  = new AssetServer();
        } else {
            assetServer = null;
        }
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

    public void showWinnerDialog() {
        AlertDialogPro.Builder builder = new AlertDialogPro.Builder(this);
        builder.setTitle("You Have Lost or Won The Game.")
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

        // Create a handler for recieving messages from other threads.
        Handler handler = new Handler() {
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
                        showWinnerDialog();
                }
            }
        };

        // Make this activity fullscreen.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        // Load GameState from bundle extras.
        Bundle extras = getIntent().getExtras();
        assert extras.containsKey("GameState");
        GameState gameState = (GameState)(extras.getSerializable("GameState"));

        // Create the game view.
        final HnefataflGame game = new HnefataflGame(gameState, handler, assetServer);
        View gameView = initializeForView(game, config);

        // Add the game view to the framelayout
        setContentView(R.layout.game_screen);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        frameLayout.addView(gameView, 0);

        // Install Norse font
        TextView currentPlayer = (TextView) findViewById(R.id.currentPlayer);
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

        hideMoveConfirmation();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(DEBUG) {
            try {
                assetServer.start(2*60*1000);
                Dialog dialog = new Dialog(this, "Asset server started.", "Server started at " + assetServer.getURL());
                dialog.show();
            } catch(IOException e) {
                e.printStackTrace();
                new SnackBar(this, "Could not start asset server.", null, null).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(DEBUG) {
            assetServer.stop();
        }
    }
}
