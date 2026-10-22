package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class Coin {
    public static final int TYPE_CRYSTAL = 0;
    public static final int TYPE_TRIPLE = 1;
    public static final int TYPE_SHIELD = 2;
    public static final int TYPE_HEAL = 3;
    public static final int TYPE_BOMB = 4;

    private final int type;
    private float x;
    private float y;
    private final float vy;

    private float animFrame = 0f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Coin(int type, float startX, float startY) {
        this.type = type;
        this.x = startX;
        this.y = startY;
        this.vy = 4.5f;
    }

    public void update() {
        y += vy;
        animFrame += 0.25f;
    }

    public void draw(Canvas canvas) {
        if (type == TYPE_CRYSTAL) {
            drawEnergyCrystal(canvas);
        } else if (type == TYPE_TRIPLE) {
            drawTripleGunIcon(canvas);
        } else if (type == TYPE_SHIELD) {
            drawShieldIcon(canvas);
        } else if (type == TYPE_HEAL) {
            drawHealIcon(canvas);
        } else if (type == TYPE_BOMB) {
            drawBombIcon(canvas);
        }
    }

    private void drawEnergyCrystal(Canvas canvas) {
        // Glowing Golden Energy Crystal
        paint.setColor(Color.parseColor("#F59E0B"));
        canvas.drawCircle(x, y, 22f, paint);

        paint.setColor(Color.WHITE);
        Path crystal = new Path();
        crystal.moveTo(x, y - 16f);
        crystal.lineTo(x + 12f, y);
        crystal.lineTo(x, y + 16f);
        crystal.lineTo(x - 12f, y);
        crystal.close();
        canvas.drawPath(crystal, paint);
    }

    private void drawTripleGunIcon(Canvas canvas) {
        // Cyan Triple Laser Power-up
        paint.setColor(Color.parseColor("#0284C7"));
        canvas.drawCircle(x, y, 24f, paint);

        paint.setColor(Color.CYAN);
        canvas.drawRect(x - 12f, y - 12f, x - 6f, y + 12f, paint);
        canvas.drawRect(x - 3f, y - 16f, x + 3f, y + 12f, paint);
        canvas.drawRect(x + 6f, y - 12f, x + 12f, y + 12f, paint);
    }

    private void drawShieldIcon(Canvas canvas) {
        // Emerald Shield Power-up
        paint.setColor(Color.parseColor("#059669"));
        canvas.drawCircle(x, y, 24f, paint);

        paint.setColor(Color.WHITE);
        Path shield = new Path();
        shield.moveTo(x - 12f, y - 12f);
        shield.lineTo(x + 12f, y - 12f);
        shield.lineTo(x + 12f, y);
        shield.lineTo(x, y + 15f);
        shield.lineTo(x - 12f, y);
        shield.close();
        canvas.drawPath(shield, paint);
    }

    private void drawHealIcon(Canvas canvas) {
        // Red Cross Repair Kit
        paint.setColor(Color.parseColor("#DC2626"));
        canvas.drawCircle(x, y, 24f, paint);

        paint.setColor(Color.WHITE);
        canvas.drawRect(x - 12f, y - 4f, x + 12f, y + 4f, paint);
        canvas.drawRect(x - 4f, y - 12f, x + 4f, y + 12f, paint);
    }

    private void drawBombIcon(Canvas canvas) {
        // Yellow Bomb Shockwave Powerup
        paint.setColor(Color.parseColor("#D97706"));
        canvas.drawCircle(x, y, 24f, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x, y, 14f, paint);
        paint.setColor(Color.RED);
        canvas.drawCircle(x, y, 6f, paint);
    }

    public boolean isOffScreen(float screenHeight) { return y > screenHeight + 50f; }

    public RectF getHitBox() {
        return new RectF(x - 24f, y - 24f, x + 24f, y + 24f);
    }

    public int getType() { return type; }
    public float getX() { return x; }
    public float getY() { return y; }
}
