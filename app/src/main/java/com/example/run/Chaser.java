package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

public class Chaser {
    private float x;
    private float y;
    private float targetX;

    private float distanceOffset = 180f;
    private float targetOffset = 180f;

    private float animFrame = 0f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Chaser(float startX, float startY) {
        this.x = startX;
        this.y = startY + distanceOffset;
        this.targetX = startX;
    }

    public void update(float playerX, float playerY, boolean playerStumbled) {
        this.targetX = playerX;
        this.x += (targetX - x) * 0.14f;

        if (playerStumbled) {
            targetOffset = 65f;
        } else {
            targetOffset = 180f;
        }

        distanceOffset += (targetOffset - distanceOffset) * 0.05f;
        this.y = playerY + distanceOffset;

        animFrame += 0.26f;
    }

    public void draw(Canvas canvas) {
        drawPoliceOfficer(canvas, x, y);
        drawSmallDog(canvas, x + 52f, y + 10f);
    }

    private void drawPoliceOfficer(Canvas canvas, float px, float py) {
        float legSwing = (float) Math.sin(animFrame) * 26f;

        // Ground Soft Shadow
        paint.setColor(Color.argb(120, 0, 0, 0));
        canvas.drawOval(px - 38, py + 5, px + 38, py + 20, paint);

        // Emergency Flashing Police Strobe Light Reflection Glow
        boolean strobeToggle = ((int) (animFrame * 5) % 2 == 0);
        paint.setColor(strobeToggle ? Color.argb(75, 239, 68, 68) : Color.argb(75, 59, 130, 246));
        canvas.drawCircle(px, py - 90, 85f, paint);

        // Dark Blue Uniform Pants & Running Legs
        paint.setColor(Color.parseColor("#1E293B"));
        paint.setStrokeWidth(18f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(px - 15, py - 40, px - 18 - legSwing, py, paint);
        canvas.drawLine(px + 15, py - 40, px + 18 + legSwing, py, paint);

        // Black Polished Leather Boots
        paint.setColor(Color.BLACK);
        canvas.drawCircle(px - 18 - legSwing, py, 11f, paint);
        canvas.drawCircle(px + 18 + legSwing, py, 11f, paint);

        // Realistic Police Jacket with Gradient Shading
        LinearGradient jacketGrad = new LinearGradient(px - 30, py - 95, px + 30, py - 35,
                Color.parseColor("#1D4ED8"), Color.parseColor("#1E3A8A"), Shader.TileMode.CLAMP);
        paint.setShader(jacketGrad);
        RectF torso = new RectF(px - 30, py - 95, px + 30, py - 35);
        canvas.drawRoundRect(torso, 14f, 14f, paint);
        paint.setShader(null);

        // Realistic Gold Police Star Badge with 3D Center
        paint.setColor(Color.parseColor("#F59E0B"));
        canvas.drawCircle(px - 14, py - 75, 9f, paint);
        paint.setColor(Color.parseColor("#FDE047"));
        canvas.drawCircle(px - 14, py - 75, 5f, paint);

        // Yellow Uniform Tie
        paint.setColor(Color.parseColor("#FBBF24"));
        canvas.drawRect(px - 4, py - 90, px + 4, py - 60, paint);

        // Shaded Arms & Police Baton
        paint.setColor(Color.parseColor("#FBBF24"));
        paint.setStrokeWidth(12f);
        canvas.drawLine(px - 30, py - 85, px - 46 - legSwing, py - 50, paint);
        canvas.drawLine(px + 30, py - 85, px + 46 + legSwing, py - 50, paint);

        // Black Police Baton
        paint.setColor(Color.BLACK);
        canvas.drawLine(px + 46 + legSwing, py - 50, px + 68 + legSwing, py - 70, paint);

        // Realistic Officer Head & Mustache
        paint.setColor(Color.parseColor("#FBBF24"));
        canvas.drawCircle(px, py - 110, 22f, paint);

        // Mustache
        paint.setColor(Color.parseColor("#451A03"));
        RectF stache = new RectF(px - 16, py - 105, px + 16, py - 97);
        canvas.drawRoundRect(stache, 5f, 5f, paint);

        // Police Cap with Leather Visor
        paint.setColor(Color.parseColor("#1E293B"));
        RectF cap = new RectF(px - 28, py - 136, px + 28, py - 112);
        canvas.drawRoundRect(cap, 8f, 8f, paint);

        paint.setColor(Color.BLACK);
        canvas.drawRect(px - 30, py - 118, px + 30, py - 112, paint);

        paint.setColor(Color.parseColor("#F59E0B"));
        canvas.drawCircle(px, py - 124, 7f, paint);

        // Whistle Note Speech Bubble
        float noteWave = (float) Math.sin(animFrame * 2f) * 6f;
        paint.setColor(Color.YELLOW);
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText("🎵 STOP!", px - 85, py - 130 + noteWave, paint);
    }

    private void drawSmallDog(Canvas canvas, float dx, float dy) {
        float dogLegSwing = (float) Math.sin(animFrame * 1.5f) * 16f;

        // Shadow
        paint.setColor(Color.argb(80, 0, 0, 0));
        canvas.drawOval(dx - 22, dy + 2, dx + 22, dy + 12, paint);

        // German Shepherd Brown Body with Saddle Shading
        paint.setColor(Color.parseColor("#92400E"));
        RectF body = new RectF(dx - 20, dy - 24, dx + 20, dy - 8);
        canvas.drawRoundRect(body, 10f, 10f, paint);

        // Dark Fur Saddle Back
        paint.setColor(Color.parseColor("#451A03"));
        RectF saddle = new RectF(dx - 12, dy - 24, dx + 12, dy - 16);
        canvas.drawRoundRect(saddle, 4f, 4f, paint);

        // Running Legs
        paint.setColor(Color.parseColor("#78350F"));
        paint.setStrokeWidth(7f);
        canvas.drawLine(dx - 14, dy - 10, dx - 16 - dogLegSwing, dy, paint);
        canvas.drawLine(dx - 4, dy - 10, dx - 2 + dogLegSwing, dy, paint);
        canvas.drawLine(dx + 6, dy - 10, dx + 4 - dogLegSwing, dy, paint);
        canvas.drawLine(dx + 14, dy - 10, dx + 16 + dogLegSwing, dy, paint);

        // Red Police Collar with Gold Tag
        paint.setColor(Color.RED);
        canvas.drawRect(dx - 18, dy - 26, dx - 11, dy - 21, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(dx - 14, dy - 20, 3f, paint);

        // Dog Head
        paint.setColor(Color.parseColor("#92400E"));
        canvas.drawCircle(dx - 18, dy - 32, 14f, paint);

        // Floppy Ears
        paint.setColor(Color.parseColor("#451A03"));
        canvas.drawOval(dx - 30, dy - 34, dx - 22, dy - 20, paint);

        // Nose & Pink Tongue
        paint.setColor(Color.BLACK);
        canvas.drawCircle(dx - 26, dy - 32, 3f, paint);
        paint.setColor(Color.parseColor("#F43F5E"));
        canvas.drawOval(dx - 28, dy - 28, dx - 21, dy - 22, paint);

        // Wagging Tail
        float tailWag = (float) Math.cos(animFrame * 2f) * 14f;
        paint.setColor(Color.parseColor("#92400E"));
        paint.setStrokeWidth(6f);
        canvas.drawLine(dx + 18, dy - 20, dx + 30, dy - 28 + tailWag, paint);

        // Bark text bubble
        if ((int) (animFrame * 10) % 20 < 10) {
            paint.setColor(Color.CYAN);
            paint.setTextSize(20f);
            paint.setFakeBoldText(true);
            canvas.drawText("WOOF!", dx + 12, dy - 38, paint);
        }
    }

    public void reset(float startX, float startY) {
        this.x = startX;
        this.y = startY + 180f;
        this.targetX = startX;
        this.distanceOffset = 180f;
        this.targetOffset = 180f;
    }
}
