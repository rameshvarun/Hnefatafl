package net.varunramesh.hnefatafl;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Created by Varun on 7/23/2015.
 */
public class HnefataflGame extends ApplicationAdapter {
    private Stage stage;
    private BoardActor board;
    private Camera cam;

    public class BoardActor extends Actor {
        Texture texture = new Texture("board.png");
        public BoardActor(){
            setBounds(getX(), getY(), texture.getWidth(), texture.getHeight());
        }

        @Override
        public void draw (Batch batch, float parentAlpha) {
            batch.setColor(getColor());
            batch.draw(texture, getX(), getY());
        }
    }

    @Override
    public void create () {
        stage = new Stage(new ScreenViewport());
        cam = stage.getCamera();
        Gdx.input.setInputProcessor(stage);
        board = new BoardActor();
        stage.addActor(board);

        cam.position.set(board.getWidth() / 2, board.getHeight() / 2, cam.position.z);


    }

    @Override
    public void render () {
        float aspect = (float)stage.getViewport().getScreenWidth() / stage.getViewport().getScreenHeight();
        Vector2 idealSize = new Vector2(1100, 850);
        if(idealSize.x > idealSize.y*aspect)
            stage.getViewport().setWorldSize(idealSize.x, idealSize.x / aspect);
        else
            stage.getViewport().setWorldSize(idealSize.y*aspect, idealSize.y);
        stage.getViewport().apply();


        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(Gdx.graphics.getDeltaTime());
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
