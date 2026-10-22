package com.example.run;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Particle {
    private float x;
    private float y;
    private final float vx;
    private final float vy;
    private final int color;
    private final float size;
    private float alpha = 255f;
    private final int fadeSpeed;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Particle(float x, float y, float vx, float vy, int color, float size, int fadeSpeed) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.size = size;
        this.fadeSpeed = fadeSpeed;
    }

    public void update() {
        x += vx;
        y += vy;
        alpha -= fadeSpeed;
        if (alpha < 0) alpha = 0;
    }

    public void draw(Canvas canvas) {
        if (alpha <= 0) return;
        paint.setColor(color);
        paint.setAlpha((int) alpha);
        canvas.drawCircle(x, y, size, paint);
    }

    public boolean isDead() {
        return alpha <= 0;
    }
}
