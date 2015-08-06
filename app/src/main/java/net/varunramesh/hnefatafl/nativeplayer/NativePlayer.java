package net.varunramesh.hnefatafl.nativeplayer;

import android.annotation.TargetApi;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.varunramesh.hnefatafl.R;
import net.varunramesh.hnefatafl.simulator.GameState;

import java.lang.reflect.Method;

/**
 * Created by Varun on 8/3/2015.
 */
public class NativePlayer extends BaseGameActivity {
    public static final String TAG = "NativePlayer";
    public GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        hideStatusBar(true);
        useImmersiveMode(true);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_board);

        // Set TextViews to Norse Font
        TextView text = (TextView) findViewById(R.id.textView);
        Typeface tf = Typeface.createFromAsset(getAssets(), "Norse-Bold.otf");
        text.setTypeface(tf);

        // Load GameState from bundle extras.
        //Bundle extras = getIntent().getExtras();
        //assert extras.containsKey("GameState");
        //JsonElement element = (new Gson()).fromJson(extras.getString("GameState"), JsonElement.class);
        GameState gameState = new GameState(GameState.GameType.PASS_AND_PLAY);

        gameView = (GameView)findViewById(R.id.gameview);
        gameView.setBoardConfiguration(gameState.currentBoard());
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }

    protected void createWakeLock (boolean use) {
        if (use) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected void hideStatusBar (boolean hide) {
        if (!hide || getVersion() < 11) return;

        View rootView = getWindow().getDecorView();

        try {
            Method m = View.class.getMethod("setSystemUiVisibility", int.class);
            if (getVersion() <= 13) m.invoke(rootView, 0x0);
            m.invoke(rootView, 0x1);
        } catch (Exception e) {
            Log.d(TAG, "Can't hide status bar", e);
        }
    }

    @TargetApi(19)
    public void useImmersiveMode (boolean use) {
        if (!use || getVersion() < Build.VERSION_CODES.KITKAT) return;

        View view = getWindow().getDecorView();
        try {
            Method m = View.class.getMethod("setSystemUiVisibility", int.class);
            int code = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            m.invoke(view, code);
        } catch (Exception e) {
            Log.d(TAG, "Can't set immersive mode", e);
        }
    }

    public int getVersion () {
        return android.os.Build.VERSION.SDK_INT;
    }
}
