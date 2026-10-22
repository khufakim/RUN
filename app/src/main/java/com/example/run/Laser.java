package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Laser {
    private float x;
    private float y;
    private final float vx;
    private final float vy;
    private final boolean isPlayer;
    private final int color;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Laser(float x, float y, float vx, float vy, boolean isPlayer, int color) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.isPlayer = isPlayer;
        this.color = color;
    }

    public void update() {
        x += vx;
        y += vy;
    }

    public void draw(Canvas canvas) {
        // Outer Laser Glow
        paint.setColor(color);
        paint.setAlpha(180);
        canvas.drawCircle(x, y, 8f, paint);

        // Core Laser Beam
        paint.setColor(Color.WHITE);
        paint.setAlpha(255);
        canvas.drawCircle(x, y, 4f, paint);

        // Laser Tail
        paint.setColor(color);
        paint.setStrokeWidth(5f);
        canvas.drawLine(x, y, x - vx * 2, y - vy * 2, paint);
    }

    public boolean isOffScreen(float screenWidth, float screenHeight) {
        return x < -50 || x > screenWidth + 50 || y < -50 || y > screenHeight + 50;
    }

    public RectF getHitBox() {
        return new RectF(x - 10f, y - 10f, x + 10f, y + 10f);
    }

    public boolean isPlayer() { return isPlayer; }
    public float getX() { return x; }
    public float getY() { return y; }
}
