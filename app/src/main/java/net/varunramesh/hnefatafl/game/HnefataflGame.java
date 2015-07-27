package net.varunramesh.hnefatafl.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.GameState;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Position;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by Varun on 7/23/2015.
 */
public class HnefataflGame extends ApplicationAdapter {
    private Stage stage;
    private BoardActor boardActor;
    private Camera cam;

    private final GameState state;

    public HnefataflGame(GameState state) {
        this.state = state;
    }

    public class BoardActor extends Actor implements LayerActor {
        private static final float SQUARE_SIZE = 2048.0f/11.0f;

        Texture texture = new Texture("gameboard.png");
        public BoardActor(){
            setBounds(getX(), getY(), texture.getWidth(), texture.getHeight());
            setZIndex(0);
        }

        @Override
        public void draw (Batch batch, float parentAlpha) {
            batch.setColor(getColor());
            batch.draw(texture, getX(), getY());
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

    public interface LayerActor {
        int getLayer();
    }

    public class PieceActor extends Actor implements LayerActor {
        TextureRegion region;
        Texture texture;
        Position boardPosition;

        public PieceActor(Piece.Type type, Position boardPosition, Vector2 worldPosition){
            switch(type) {
                case KING:
                    texture = new Texture("king.png");
                    break;
                case DEFENDER:
                    texture = new Texture("defender.png");
                    break;
                case ATTACKER:
                    texture = new Texture("attacker.png");
                    break;
            }

            region = new TextureRegion(texture);

            this.boardPosition = boardPosition;

            setWidth(texture.getWidth());
            setHeight(texture.getHeight());

            setX(worldPosition.x - getWidth() / 2.0f);
            setY(worldPosition.y - 87.0f);

            setZIndex(11 - boardPosition.getY());
        }

        @Override
        public void draw (Batch batch, float parentAlpha) {
            batch.setColor(getColor());
            batch.draw(region, getX(), getY(), getOriginX(), getOriginY(), getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
        }

        @Override
        public int getLayer() {
            return 11 - boardPosition.getY();
        }
    }

    @Override
    public void create () {
        stage = new Stage(new ScreenViewport());
        cam = stage.getCamera();
        Gdx.input.setInputProcessor(stage);

        // Create game board.
        boardActor = new BoardActor();
        stage.addActor(boardActor);

        // If this is the first move, then simply display the board.
        if(state.isFirstMove()) {
            setBoardConfiguration(state.currentBoard());
        } else {
            // Otherwise, replay the last move.
            throw new UnsupportedOperationException();
        }

        cam.position.set(boardActor.getWidth() / 2, boardActor.getHeight() / 2, cam.position.z);
    }

    private void setBoardConfiguration(Board board) {
        // TODO: Clear board of piece actor

        for(Map.Entry<Position, Piece> pieces : board.getPieces()) {
            PieceActor actor = new PieceActor(
                    pieces.getValue().getType(),
                    pieces.getKey(),
                    this.boardActor.toWorldPosition(pieces.getKey())
            );
            stage.addActor(actor);
        }
    }

    @Override
    public void render () {
        float aspect = (float)stage.getViewport().getScreenWidth() / stage.getViewport().getScreenHeight();
        Vector2 idealSize = new Vector2(2048, 2048);
        if(idealSize.x > idealSize.y*aspect)
            stage.getViewport().setWorldSize(idealSize.x, idealSize.x / aspect);
        else
            stage.getViewport().setWorldSize(idealSize.y*aspect, idealSize.y);
        stage.getViewport().apply();


        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());

        /* Sort actors according to layer */
        stage.getRoot().getChildren().sort(new Comparator<Actor>() {
            @Override
            public int compare(Actor lhs, Actor rhs) {
                int llayer = lhs instanceof LayerActor ? ((LayerActor) lhs).getLayer() : 0;
                int rlayer = rhs instanceof LayerActor ? ((LayerActor) rhs).getLayer() : 0;
                return Integer.valueOf(llayer).compareTo(Integer.valueOf(rlayer));
            }
        });

        stage.draw();
    }

    @Override
    public void resize (int width, int height) {
        stage.getViewport().setScreenBounds(0, 0, width, height);
        stage.getViewport().apply();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

}
