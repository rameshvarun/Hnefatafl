package net.varunramesh.hnefatafl.game;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.varunramesh.hnefatafl.R;
import net.varunramesh.hnefatafl.game.HnefataflGame;
import net.varunramesh.hnefatafl.simulator.GameState;

/**
 * Created by Varun on 7/23/2015.
 */
public class PlayerActivity extends AndroidApplication {
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

    public static final int MESSAGE_SHOW_CONFIRMATION = 1;
    public static final int MESSAGE_HIDE_CONFIRMATION = 2;

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
        JsonElement element = (new Gson()).fromJson(extras.getString("GameState"), JsonElement.class);
        GameState gameState = new GameState(element);

        // Create the game view.
        View gameView = initializeForView(new HnefataflGame(gameState, handler), config);

        // Add the game view to the framelayout
        setContentView(R.layout.game_screen);
        FrameLayout frameLayout = (FrameLayout)findViewById(R.id.frameLayout);
        frameLayout.addView(gameView, 0);

        //
        TextView currentPlayer = (TextView) findViewById(R.id.currentPlayer);
        Typeface tf = Typeface.createFromAsset(getAssets(), "Norse-Bold.otf");
        currentPlayer.setTypeface(tf);

        hideMoveConfirmation();
    }
}
