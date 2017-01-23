package com.example.kidsalphabetsar.Util;

import android.graphics.Canvas;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.text.style.ReplacementSpan;

/**
 * Created by User on 10-Dec-16.
 */

public class DashedLetterSpan extends ReplacementSpan {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int width;

    public DashedLetterSpan(int strokeWidth) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        //paint.setTextSize(160);
        PathEffect dash = new DashPathEffect(
                new float[] { strokeWidth * 3, strokeWidth }, 0);
        PathEffect corner = new CornerPathEffect(strokeWidth);
        PathEffect effect = new ComposePathEffect(dash, corner);
        paint.setPathEffect(effect);
    }

    @Override
    public int getSize(Paint paint, CharSequence text,
                       int start, int end, Paint.FontMetricsInt fm) {
        this.paint.setColor(paint.getColor());

        width = (int) (paint.measureText(text, start, end) +
                this.paint.getStrokeWidth());
        System.out.println("draw Canvas width: " + width);
        return width;
    }

    @Override
    public void draw(
            Canvas canvas, CharSequence text, int start, int end,
            float x, int top, int y, int bottom, Paint paint) {
        System.out.println("draw Canvas ");
        path.reset();
        System.out.println("draw start end x y" + start + " " + end + " " + x + " " + y);
        paint.getTextPath(text.toString(), start, end, x, y, path);
        path.close();

        canvas.translate(this.paint.getStrokeWidth() / 2, 0);
        canvas.drawPath(path, this.paint);
        canvas.translate(-this.paint.getStrokeWidth() / 2, 0);
    }

}
