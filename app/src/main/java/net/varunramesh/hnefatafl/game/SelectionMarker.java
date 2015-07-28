package net.varunramesh.hnefatafl.game;

import android.util.Log;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import net.varunramesh.hnefatafl.simulator.Position;

/**
 * Created by varunramesh on 7/27/15.
 */
public class SelectionMarker extends Actor implements LayerActor {
    private static final String TAG = "SelectionMarker";
    private static Texture texture;

    private final TextureRegion region;
    private final Position position;

    public SelectionMarker(HnefataflGame game, Position position){
        if(texture == null) texture = new Texture("selectionmarker.png");
        region = new TextureRegion(texture);
        this.position = position;

        float scale = (2048.0f/11.0f)/64.0f;

        setWidth(texture.getWidth()); setHeight(texture.getHeight());
        setScaleX(scale); setScaleY(scale);
        setOriginX(texture.getWidth() / 2.0f); setOriginY(texture.getHeight() / 2.0f);

        Vector2 worldPosition = game.toWorldPosition(position);
        setX(worldPosition.x - 32.0f); setY(worldPosition.y - 32.0f);
    }

    @Override
    public int getLayer() {
        return 1;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        batch.setColor(getColor());
        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }

    public Position getPosition() {
        return position;
    }
}
