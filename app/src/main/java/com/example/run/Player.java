package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

public class Player {
    private float x;
    private float y;
    private float targetX;
    private float targetY;

    private int hp = 100;
    private static final int MAX_HP = 100;

    private boolean tripleLaser = false;
    private int tripleLaserTimer = 0;

    private boolean shield = false;
    private int shieldTimer = 0;

    private int bombs = 2;

    private float animFrame = 0f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.targetX = startX;
        this.targetY = startY;
    }

    public void setTargetPosition(float tx, float ty) {
        this.targetX = tx;
        this.targetY = ty;
    }

    public void update(float screenWidth, float screenHeight) {
        // Smooth positioning towards touch input
        x += (targetX - x) * 0.25f;
        y += (targetY - y) * 0.25f;

        // Keep inside screen bounds
        if (x < 50f) x = 50f;
        if (x > screenWidth - 50f) x = screenWidth - 50f;
        if (y < 200f) y = 200f;
        if (y > screenHeight - 120f) y = screenHeight - 120f;

        if (tripleLaser) {
            tripleLaserTimer--;
            if (tripleLaserTimer <= 0) tripleLaser = false;
        }

        if (shield) {
            shieldTimer--;
            if (shieldTimer <= 0) shield = false;
        }

        animFrame += 0.3f;
    }

    public void draw(Canvas canvas) {
        // Thruster Fire Exhaust
        float thrusterHeight = 25f + ((float) Math.sin(animFrame * 5f) * 10f);
        paint.setColor(Color.parseColor("#FF5500"));
        canvas.drawCircle(x - 12f, y + 45f + thrusterHeight / 2f, 8f, paint);
        canvas.drawCircle(x + 12f, y + 45f + thrusterHeight / 2f, 8f, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x - 12f, y + 40f + thrusterHeight / 2f, 4f, paint);
        canvas.drawCircle(x + 12f, y + 40f + thrusterHeight / 2f, 4f, paint);

        // Shield Bubble
        if (shield) {
            paint.setColor(Color.argb(90, 6, 182, 212));
            canvas.drawCircle(x, y, 70f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6f);
            paint.setColor(Color.CYAN);
            canvas.drawCircle(x, y, 70f, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        // Cyber Starfighter Jet Wings
        Path wings = new Path();
        wings.moveTo(x, y - 55f);             // Nose
        wings.lineTo(x + 55f, y + 35f);       // Right Wing Tip
        wings.lineTo(x + 25f, y + 25f);       // Right Inner Wing
        wings.lineTo(x + 18f, y + 45f);       // Right Engine Body
        wings.lineTo(x - 18f, y + 45f);       // Left Engine Body
        wings.lineTo(x - 25f, y + 25f);       // Left Inner Wing
        wings.lineTo(x - 55f, y + 35f);       // Left Wing Tip
        wings.close();

        LinearGradient bodyGrad = new LinearGradient(x - 55f, y - 55f, x + 55f, y + 45f,
                Color.parseColor("#38BDF8"), Color.parseColor("#0284C7"), Shader.TileMode.CLAMP);
        paint.setShader(bodyGrad);
        canvas.drawPath(wings, paint);
        paint.setShader(null);

        // Dark Metal Center Fuselage
        RectF fuselage = new RectF(x - 16f, y - 35f, x + 16f, y + 40f);
        paint.setColor(Color.parseColor("#0F172A"));
        canvas.drawRoundRect(fuselage, 10f, 10f, paint);

        // Glowing Cyan Cockpit Glass Canopy
        paint.setColor(Color.CYAN);
        RectF canopy = new RectF(x - 9f, y - 20f, x + 9f, y + 10f);
        canvas.drawRoundRect(canopy, 6f, 6f, paint);

        // Dual Plasma Cannons
        paint.setColor(Color.RED);
        canvas.drawRect(x - 42f, y + 5f, x - 36f, y - 25f, paint);
        canvas.drawRect(x + 36f, y + 5f, x + 42f, y - 25f, paint);
    }

    public RectF getHitBox() {
        return new RectF(x - 45f, y - 45f, x + 45f, y + 40f);
    }

    public void takeDamage(int amount) {
        if (shield) return; // Shield blocks damage!
        hp -= amount;
        if (hp < 0) hp = 0;
    }

    public void heal(int amount) {
        hp += amount;
        if (hp > MAX_HP) hp = MAX_HP;
    }

    public void activateTripleLaser(int durationFrames) {
        this.tripleLaser = true;
        this.tripleLaserTimer = durationFrames;
    }

    public void activateShield(int durationFrames) {
        this.shield = true;
        this.shieldTimer = durationFrames;
    }

    public boolean addBomb() {
        if (bombs < 5) {
            bombs++;
            return true;
        }
        return false;
    }

    public boolean useBomb() {
        if (bombs > 0) {
            bombs--;
            return true;
        }
        return false;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getHp() { return hp; }
    public int getMaxHp() { return MAX_HP; }
    public boolean hasTripleLaser() { return tripleLaser; }
    public boolean hasShield() { return shield; }
    public int getBombs() { return bombs; }

    public void reset(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.hp = MAX_HP;
        this.tripleLaser = false;
        this.tripleLaserTimer = 0;
        this.shield = false;
        this.shieldTimer = 0;
        this.bombs = 2;
    }
}
