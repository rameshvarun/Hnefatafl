package net.varunramesh.hnefatafl.game;

import android.os.Handler;
import android.util.Log;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Varun on 7/23/2015.
 */
public class HnefataflGame extends ApplicationAdapter implements EventHandler {
    private final String TAG = "HnefataflGame";

    private final GameState state;
    private final Handler uiHandler;

    private BoardActor boardActor;
    private Stage stage;
    private Camera cam;
    private Texture done;

    private AssetManager manager;

    private final Queue<Integer> messageQueue;
    public void postMessage(int message) {
        messageQueue.add(new Integer(message));
    }

    private final HashMap<String, Texture> textures = new HashMap<>();
    public Texture getTexture(String file) {
        if(!textures.containsKey(file)) {
            textures.put(file, new Texture(file));
        }
        return textures.get(file);
    }

    public HnefataflGame(GameState state, Handler uiHandler) {
        this.state = state;
        this.uiHandler = uiHandler;
        moveState = MoveState.SELECT_MOVE;
        messageQueue = new ConcurrentLinkedQueue<Integer>();
    }

    @Override
    public void MovePiece(Position from, Position to) {
        Log.d(TAG, "Move piece from " + from.toString() + " to " + to.toString() + ".");
        PieceActor actor = getPieceActorAt(from);
        assert actor != null;
        actor.slideTo(to);
    }

    @Override
    public void RemovePiece(Position position) {
        Log.d(TAG, "Removed piece from " + position.toString() + ".");
        PieceActor actor = getPieceActorAt(position);
        assert actor != null;
        pieceActors.remove(actor);
        actor.capture();
    }

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
        manager = new AssetManager();

        // Create game board.
        boardActor = new BoardActor(this);
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

    /** The staged board for a move that hasn't been confirmed yet. */
    private Board stagedBoard;
    private Action stagedAction;

    public void stageAction(Action action) {
        // We can only stage an action from the SELECT_MOVE state.
        assert getMoveState() == MoveState.SELECT_MOVE;

        // Destroy all move selecters.
        clearMoveSelectors();

        // Step forward the state, enacting events.
        stagedAction = action;
        stagedBoard = state.currentBoard().step(stagedAction, this);

        // Transition into the CONFIRM_MOVE state
        moveState = MoveState.CONFIRM_MOVE;
        uiHandler.sendEmptyMessage(PlayerActivity.MESSAGE_SHOW_CONFIRMATION);
    }

    /** Call when in CONFIRM_MOVE state to revert the move */
    private void cancelMove() {
        // In order to cancel a move, we must already have staged a move.
        assert moveState == MoveState.CONFIRM_MOVE;

        // Set board back to original state.
        setBoardConfiguration(state.currentBoard());

        // Transition back to the SELECT_MOVE state.
        moveState = MoveState.SELECT_MOVE;
        uiHandler.sendEmptyMessage(PlayerActivity.MESSAGE_HIDE_CONFIRMATION);
    }

    private void confirmMove() {
        // In order to confirm a move, we must already have staged a move.
        assert moveState == MoveState.CONFIRM_MOVE;

        state.pushBoard(stagedAction, stagedBoard);

        // Transition back to the SELECT_MOVE state.
        moveState = MoveState.SELECT_MOVE;
        uiHandler.sendEmptyMessage(PlayerActivity.MESSAGE_HIDE_CONFIRMATION);
    }

    private List<Actor> moveSelectors = new ArrayList<>();
    private PieceActor selection;

    public List<Actor> getMoveSelectors() { return moveSelectors; }

    public void clearMoveSelectors() {
        selection = null;
        for(Actor actor : moveSelectors) {
            if(actor instanceof SelectionMarker || actor instanceof MoveMarker)
                actor.remove();
        }
        moveSelectors.clear();
    }

    /** Return true if the given PieceActor is the currently selected one - false if otherwise */
    public boolean isSelected(PieceActor piece) {
        return selection == piece;
    }

    public void selectPiece(PieceActor piece) {
        // You can only select a piece during the select move state.
        if(getMoveState() == MoveState.SELECT_MOVE) {
            // Clear move selectors.
            clearMoveSelectors();

            // Set selection.
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

    private final List<PieceActor> pieceActors = new ArrayList<>();

    /** Returns the PieceActor at the given position, or null if there is none. */
    private PieceActor getPieceActorAt(Position boardPosition) {
        for(PieceActor pieceActor : pieceActors) {
            if(pieceActor.getPosition().equals(boardPosition)) return pieceActor;
        }
        return null;
    }

    /** Clear the visual board and add pieces to match the given configuration */
    private void setBoardConfiguration(Board board) {
        // Clear the current actors
        clearMoveSelectors();
        for(PieceActor pieceActor : pieceActors) pieceActor.remove();
        pieceActors.clear();

        // Add in the new actors.
        for(Map.Entry<Position, Piece> piece : board.getPieces()) {
            PieceActor actor = new PieceActor(this, piece.getValue().getType(), piece.getKey());
            stage.addActor(actor);
            pieceActors.add(actor);
        }
    }

    public static final int MESSAGE_CANCEL_MOVE = 1;
    public static final int MESSAGE_CONFIRM_MOVE = 2;

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
        stage.getRoot().getChildren().sort((Actor lhs, Actor rhs) -> {
            int llayer = lhs instanceof LayerActor ? ((LayerActor) lhs).getLayer() : 0;
            int rlayer = rhs instanceof LayerActor ? ((LayerActor) rhs).getLayer() : 0;
            return Integer.valueOf(llayer).compareTo(Integer.valueOf(rlayer));
        });

        stage.draw();

        while(messageQueue.size() > 0) {
            int message = messageQueue.remove().intValue();
            switch (message) {
                case MESSAGE_CANCEL_MOVE:
                    cancelMove();
                    break;
                case MESSAGE_CONFIRM_MOVE:
                    confirmMove();
                    break;
            }
        }
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
