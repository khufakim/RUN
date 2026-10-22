package com.example.run;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    public static final int STATE_MENU = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSED = 2;
    public static final int STATE_GAME_OVER = 3;

    private int gameState = STATE_MENU;

    private GameThread gameThread;
    private final SoundManager soundManager;
    private final Random random = new Random();

    // Dimensions
    private int screenWidth;
    private int screenHeight;

    // Entities
    private Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Laser> lasers = new ArrayList<>();
    private final List<Asteroid> asteroids = new ArrayList<>();
    private final List<Coin> powerups = new ArrayList<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // Starfield Background
    private final float[] starX = new float[60];
    private final float[] starY = new float[60];
    private final float[] starSpeed = new float[60];

    // Game Metrics
    private int score = 0;
    private int crystals = 0;
    private int wave = 1;
    private int highScore = 0;

    private int frameCounter = 0;
    private int fireTimer = 0;

    // Drawing
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private SharedPreferences prefs;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        getHolder().addCallback(this);
        setFocusable(true);

        soundManager = new SoundManager();
        if (!isInEditMode()) {
            prefs = context.getSharedPreferences("GalaxyStrikerPrefs", Context.MODE_PRIVATE);
            highScore = prefs.getInt("highScore", 0);
        }

        // Initialize Starfield
        for (int i = 0; i < 60; i++) {
            starX[i] = random.nextFloat() * 1080f;
            starY[i] = random.nextFloat() * 2400f;
            starSpeed[i] = 2f + random.nextFloat() * 8f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode()) {
            screenWidth = getWidth() > 0 ? getWidth() : 1080;
            screenHeight = getHeight() > 0 ? getHeight() : 2400;

            if (player == null) {
                player = new Player(screenWidth / 2f, screenHeight * 0.8f);
            }
            drawGame(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
        }

        float touchX = event.getX();
        float touchY = event.getY();

        if (gameState == STATE_PLAYING) {
            // Check Bomb Button (Bottom Right)
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isTouchInCircle(touchX, touchY, screenWidth - 100, screenHeight - 120, 50)) {
                    triggerBomb();
                    return true;
                }
                // Check Pause Button (Top Right)
                if (isTouchInCircle(touchX, touchY, screenWidth - 70, 70, 40)) {
                    gameState = STATE_PAUSED;
                    return true;
                }
            }
            // Move Player towards touch position
            if (player != null) {
                player.setTargetPosition(touchX, touchY - 80f);
            }
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameState == STATE_MENU) {
                // Sound Toggle
                if (isTouchInCircle(touchX, touchY, screenWidth - 70, 80, 40)) {
                    soundManager.toggleSound();
                    return true;
                }
                startGame();
            } else if (gameState == STATE_PAUSED) {
                // Resume
                if (isTouchInRect(touchX, touchY, screenWidth / 2f - 150, screenHeight / 2f - 40, screenWidth / 2f + 150, screenHeight / 2f + 20)) {
                    gameState = STATE_PLAYING;
                }
                // Menu
                else if (isTouchInRect(touchX, touchY, screenWidth / 2f - 150, screenHeight / 2f + 50, screenWidth / 2f + 150, screenHeight / 2f + 110)) {
                    gameState = STATE_MENU;
                }
            } else if (gameState == STATE_GAME_OVER) {
                // Retry
                if (isTouchInRect(touchX, touchY, screenWidth / 2f - 150, screenHeight / 2f + 80, screenWidth / 2f + 150, screenHeight / 2f + 150)) {
                    startGame();
                }
                // Menu
                else if (isTouchInRect(touchX, touchY, screenWidth / 2f - 150, screenHeight / 2f + 170, screenWidth / 2f + 150, screenHeight / 2f + 230)) {
                    gameState = STATE_MENU;
                }
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private boolean isTouchInRect(float tx, float ty, float left, float top, float right, float bottom) {
        return tx >= left && tx <= right && ty >= top && ty <= bottom;
    }

    private boolean isTouchInCircle(float tx, float ty, float cx, float cy, float radius) {
        float dx = tx - cx;
        float dy = ty - cy;
        return (dx * dx + dy * dy) <= (radius * radius);
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        if (player == null) {
            player = new Player(screenWidth / 2f, screenHeight * 0.8f);
        }

        startGameThread();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        stopGameThread();
    }

    private void startGameThread() {
        if (gameThread == null) {
            gameThread = new GameThread(getHolder(), this);
            gameThread.setRunning(true);
            gameThread.start();
        }
    }

    private void stopGameThread() {
        if (gameThread != null) {
            gameThread.setRunning(false);
            boolean retry = true;
            while (retry) {
                try {
                    gameThread.join();
                    retry = false;
                } catch (InterruptedException ignored) {
                }
            }
            gameThread = null;
        }
    }

    public void pause() {
        if (gameState == STATE_PLAYING) {
            gameState = STATE_PAUSED;
        }
        stopGameThread();
    }

    public void resume() {
        startGameThread();
    }

    private void startGame() {
        score = 0;
        crystals = 0;
        wave = 1;
        enemies.clear();
        lasers.clear();
        asteroids.clear();
        powerups.clear();
        particles.clear();
        floatingTexts.clear();

        player.reset(screenWidth / 2f, screenHeight * 0.8f);
        gameState = STATE_PLAYING;
    }

    private void triggerBomb() {
        if (player != null && player.useBomb()) {
            soundManager.playBombSound();

            // Destroy enemies
            for (Enemy enemy : enemies) {
                spawnExplosion(enemy.getX(), enemy.getY(), Color.YELLOW, 20);
                score += 150;
            }
            enemies.clear();

            // Destroy enemy lasers
            lasers.removeIf(laser -> !laser.isPlayer());

            floatingTexts.add(new FloatingText("BOMB SHOCKWAVE!", screenWidth / 2f, screenHeight / 2f, Color.YELLOW));
        }
    }

    public void updateGame() {
        // Update Starfield
        for (int i = 0; i < 60; i++) {
            starY[i] += starSpeed[i];
            if (starY[i] > screenHeight) {
                starY[i] = -20f;
                starX[i] = random.nextFloat() * screenWidth;
            }
        }

        if (gameState != STATE_PLAYING) return;

        frameCounter++;

        // Update Player
        player.update(screenWidth, screenHeight);

        // Player Cannon Firing
        fireTimer++;
        if (fireTimer >= 8) {
            fireTimer = 0;
            soundManager.playLaserSound();

            if (player.hasTripleLaser()) {
                lasers.add(new Laser(player.getX() - 25f, player.getY() - 30f, -3f, -28f, true, Color.CYAN));
                lasers.add(new Laser(player.getX(), player.getY() - 45f, 0f, -30f, true, Color.CYAN));
                lasers.add(new Laser(player.getX() + 25f, player.getY() - 30f, 3f, -28f, true, Color.CYAN));
            } else {
                lasers.add(new Laser(player.getX() - 15f, player.getY() - 35f, 0f, -28f, true, Color.CYAN));
                lasers.add(new Laser(player.getX() + 15f, player.getY() - 35f, 0f, -28f, true, Color.CYAN));
            }
        }

        // Spawning Wave Logic
        if (frameCounter % 60 == 0 && enemies.size() < 6) {
            int spawnType = (frameCounter % 300 == 0) ? Enemy.TYPE_FIGHTER : Enemy.TYPE_SCOUT;
            float spawnX = 80f + random.nextFloat() * (screenWidth - 160f);
            enemies.add(new Enemy(spawnType, spawnX, -60f, 3.5f + wave * 0.4f));
        }

        // Boss Spawn Every 1000 score
        if (score > wave * 1000 && enemies.stream().noneMatch(e -> e.getType() == Enemy.TYPE_BOSS)) {
            enemies.add(new Enemy(Enemy.TYPE_BOSS, screenWidth / 2f, -100f, 2f));
            floatingTexts.add(new FloatingText("WARNING: BOSS INCOMING!", screenWidth / 2f, screenHeight * 0.3f, Color.RED));
            wave++;
        }

        // Asteroids Spawning
        if (frameCounter % 90 == 0) {
            float aX = random.nextFloat() * screenWidth;
            float aSize = 30f + random.nextFloat() * 25f;
            asteroids.add(new Asteroid(aX, -50f, aSize, 4f + random.nextFloat() * 3f));
        }

        // Enemy Laser Fire
        for (Enemy enemy : enemies) {
            if (random.nextFloat() < 0.02f) {
                lasers.add(new Laser(enemy.getX(), enemy.getY() + 30f, 0f, 14f, false, Color.RED));
            }
        }

        // Update Lasers
        Iterator<Laser> lIterator = lasers.iterator();
        while (lIterator.hasNext()) {
            Laser laser = lIterator.next();
            laser.update();

            if (laser.isOffScreen(screenWidth, screenHeight)) {
                lIterator.remove();
                continue;
            }

            if (laser.isPlayer()) {
                // Player Laser hits Enemy
                for (Enemy enemy : enemies) {
                    if (RectF.intersects(laser.getHitBox(), enemy.getHitBox())) {
                        enemy.takeDamage(1);
                        lIterator.remove();
                        spawnParticles(laser.getX(), laser.getY(), Color.CYAN, 5);

                        if (enemy.isDead()) {
                            soundManager.playExplosionSound();
                            spawnExplosion(enemy.getX(), enemy.getY(), Color.YELLOW, 18);
                            score += (enemy.getType() == Enemy.TYPE_BOSS) ? 500 : 100;

                            if (random.nextFloat() < 0.3f) {
                                int pType = random.nextInt(5);
                                powerups.add(new Coin(pType, enemy.getX(), enemy.getY()));
                            }
                        }
                        break;
                    }
                }
            } else {
                // Enemy Laser hits Player
                if (RectF.intersects(laser.getHitBox(), player.getHitBox())) {
                    lIterator.remove();
                    player.takeDamage(12);
                    spawnParticles(player.getX(), player.getY(), Color.RED, 10);

                    if (player.getHp() <= 0) {
                        onGameOver();
                        return;
                    }
                }
            }
        }

        // Remove Dead Enemies
        enemies.removeIf(enemy -> enemy.isDead() || enemy.isOffScreen(screenHeight));

        // Update Asteroids
        Iterator<Asteroid> aIterator = asteroids.iterator();
        while (aIterator.hasNext()) {
            Asteroid ast = aIterator.next();
            ast.update();

            if (ast.isOffScreen(screenHeight)) {
                aIterator.remove();
                continue;
            }

            if (RectF.intersects(ast.getHitBox(), player.getHitBox())) {
                aIterator.remove();
                player.takeDamage(20);
                soundManager.playExplosionSound();
                spawnExplosion(ast.getX(), ast.getY(), Color.GRAY, 15);

                if (player.getHp() <= 0) {
                    onGameOver();
                    return;
                }
            }
        }

        // Update Powerups
        Iterator<Coin> pIterator = powerups.iterator();
        while (pIterator.hasNext()) {
            Coin p = pIterator.next();
            p.update();

            if (p.isOffScreen(screenHeight)) {
                pIterator.remove();
                continue;
            }

            if (RectF.intersects(p.getHitBox(), player.getHitBox())) {
                pIterator.remove();
                soundManager.playPowerupSound();

                switch (p.getType()) {
                    case Coin.TYPE_CRYSTAL:
                        crystals++;
                        score += 50;
                        floatingTexts.add(new FloatingText("+50 CRYSTAL", player.getX(), player.getY() - 40, Color.YELLOW));
                        break;
                    case Coin.TYPE_TRIPLE:
                        player.activateTripleLaser(300);
                        floatingTexts.add(new FloatingText("TRIPLE LASER!", player.getX(), player.getY() - 40, Color.CYAN));
                        break;
                    case Coin.TYPE_SHIELD:
                        player.activateShield(350);
                        floatingTexts.add(new FloatingText("SHIELD ACTIVE!", player.getX(), player.getY() - 40, Color.GREEN));
                        break;
                    case Coin.TYPE_HEAL:
                        player.heal(30);
                        floatingTexts.add(new FloatingText("+30 REPAIR HP", player.getX(), player.getY() - 40, Color.RED));
                        break;
                    case Coin.TYPE_BOMB:
                        player.addBomb();
                        floatingTexts.add(new FloatingText("+1 PLASMA BOMB", player.getX(), player.getY() - 40, Color.YELLOW));
                        break;
                }
            }
        }

        // Update Enemies
        for (Enemy enemy : enemies) {
            enemy.update(screenWidth);
        }

        // Update Particles
        Iterator<Particle> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            Particle part = particleIterator.next();
            part.update();
            if (part.isDead()) particleIterator.remove();
        }

        // Update Floating Text
        Iterator<FloatingText> ftIterator = floatingTexts.iterator();
        while (ftIterator.hasNext()) {
            FloatingText ft = ftIterator.next();
            ft.update();
            if (ft.isDead()) ftIterator.remove();
        }
    }

    private void spawnParticles(float x, float y, int color, int count) {
        for (int i = 0; i < count; i++) {
            float vx = (random.nextFloat() - 0.5f) * 12f;
            float vy = (random.nextFloat() - 0.5f) * 12f;
            particles.add(new Particle(x, y, vx, vy, color, 6f + random.nextFloat() * 4f, 12 + random.nextInt(8)));
        }
    }

    private void spawnExplosion(float x, float y, int color, int count) {
        spawnParticles(x, y, color, count);
        spawnParticles(x, y, Color.RED, count / 2);
        spawnParticles(x, y, Color.WHITE, count / 2);
    }

    private void onGameOver() {
        gameState = STATE_GAME_OVER;
        soundManager.playExplosionSound();
        spawnExplosion(player.getX(), player.getY(), Color.RED, 30);

        if (score > highScore && prefs != null) {
            highScore = score;
            prefs.edit().putInt("highScore", highScore).apply();
        }
    }

    public void drawGame(Canvas canvas) {
        if (canvas == null) return;

        // Space Deep Sky Background
        canvas.drawColor(Color.parseColor("#0B0F19"));

        // Twinkling Starfield
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 60; i++) {
            paint.setAlpha(120 + (i * 2) % 135);
            canvas.drawCircle(starX[i], starY[i], (i % 3 == 0) ? 3f : 2f, paint);
        }
        paint.setAlpha(255);

        // Draw Asteroids
        for (Asteroid ast : asteroids) {
            ast.draw(canvas);
        }

        // Draw Powerups
        for (Coin powerup : powerups) {
            powerup.draw(canvas);
        }

        // Draw Lasers
        for (Laser laser : lasers) {
            laser.draw(canvas);
        }

        // Draw Enemies
        for (Enemy enemy : enemies) {
            enemy.draw(canvas);
        }

        // Draw Player Ship
        if (player != null && gameState != STATE_GAME_OVER) {
            player.draw(canvas);
        }

        // Draw Particles
        for (Particle part : particles) {
            part.draw(canvas);
        }

        // Draw Floating Text
        for (FloatingText ft : floatingTexts) {
            ft.draw(canvas);
        }

        drawHUD(canvas);
    }

    private void drawHUD(Canvas canvas) {
        if (gameState == STATE_PLAYING || gameState == STATE_PAUSED) {
            // Player HP Bar Top Left
            paint.setColor(Color.argb(190, 15, 23, 42));
            RectF hpBg = new RectF(30, 30, 260, 80);
            canvas.drawRoundRect(hpBg, 14f, 14f, paint);

            paint.setColor(Color.RED);
            float hpRatio = (float) player.getHp() / player.getMaxHp();
            RectF hpFill = new RectF(35, 35, 35 + (220 * hpRatio), 75);
            canvas.drawRoundRect(hpFill, 10f, 10f, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(24f);
            paint.setFakeBoldText(true);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("HP " + player.getHp(), 145, 62, paint);

            // Score Top Center
            paint.setColor(Color.argb(190, 15, 23, 42));
            RectF scoreBg = new RectF(screenWidth / 2f - 120, 30, screenWidth / 2f + 120, 80);
            canvas.drawRoundRect(scoreBg, 14f, 14f, paint);

            paint.setColor(Color.CYAN);
            paint.setTextSize(32f);
            canvas.drawText("SCORE " + score, screenWidth / 2f, 66, paint);

            // Pause Button Top Right
            paint.setColor(Color.argb(190, 15, 23, 42));
            canvas.drawCircle(screenWidth - 70, 70, 35f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawRect(screenWidth - 80, 52, screenWidth - 73, 88, paint);
            canvas.drawRect(screenWidth - 67, 52, screenWidth - 60, 88, paint);

            // Bomb Button Bottom Right
            paint.setColor(Color.argb(220, 220, 38, 38));
            canvas.drawCircle(screenWidth - 100, screenHeight - 120, 50f, paint);
            paint.setColor(Color.YELLOW);
            paint.setTextSize(26f);
            canvas.drawText("BOMB (" + player.getBombs() + ")", screenWidth - 100, screenHeight - 110, paint);

            if (gameState == STATE_PAUSED) {
                drawPauseOverlay(canvas);
            }
        } else if (gameState == STATE_MENU) {
            drawMenuOverlay(canvas);
        } else if (gameState == STATE_GAME_OVER) {
            drawGameOverOverlay(canvas);
        }
    }

    private void drawMenuOverlay(Canvas canvas) {
        paint.setColor(Color.argb(190, 11, 15, 25));
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        paint.setColor(Color.parseColor("#0284C7"));
        RectF titleBox = new RectF(40, screenHeight * 0.18f, screenWidth - 40, screenHeight * 0.35f);
        canvas.drawRoundRect(titleBox, 26f, 26f, paint);

        paint.setColor(Color.CYAN);
        paint.setTextSize(64f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GALAXY STRIKER", screenWidth / 2f, screenHeight * 0.25f, paint);

        paint.setColor(Color.YELLOW);
        paint.setTextSize(32f);
        canvas.drawText("CYBER WAR ACTION", screenWidth / 2f, screenHeight * 0.31f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36f);
        canvas.drawText("🏆 HIGH SCORE: " + highScore, screenWidth / 2f, screenHeight * 0.44f, paint);

        paint.setColor(Color.parseColor("#22C55E"));
        RectF launchBtn = new RectF(screenWidth / 2f - 170, screenHeight * 0.58f, screenWidth / 2f + 170, screenHeight * 0.68f);
        canvas.drawRoundRect(launchBtn, 32f, 32f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(42f);
        canvas.drawText("TAP TO LAUNCH!", screenWidth / 2f, screenHeight * 0.645f, paint);

        paint.setTextSize(26f);
        paint.setColor(Color.parseColor("#94A3B8"));
        canvas.drawText("DRAG SHIP TO MOVE & FIRE LASERS", screenWidth / 2f, screenHeight * 0.77f, paint);
        canvas.drawText("TAP BOMB TO CLEAR ENEMY WAVES", screenWidth / 2f, screenHeight * 0.82f, paint);
    }

    private void drawPauseOverlay(Canvas canvas) {
        paint.setColor(Color.argb(210, 11, 15, 25));
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        paint.setColor(Color.CYAN);
        paint.setTextSize(56f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME PAUSED", screenWidth / 2f, screenHeight / 2f - 100, paint);

        paint.setColor(Color.parseColor("#0284C7"));
        RectF resBtn = new RectF(screenWidth / 2f - 150, screenHeight / 2f - 40, screenWidth / 2f + 150, screenHeight / 2f + 20);
        canvas.drawRoundRect(resBtn, 22f, 22f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(34f);
        canvas.drawText("RESUME", screenWidth / 2f, screenHeight / 2f, paint);

        paint.setColor(Color.parseColor("#475569"));
        RectF menuBtn = new RectF(screenWidth / 2f - 150, screenHeight / 2f + 50, screenWidth / 2f + 150, screenHeight / 2f + 110);
        canvas.drawRoundRect(menuBtn, 22f, 22f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("MAIN MENU", screenWidth / 2f, screenHeight / 2f + 90, paint);
    }

    private void drawGameOverOverlay(Canvas canvas) {
        paint.setColor(Color.argb(220, 11, 15, 25));
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);

        paint.setColor(Color.parseColor("#DC2626"));
        RectF overBanner = new RectF(40, screenHeight * 0.20f, screenWidth - 40, screenHeight * 0.33f);
        canvas.drawRoundRect(overBanner, 26f, 26f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(52f);
        paint.setFakeBoldText(true);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("MISSION FAILED!", screenWidth / 2f, screenHeight * 0.27f, paint);

        paint.setColor(Color.parseColor("#1E293B"));
        RectF statsBox = new RectF(60, screenHeight * 0.36f, screenWidth - 60, screenHeight * 0.52f);
        canvas.drawRoundRect(statsBox, 18f, 18f, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(34f);
        canvas.drawText("FINAL SCORE: " + score, screenWidth / 2f, screenHeight * 0.42f, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawText("CRYSTALS: " + crystals, screenWidth / 2f, screenHeight * 0.47f, paint);

        paint.setColor(Color.parseColor("#22C55E"));
        RectF retryBtn = new RectF(screenWidth / 2f - 150, screenHeight / 2f + 80, screenWidth / 2f + 150, screenHeight / 2f + 150);
        canvas.drawRoundRect(retryBtn, 26f, 26f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(38f);
        canvas.drawText("PLAY AGAIN", screenWidth / 2f, screenHeight / 2f + 128, paint);

        paint.setColor(Color.parseColor("#64748B"));
        RectF menuBtn = new RectF(screenWidth / 2f - 150, screenHeight / 2f + 170, screenWidth / 2f + 150, screenHeight / 2f + 230);
        canvas.drawRoundRect(menuBtn, 26f, 26f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(34f);
        canvas.drawText("MAIN MENU", screenWidth / 2f, screenHeight / 2f + 210, paint);
    }

    public void releaseResources() {
        if (soundManager != null) {
            soundManager.release();
        }
    }

    @Override
    public void run() {
        // Driven by GameThread
    }
}
