package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;

public class Obstacle {
    public static final int TYPE_CONE = 0;
    public static final int TYPE_BARRIER = 1;
    public static final int TYPE_TRASHCAN = 2;
    public static final int TYPE_HIGH_BAR = 3;

    private final int type;
    private final int lane;
    private float z;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Obstacle(int type, int lane, float startZ) {
        this.type = type;
        this.lane = lane;
        this.z = startZ;
    }

    public void update(float speed) {
        z -= speed;
    }

    public boolean isPassed() {
        return z < -50f;
    }

    public boolean isNearPlayer() {
        return z >= -20f && z <= 60f;
    }

    public void draw(Canvas canvas, float screenWidth, float horizonY, float runnerY, float[] laneXBase) {
        if (z > 1000f || z < -100f) return;

        float maxZ = 1000f;
        float pFactor = 1.0f - (Math.max(0, z) / maxZ);
        pFactor = Math.min(1.0f, Math.max(0f, pFactor));

        float screenY = horizonY + (runnerY - horizonY) * pFactor;
        float centerScreenX = screenWidth / 2f;
        float baseLaneOffset = laneXBase[lane] - centerScreenX;
        float screenX = centerScreenX + baseLaneOffset * pFactor;

        float scale = 0.25f + 0.75f * pFactor;

        switch (type) {
            case TYPE_CONE:
                drawCone(canvas, screenX, screenY, scale);
                break;
            case TYPE_BARRIER:
                drawBarrier(canvas, screenX, screenY, scale);
                break;
            case TYPE_TRASHCAN:
                drawTrashCan(canvas, screenX, screenY, scale);
                break;
            case TYPE_HIGH_BAR:
                drawHighBar(canvas, screenX, screenY, scale);
                break;
        }
    }

