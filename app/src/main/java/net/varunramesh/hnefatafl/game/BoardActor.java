package net.varunramesh.hnefatafl.game;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;

import net.varunramesh.hnefatafl.simulator.Position;

/**
 * Created by varunramesh on 7/27/15.
 */
public class BoardActor extends Actor implements LayerActor {
    public static final float SQUARE_SIZE = 2048.0f/11.0f;

    private final Texture texture;
    private final int boardSize;

    public BoardActor(HnefataflGame game, int boardSize) {
        this.boardSize = boardSize;

        switch (boardSize) {
            case 11:
                texture = game.getTexture("boards/11.png");
                break;
            case 7:
                texture = game.getTexture("boards/7.png");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported board size.");
        }


        setBounds(getX(), getY(), SQUARE_SIZE*boardSize, SQUARE_SIZE*boardSize);
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        batch.setColor(getColor());
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
    }

    /** Turn a Position object into a Vector2 stage position. */
    public Vector2 toWorldPosition(Position position) {
        return new Vector2(
                SQUARE_SIZE/2 + SQUARE_SIZE*position.getX(),
                SQUARE_SIZE/2 + SQUARE_SIZE*position.getY()
        );
    }

    @Override
    public int getLayer() {
        return 0;
    }
}