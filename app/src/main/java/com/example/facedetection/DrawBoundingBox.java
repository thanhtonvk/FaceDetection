package com.example.facedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawBoundingBox extends View {
    private float r = 100;
    private float b = 100;
    private float l = 100;
    private float t = 200;
    Paint mPaint;

    public DrawBoundingBox(Context context) {
        super(context);
        initPaint();
    }

    public DrawBoundingBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }

    public DrawBoundingBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeWidth(10);
        rectangle = new Rect(0, 0, 0, 0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(rectangle, mPaint);
        // Draw the rectangle with left, right, top, and bottom coordinates.
    }

    private Rect rectangle;

    public void updateRectangle(int left, int top, int right, int bottom) {
        // Update the rectangle.
        rectangle.set(left, top, right, bottom);
        // Invalidate the view to force it to redraw itself.
        invalidate();
    }
}

