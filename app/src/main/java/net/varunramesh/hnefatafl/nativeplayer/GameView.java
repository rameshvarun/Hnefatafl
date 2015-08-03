package net.varunramesh.hnefatafl.nativeplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import net.varunramesh.hnefatafl.R;

/**
 * Created by Varun on 8/3/2015.
 */
class GameView extends View {
    private final Bitmap boardBitmap;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //boardDrawable = context.getResources().getDrawable(R.drawable.gameboard);
        boardBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.gameboard);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        float widthScale = (float)canvas.getWidth() /  boardBitmap.getWidth();
        float heightScale = (float)canvas.getHeight() /  boardBitmap.getHeight();
        float currRectToDrwScale = Math.min(widthScale, heightScale);

        final Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(currRectToDrwScale, currRectToDrwScale);

        canvas.drawBitmap(boardBitmap, matrix, null);
    }
}