package net.varunramesh.hnefatafl.game;

import android.os.Handler;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.EventHandler;
import net.varunramesh.hnefatafl.simulator.GameState;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Varun on 7/23/2015.
 */
public class HnefataflGame extends ApplicationAdapter implements EventHandler {
    private final GameState state;
    private final Handler uiHandler;

    private BoardActor boardActor;
    private Stage stage;
    private Camera cam;
    private Texture done;

    public HnefataflGame(GameState state, Handler uiHandler) {
        this.state = state;
        this.uiHandler = uiHandler;
        moveState = MoveState.SELECT_MOVE;
    }

    @Override
    public void MovePiece(Position from, Position to) {}

    @Override
    public void RemovePiece(Position position) {}

    @Override
    public void SetWinner(Player player) {}

    public static enum MoveState {
        SELECT_MOVE,
        CONFIRM_MOVE
    }
    private MoveState moveState;
    public MoveState getMoveState() { return moveState; }

    @Override
    public void create () {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        cam = stage.getCamera();

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

    public Vector2 toWorldPosition(Position position) {
        assert boardActor != null;
        return boardActor.toWorldPosition(position);
    }


    public void stageAction(Action action) {
        assert getMoveState() == MoveState.SELECT_MOVE;
        clearMoveSelectors();
        uiHandler.sendEmptyMessage(PlayerActivity.MESSAGE_SHOW_CONFIRMATION);
        moveState = MoveState.CONFIRM_MOVE;
    }

    private List<Actor> moveSelectors = new ArrayList<>();
    private PieceActor selection;

    public void clearMoveSelectors() {
        selection = null;
        for(Actor actor : moveSelectors) {
            if(actor instanceof SelectionMarker || actor instanceof MoveMarker)
                actor.remove();
        }
        moveSelectors.clear();
    }

    public boolean isSelected(PieceActor piece) {
        return selection == piece;
    }

    public void selectPiece(PieceActor piece) {
        // You can only select a piece during the select move state.
        if(getMoveState() == MoveState.SELECT_MOVE) {
            // Clear move selectors.
            clearMoveSelectors();

            // Set selection
            selection = piece;

            // Create selection marker.
            SelectionMarker selection = new SelectionMarker(this, piece.getPosition());
            moveSelectors.add(selection);
            stage.addActor(selection);

            // Create move markers.
            for (Action action : state.currentBoard().getActions(piece.getPosition())) {
                MoveMarker move = new MoveMarker(this, piece, action);
                moveSelectors.add(move);
                stage.addActor(move);
            }
        }
    }

    private void setBoardConfiguration(Board board) {
        for(Map.Entry<Position, Piece> piece : board.getPieces()) {
            PieceActor actor = new PieceActor(this, piece.getValue().getType(), piece.getKey());
            stage.addActor(actor);
        }
    }

    @Override
    public void render () {
        float aspect = (float)stage.getViewport().getScreenWidth() / stage.getViewport().getScreenHeight();
        Vector2 idealSize = new Vector2(2048, 2048 + 100);
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