    private void drawCone(Canvas canvas, float cx, float cy, float scale) {
        float w = 72f * scale;
        float h = 92f * scale;

        // Ground Soft Shadow
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawOval(cx - w * 0.65f, cy - 5, cx + w * 0.65f, cy + 14, paint);

        // Orange Base Plate
        paint.setColor(Color.parseColor("#C2410C"));
        RectF base = new RectF(cx - w / 2f, cy - h * 0.2f, cx + w / 2f, cy);
        canvas.drawRoundRect(base, 4f, 4f, paint);

        // 3D Shaded Cone Body
        LinearGradient coneGrad = new LinearGradient(cx - w * 0.35f, cy - h, cx + w * 0.35f, cy,
                Color.parseColor("#FB923C"), Color.parseColor("#EA580C"), Shader.TileMode.CLAMP);
        paint.setShader(coneGrad);
        Path path = new Path();
        path.moveTo(cx - w * 0.35f, cy - h * 0.2f);
        path.lineTo(cx, cy - h);
        path.lineTo(cx + w * 0.35f, cy - h * 0.2f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setShader(null);

        // Reflective Metallic Bands
        paint.setColor(Color.WHITE);
        Path stripe = new Path();
        stripe.moveTo(cx - w * 0.22f, cy - h * 0.48f);
        stripe.lineTo(cx + w * 0.22f, cy - h * 0.48f);
        stripe.lineTo(cx + w * 0.14f, cy - h * 0.68f);
        stripe.lineTo(cx - w * 0.14f, cy - h * 0.68f);
        stripe.close();
        canvas.drawPath(stripe, paint);
    }

    private void drawBarrier(Canvas canvas, float cx, float cy, float scale) {
        float w = 120f * scale;
        float h = 90f * scale;

        // Shadow
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawOval(cx - w * 0.6f, cy - 5, cx + w * 0.6f, cy + 14, paint);

        // Heavy Wooden Support Frame
        paint.setColor(Color.parseColor("#374151"));
        paint.setStrokeWidth(8f * scale);
        canvas.drawLine(cx - w * 0.4f, cy, cx - w * 0.35f, cy - h * 0.8f, paint);
        canvas.drawLine(cx + w * 0.4f, cy, cx + w * 0.35f, cy - h * 0.8f, paint);

        // Reflector Board Frame
        paint.setColor(Color.parseColor("#DC2626"));
        RectF board = new RectF(cx - w / 2f, cy - h, cx + w / 2f, cy - h * 0.3f);
        canvas.drawRoundRect(board, 6f, 6f, paint);

        // White Diagonal Reflective Panels
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 3; i++) {
            float sx = cx - w * 0.35f + i * (w * 0.3f);
            canvas.drawRect(sx - 10f * scale, cy - h + 4f, sx + 10f * scale, cy - h * 0.3f - 4f, paint);
        }

        // Dual Blinking Amber Hazard Beacons on Top
        paint.setColor(Color.parseColor("#F59E0B"));
        canvas.drawCircle(cx - w * 0.4f, cy - h - 10f * scale, 9f * scale, paint);
        canvas.drawCircle(cx + w * 0.4f, cy - h - 10f * scale, 9f * scale, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(cx - w * 0.4f, cy - h - 10f * scale, 5f * scale, paint);
        canvas.drawCircle(cx + w * 0.4f, cy - h - 10f * scale, 5f * scale, paint);
    }

    private void drawTrashCan(Canvas canvas, float cx, float cy, float scale) {
        float w = 84f * scale;
        float h = 104f * scale;

        // Shadow
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawOval(cx - w * 0.6f, cy - 5, cx + w * 0.6f, cy + 14, paint);

        // Steel Dumpster Body with Metallic Gradient Shading
        LinearGradient metalGrad = new LinearGradient(cx - w / 2f, cy - h, cx + w / 2f, cy,
                Color.parseColor("#94A3B8"), Color.parseColor("#475569"), Shader.TileMode.CLAMP);
        paint.setShader(metalGrad);
        RectF body = new RectF(cx - w / 2f, cy - h * 0.85f, cx + w / 2f, cy);
        canvas.drawRoundRect(body, 8f, 8f, paint);
        paint.setShader(null);

        // Metallic Shine Highlight Strip
        paint.setColor(Color.argb(120, 255, 255, 255));
        canvas.drawRect(cx - w * 0.38f, cy - h * 0.8f, cx - w * 0.22f, cy - h * 0.05f, paint);

        // Lid Frame
        paint.setColor(Color.parseColor("#1E293B"));
        RectF lid = new RectF(cx - w * 0.55f, cy - h, cx + w * 0.55f, cy - h * 0.8f);
        canvas.drawRoundRect(lid, 6f, 6f, paint);
        // Lid Handle
        canvas.drawRect(cx - 10f * scale, cy - h - 6f * scale, cx + 10f * scale, cy - h, paint);
    }

    private void drawHighBar(Canvas canvas, float cx, float cy, float scale) {
        float w = 140f * scale;
        float h = 180f * scale;

        // Industrial Steel Posts
        paint.setColor(Color.parseColor("#D97706"));
        paint.setStrokeWidth(14f * scale);
        canvas.drawLine(cx - w / 2f, cy, cx - w / 2f, cy - h, paint);
        canvas.drawLine(cx + w / 2f, cy, cx + w / 2f, cy - h, paint);

        // Overhead Bar
        paint.setColor(Color.parseColor("#DC2626"));
        RectF bar = new RectF(cx - w / 2f - 14f * scale, cy - h, cx + w / 2f + 14f * scale, cy - h + 52f * scale);
        canvas.drawRoundRect(bar, 8f, 8f, paint);

        // Flashing Hazard Beacons
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(cx - w / 2f - 14f * scale, cy - h + 26f * scale, 12f * scale, paint);
        canvas.drawCircle(cx + w / 2f + 14f * scale, cy - h + 26f * scale, 12f * scale, paint);

        // "SLIDE UNDER!" Sign
        paint.setColor(Color.WHITE);
        paint.setTextSize(26f * scale);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("SLIDE!", cx, cy - h + 36f * scale, paint);
    }

    public RectF getHitBox(float screenWidth, float horizonY, float runnerY, float[] laneXBase) {
        float maxZ = 1000f;
        float pFactor = 1.0f - (Math.max(0, z) / maxZ);
        float screenY = horizonY + (runnerY - horizonY) * pFactor;
        float centerScreenX = screenWidth / 2f;
        float baseLaneOffset = laneXBase[lane] - centerScreenX;
        float screenX = centerScreenX + baseLaneOffset * pFactor;
        float scale = 0.25f + 0.75f * pFactor;

        float w = 84f * scale;
        float h = (type == TYPE_HIGH_BAR) ? 140f * scale : 90f * scale;

        if (type == TYPE_HIGH_BAR) {
            return new RectF(screenX - w / 2f, screenY - h, screenX + w / 2f, screenY - h * 0.45f);
        } else {
            return new RectF(screenX - w / 2f, screenY - h, screenX + w / 2f, screenY);
        }
    }

    public int getType() { return type; }
    public float getZ() { return z; }
}
