package net.varunramesh.hnefatafl.game;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import net.varunramesh.hnefatafl.game.HnefataflGame;
import net.varunramesh.hnefatafl.simulator.GameState;

/**
 * Created by Varun on 7/23/2015.
 */
public class PlayerActivity extends AndroidApplication {
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        // Load GameState from bundle extras.
        Bundle extras = getIntent().getExtras();
        assert extras.containsKey("GameState");
        JsonElement element = (new Gson()).fromJson(extras.getString("GameState"), JsonElement.class);
        GameState gameState = new GameState(element);

        initialize(new HnefataflGame(gameState), config);
    }
}
