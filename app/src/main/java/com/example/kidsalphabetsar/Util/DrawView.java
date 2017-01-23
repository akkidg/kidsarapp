package com.example.kidsalphabetsar.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {

    int back_color=0xFF000000;
    int i=0;
    int newColor;
    public int width;
    int brush_size=20;
    public int height;
    int x_value=0;
    int set_color=0xFF000000;

    private Bitmap mBitmap,bitmap;

    private Canvas mCanvas;

    private Path mPath;
    private MaskFilter mEmboss;

    Context context;

    private Paint mPaint,mBitmapPaint;

    private float mX, mY;
    private static final float TOLERANCE = 0;
    private float x,y;
//    String text_to_draw;

    public DrawView(Context c, AttributeSet attrs) {

        super(c, attrs);

        context = c;

        // we set a new Path

        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(brush_size);
//        mCanvas.drawText("abc",10,20,mPaint);

        // and we set a new Paint with the desired attributes


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);

        width=w;
        height=h;

        // your Canvas will draw onto the defined Bitmap

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        mCanvas = new Canvas(mBitmap);

    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);


            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
           // Toast.makeText(getContext(),"value else "+x_value,Toast.LENGTH_SHORT).show();

            // draw the mPath with the mPaint on the canvas when onDraw
            canvas.drawPath(mPath, mPaint);


    }

    public void startTouch(float x, float y) {
        mPath.reset();

        mPath.moveTo(x, y);

        mX = x;

        mY = y;


    }

    public void moveTouch(float x, float y) {

        float dx = Math.abs(x - mX);

        float dy = Math.abs(y - mY);

        if (dx >= TOLERANCE || dy >= TOLERANCE) {

            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);

            mX = x;

            mY = y;



        }



    }


    public void upTouch() {


            mPath.lineTo(mX, mY);
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();





    }

    @Override

    public boolean onTouchEvent(MotionEvent event) {

        x = event.getX();

        y = event.getY();

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:

                    startTouch(x, y);

                    invalidate();

                    break;

                case MotionEvent.ACTION_MOVE:

                    moveTouch(x, y);

                    invalidate();

                    break;

                case MotionEvent.ACTION_UP:

                    upTouch();

                    invalidate();

                    break;

            }


        return true;

    }

  /*  void setTextToDraw(String str){
        text_to_draw=str;
    }*/

    public void clear()

    {
        x_value=0;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        //mCanvas.drawBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.a),0,0,null);
        //mPath = new Path();
    }



}