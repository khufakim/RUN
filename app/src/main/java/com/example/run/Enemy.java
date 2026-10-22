package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

public class Enemy {
    public static final int TYPE_SCOUT = 0;
    public static final int TYPE_FIGHTER = 1;
    public static final int TYPE_BOSS = 2;

    private int type;
    private float x;
    private float y;
    private float vx;
    private float vy;

    private int hp;
    private final int maxHp;

    private float animFrame = 0f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Enemy(int type, float startX, float startY, float vy) {
        this.type = type;
        this.x = startX;
        this.y = startY;
        this.vx = (float) (Math.sin(startX) * 2.0);
        this.vy = vy;

        if (type == TYPE_SCOUT) {
            this.hp = 1;
            this.maxHp = 1;
        } else if (type == TYPE_FIGHTER) {
            this.hp = 3;
            this.maxHp = 3;
        } else {
            this.hp = 50; // Boss Ship!
            this.maxHp = 50;
        }
    }

    public void update(float screenWidth) {
        x += vx;
        y += vy;

        if (type == TYPE_BOSS) {
            // Boss moves side-to-side across screen
            if (x < 100f || x > screenWidth - 100f) {
                vx = -vx;
            }
            if (y > 220f) {
                vy = 0f; // Boss stops at top screen area and stays
            }
        }

        animFrame += 0.2f;
    }

    public void draw(Canvas canvas) {
        if (type == TYPE_SCOUT) {
            drawScout(canvas);
        } else if (type == TYPE_FIGHTER) {
            drawFighter(canvas);
        } else {
            drawBoss(canvas);
        }
    }

    private void drawScout(Canvas canvas) {
        // Red Scout Alien Ship
        Path ship = new Path();
        ship.moveTo(x, y + 25f);
        ship.lineTo(x + 25f, y - 20f);
        ship.lineTo(x, y - 10f);
        ship.lineTo(x - 25f, y - 20f);
        ship.close();

        paint.setColor(Color.parseColor("#EF4444"));
        canvas.drawPath(ship, paint);

        // Glowing Eye
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x, y - 2f, 6f, paint);
    }

    private void drawFighter(Canvas canvas) {
        // Purple Armored Cyborg Ship
        Path ship = new Path();
        ship.moveTo(x, y + 35f);
        ship.lineTo(x + 40f, y - 25f);
        ship.lineTo(x + 18f, y - 10f);
        ship.lineTo(x, y - 30f);
        ship.lineTo(x - 18f, y - 10f);
        ship.lineTo(x - 40f, y - 25f);
        ship.close();

        LinearGradient grad = new LinearGradient(x - 40f, y - 30f, x + 40f, y + 35f,
                Color.parseColor("#A855F7"), Color.parseColor("#6B21A8"), Shader.TileMode.CLAMP);
        paint.setShader(grad);
        canvas.drawPath(ship, paint);
        paint.setShader(null);

        // Glowing Red Cockpit
        paint.setColor(Color.RED);
        canvas.drawCircle(x, y, 8f, paint);
    }

    private void drawBoss(Canvas canvas) {
        // Giant Boss Warship
        Path bossBody = new Path();
        bossBody.moveTo(x, y + 60f);
        bossBody.lineTo(x + 110f, y - 30f);
        bossBody.lineTo(x + 60f, y - 60f);
        bossBody.lineTo(x - 60f, y - 60f);
        bossBody.lineTo(x - 110f, y - 30f);
        bossBody.close();

        LinearGradient bossGrad = new LinearGradient(x - 110f, y - 60f, x + 110f, y + 60f,
                Color.parseColor("#DC2626"), Color.parseColor("#7F1D1D"), Shader.TileMode.CLAMP);
        paint.setShader(bossGrad);
        canvas.drawPath(bossBody, paint);
        paint.setShader(null);

        // Glowing Core
        float corePulse = (float) Math.sin(animFrame * 3f) * 6f;
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x, y - 10f, 22f + corePulse, paint);
        paint.setColor(Color.RED);
        canvas.drawCircle(x, y - 10f, 12f, paint);

        // Boss Health Bar above Boss
        paint.setColor(Color.DKGRAY);
        RectF hpBg = new RectF(x - 80f, y - 85f, x + 80f, y - 73f);
        canvas.drawRoundRect(hpBg, 6f, 6f, paint);

        paint.setColor(Color.GREEN);
        float hpPercent = (float) hp / maxHp;
        RectF hpFill = new RectF(x - 80f, y - 85f, x - 80f + (160f * hpPercent), y - 73f);
        canvas.drawRoundRect(hpFill, 6f, 6f, paint);
    }

    public RectF getHitBox() {
        float size = (type == TYPE_BOSS) ? 90f : ((type == TYPE_FIGHTER) ? 35f : 22f);
        return new RectF(x - size, y - size, x + size, y + size);
    }

    public void takeDamage(int amount) {
        hp -= amount;
    }

    public boolean isDead() { return hp <= 0; }
    public boolean isOffScreen(float screenHeight) { return y > screenHeight + 100f; }

    public int getType() { return type; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getHp() { return hp; }
}
