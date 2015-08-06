package net.varunramesh.hnefatafl.nativeplayer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;

import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Position;

/**
 * Created by Varun on 8/4/2015.
 */
public class GamePiece {
    private final Position position;
    private final Piece.Type type;
    private final GameView gameView;


    public GamePiece(Position position, Piece.Type type, GameView gameView) {
        this.position = position;
        this.type = type;
        this.gameView = gameView;
    }

    public Position getPosition() { return position; }

    public void draw(Matrix matrix, Canvas canvas, Paint paint) {
        Bitmap bitmap = null;
        switch(type) {
            case KING:
                bitmap = gameView.kingBitmap;
                break;
            case ATTACKER:
                bitmap = gameView.attackerBitmap;
                break;
            case DEFENDER:
                bitmap = gameView.defenderBitmap;
                break;
        }
        assert bitmap != null;

        //canvas.save();
        //canvas.translate();
        //matrix.postTranslate();
        //canvas.scale(scale, scale);
        canvas.drawBitmap(bitmap, position.getX() * gameView.getTileSize(), position.getY() * gameView.getTileSize(), paint);
        //canvas.restore();
    }
}
