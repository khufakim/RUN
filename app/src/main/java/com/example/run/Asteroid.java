package com.example.run;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class Asteroid {
    private float x;
    private float y;
    private final float size;
    private final float speed;
    private float angle = 0f;
    private final float rotSpeed;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public Asteroid(float startX, float startY, float size, float speed) {
        this.x = startX;
        this.y = startY;
        this.size = size;
        this.speed = speed;
        this.rotSpeed = (float) ((Math.random() - 0.5) * 0.1);
    }

    public void update() {
        y += speed;
        angle += rotSpeed;
    }

    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(x, y);
        canvas.rotate((float) Math.toDegrees(angle));

        // Asteroid Rock Body
        paint.setColor(Color.parseColor("#64748B"));
        Path rock = new Path();
        rock.moveTo(-size * 0.8f, -size * 0.5f);
        rock.lineTo(0f, -size);
        rock.lineTo(size * 0.9f, -size * 0.6f);
        rock.lineTo(size, size * 0.4f);
        rock.lineTo(size * 0.3f, size * 0.9f);
        rock.lineTo(-size * 0.7f, size * 0.7f);
        rock.close();
        canvas.drawPath(rock, paint);

        // Crater Details
        paint.setColor(Color.parseColor("#334155"));
        canvas.drawCircle(-size * 0.2f, -size * 0.3f, size * 0.22f, paint);
        canvas.drawCircle(size * 0.3f, size * 0.2f, size * 0.18f, paint);

        canvas.restore();
    }

    public boolean isOffScreen(float screenHeight) {
        return y > screenHeight + 100f;
    }

    public RectF getHitBox() {
        return new RectF(x - size * 0.8f, y - size * 0.8f, x + size * 0.8f, y + size * 0.8f);
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
