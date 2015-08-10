package net.varunramesh.hnefatafl.game.livereload;

import com.badlogic.gdx.graphics.Texture;

/**
 * Created by Varun on 8/9/2015.
 */
public interface AssetManager {
    public Texture getTexture(String textureFile);
    public void update();
}
