package net.varunramesh.hnefatafl.nativeplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import net.varunramesh.hnefatafl.R;
import net.varunramesh.hnefatafl.simulator.Board;
import net.varunramesh.hnefatafl.simulator.Piece;
import net.varunramesh.hnefatafl.simulator.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by Varun on 8/3/2015.
 */
class GameView extends View {
    public final Bitmap boardBitmap;
    public final Bitmap kingBitmap;
    public final Bitmap attackerBitmap;
    public final Bitmap defenderBitmap;

    private final ArrayList<GamePiece> pieces = new ArrayList<>();;
    private final Paint paint = new Paint();
    private final Matrix matrix = new Matrix();

    public static Matrix fromTranslateScale(PointF translate, PointF scale) {
        final Matrix matrix = new Matrix();
        matrix.postScale(scale.x, scale.y);
        matrix.postTranslate(translate.x, translate.y);
        return matrix;
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        boardBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameboard);
        kingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.king);
        attackerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.attacker);
        defenderBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.defender);
        setBoardConfiguration(new Board());
    }

    private final int HEIGHT_BORDER = 100;

    public float getAspectRatio() {
        return (1024.0f + HEIGHT_BORDER) / 1024.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);

        float widthScale = (float)canvas.getWidth() /  boardBitmap.getWidth();
        float heightScale = (float)canvas.getHeight() /  (boardBitmap.getHeight() + HEIGHT_BORDER);
        float currRectToDrwScale = Math.min(widthScale, heightScale);

        //final Matrix matrix = canvas.getMatrix();
        //canvas.save();

        canvas.setMatrix(new Matrix());

        //matrix.postScale(currRectToDrwScale, currRectToDrwScale);
        //matrix.postTranslate(0.0f, HEIGHT_BORDER);
        /*canvas.save();
        canvas.translate();
        canvas.scale();
        canvas.restore();*/

        //canvas.getMatrix().preScale(currRectToDrwScale, currRectToDrwScale);

        canvas.drawBitmap(boardBitmap, 0, 0, paint);

        // Sort collections by their Y position.
        Collections.sort(pieces, new Comparator<GamePiece>() {
            @Override
            public int compare(GamePiece lhs, GamePiece rhs) {
                return Integer.valueOf(lhs.getPosition().getY()).compareTo(Integer.valueOf(rhs.getPosition().getY()));
            }
        });

        for(GamePiece piece : pieces) {
            piece.draw(matrix, canvas, paint);
        }

        //canvas.restore();
    }

    public void setBoardConfiguration(Board board) {
        pieces.clear();
        for(Map.Entry<Position, Piece> piece : board.getPieces()) {
            pieces.add(new GamePiece(
                    piece.getKey(), piece.getValue().getType(), this));
        }
        invalidate(); // Redraw board.
    }

    public float getTileSize() {
        return 1024.0f / 11.0f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int newWidth = getMeasuredWidth();
        int newHeight = (int) (newWidth * getAspectRatio());

        setMeasuredDimension(newWidth, newHeight);
    }
}