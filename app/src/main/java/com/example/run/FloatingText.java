package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class FloatingText {
    private float x;
    private float y;
    private final String text;
    private final int color;
    private float alpha = 255f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FloatingText(String text, float x, float y, int color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void update() {
        y -= 2.5f; // Float upward
        alpha -= 5f; // Fade out
        if (alpha < 0) alpha = 0;
    }

    public void draw(Canvas canvas) {
        if (alpha <= 0) return;
        paint.setColor(color);
        paint.setAlpha((int) alpha);
        paint.setTextSize(32f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);

        // Text shadow / outline
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha((int) (alpha * 0.7f));
        shadowPaint.setTextSize(32f);
        shadowPaint.setFakeBoldText(true);
        shadowPaint.setTextAlign(Paint.Align.CENTER);

        canvas.drawText(text, x + 2, y + 2, shadowPaint);
        canvas.drawText(text, x, y, paint);
    }

    public boolean isDead() {
        return alpha <= 0;
    }
}
