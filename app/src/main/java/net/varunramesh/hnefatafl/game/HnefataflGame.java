package net.varunramesh.hnefatafl.game;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.android.gms.wearable.Asset;

import junit.framework.Assert;

import net.varunramesh.hnefatafl.R;
import net.varunramesh.hnefatafl.ai.AIStrategy;
import net.varunramesh.hnefatafl.ai.MinimaxStrategy;
import net.varunramesh.hnefatafl.ai.RandomStrategy;
import net.varunramesh.hnefatafl.simulator.Action;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.EventHandler;
import net.varunramesh.hnefatafl.simulator.GameState;
import net.varunramesh.hnefatafl.simulator.GameType;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Player;
import net.varunramesh.hnefatafl.simulator.Position;
import net.varunramesh.hnefatafl.simulator.Winner;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

/**
 * Created by Varun on 7/23/2015.
 */
public class HnefataflGame extends ApplicationAdapter implements EventHandler {
    private final String TAG = "HnefataflGame";

    private final GameState state;
    private final Handler uiHandler;
    private final Context context;

    private BoardActor boardActor;
    private Stage stage;
    private OrthographicCamera cam;
    private GestureAdapter controller;

    private HashMap<String, MediaPlayer> sounds;

    private final HashMap<String, Texture> textures = new HashMap<>();
    public synchronized Texture getTexture(String textureFile) {
        if(!textures.containsKey(textureFile))
            textures.put(textureFile, new Texture(textureFile));
        return textures.get(textureFile);
    }

    private final Queue<Integer> messageQueue;
    public void postMessage(int message) {
        messageQueue.add(new Integer(message));
    }

    public HnefataflGame(Context context, GameState state, Handler uiHandler) {
        this.state = state;
        this.uiHandler = uiHandler;
        messageQueue = new ConcurrentLinkedQueue<Integer>();
        this.context = context;

        /** Sound effects **/
        sounds = new HashMap<>();
        sounds.put("capture", MediaPlayer.create(context, R.raw.swish));
        sounds.get("capture").setVolume((float) 0.5, (float) 0.5);

    }

    public void TakeAIMove() {
        Assert.assertEquals("We should be in the AI_MOVE state before taking an AI move.", moveState, MoveState.AI_MOVE);

        final Player aiPlayer = ((GameType.PlayerVsAI) state.getType()).getAIPlayer();
        final AIStrategy strategy = new MinimaxStrategy(aiPlayer);

        final FutureTask<Action> aiTask = new FutureTask<Action>(() -> {
            // Decide the next move
            Action action = strategy.decide(state.currentBoard(), state.currentBoard().getActions());
            Assert.assertNotNull("The AI should not return a null action.", action);
            Log.d(TAG, "AI wants to move " + action.toString());
            return action;
        });
        new Thread(aiTask).start();

        Utils.schedule(() -> {
            Action action = null;
            try {
                action = aiTask.get();

                // Step forward the state, enacting events.
                Board newBoard = state.currentBoard().step(action, this);
                state.pushBoard(action, newBoard);

                if(newBoard.isOver()) {
                    moveState = MoveState.WINNER_DETERMINED;
                    showWinner();
                } else {
                    // Let the player move again, in the SELECT_MOVE state
                    moveState = MoveState.SELECT_MOVE;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }, 1.0f);
    }

    /** Show Winner Dialog **/
    public void showWinner() {
        Assert.assertTrue("The winner has been determined", winner != Winner.UNDETERMINED);
        uiHandler.sendMessage(uiHandler.obtainMessage(PlayerActivity.MESSAGE_SHOW_WINNER, winner));
    }

    @Override
    public void movePiece(Position from, Position to) {
        Log.d(TAG, "Move piece from " + from.toString() + " to " + to.toString() + ".");
        PieceActor actor = getPieceActorAt(from);
        assert actor != null;
        actor.slideTo(to);
    }

    @Override
    public void removePiece(Position position) {
        Log.d(TAG, "Removed piece from " + position.toString() + ".");
        PieceActor actor = getPieceActorAt(position);
        assert actor != null;
        pieceActors.remove(actor);
        actor.capture();

        sounds.get("capture").start();
    }

    private Winner winner = Winner.UNDETERMINED;

    @Override
    public void setWinner(Winner winner) {
        Log.d(TAG, winner + " won the game.");
        this.winner = winner;
    }

    public static enum MoveState {
        SELECT_MOVE,
        CONFIRM_MOVE,
        AI_MOVE,
        WINNER_DETERMINED
    }
    private MoveState moveState;
    public MoveState getMoveState() { return moveState; }

    @Override
    public void create () {

        cam = new OrthographicCamera();
        stage = new Stage(new ScreenViewport(cam));
        Gdx.input.setInputProcessor(stage);

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

        /** Pinch-zoom functionality **/
        controller = new GestureAdapter(){

            float initialScale = 1;

            @Override
            public boolean touchDown(float x, float y, int pointer, int button){
                initialScale = cam.zoom;
                return false;
            }

            @Override
            public boolean zoom(float originalDistance, float currentDistance){
                float ratio = originalDistance / currentDistance;
                cam.zoom = initialScale * ratio;
                return false;
            }

            @Override
            public boolean pinch(Vector2 initialFirstPointer, Vector2 initialSecondPointer,
                                 Vector2 firstPointer, Vector2 secondPointer){
                return false;
            }
        };


        if (state.getType() instanceof GameType.PassAndPlay) {
            moveState = MoveState.SELECT_MOVE;
        } else if (state.getType() instanceof GameType.PlayerVsAI) {
            GameType.PlayerVsAI pvaState = (GameType.PlayerVsAI) state.getType();

            Log.d(TAG, state.currentBoard().getCurrentPlayer().toString());
            Log.d(TAG, pvaState.getHumanPlayer().toString());
            if(state.currentBoard().getCurrentPlayer().equals(pvaState.getHumanPlayer())) {
                moveState = MoveState.SELECT_MOVE;
            } else {
                moveState = MoveState.AI_MOVE;
                TakeAIMove();
            }
        }
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

        if(stagedBoard.isOver()) {
            moveState = MoveState.WINNER_DETERMINED;
            showWinner();
        } else {
            // Transition into the CONFIRM_MOVE state
            moveState = MoveState.CONFIRM_MOVE;
            uiHandler.sendEmptyMessage(PlayerActivity.MESSAGE_SHOW_CONFIRMATION);
        }
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

        if (state.getType() instanceof GameType.PassAndPlay) {
            // Transition back to the SELECT_MOVE state.
            moveState = MoveState.SELECT_MOVE;
        } else if (state.getType() instanceof GameType.PlayerVsAI) {
            moveState = MoveState.AI_MOVE;
            TakeAIMove();
        }

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
            PieceActor actor = new PieceActor(this, piece.getValue(), piece.getKey());
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
