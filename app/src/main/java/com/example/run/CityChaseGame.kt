package com.example.run

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class CityChaseGame(context: Context) : View(context) {

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private var scaleX = 1f
    private var scaleY = 1f

    private var lane = 1
    private var score = 0
    private var coins = 0
    private var speed = 6f
    private var frames = 0
    private var roadScroll = 0f

    private var jumpTimer = 0
    private var slideTimer = 0
    private var gameOver = false

    private var startX = 0f
    private var startY = 0f

    private data class Obstacle(
        var lane: Int,
        var y: Float,
        val type: Int
    )

    private data class CoinItem(
        var lane: Int,
        var y: Float,
        var taken: Boolean = false
    )

    private val obstacles = mutableListOf<Obstacle>()
    private val coinList = mutableListOf<CoinItem>()

    private val sky = Color.rgb(105, 190, 235)
    private val road = Color.rgb(48, 53, 64)
    private val white = Color.WHITE
    private val gold = Color.rgb(255, 195, 35)

    private fun color(c: Int) {
        p.color = c
        p.style = Paint.Style.FILL
    }

    private fun rect(
        c: Canvas, x: Float, y: Float,
        w: Float, h: Float, col: Int
    ) {
        color(col)
        c.drawRect(x, y, x + w, y + h, p)
    }

    private fun circle(
        c: Canvas, x: Float, y: Float,
        r: Float, col: Int
    ) {
        color(col)
        c.drawCircle(x, y, r, p)
    }

    private fun text(
        c: Canvas, value: String,
        x: Float, y: Float,
        size: Float, col: Int,
        bold: Boolean = true
    ) {
        color(col)
        p.textSize = size
        p.typeface = if (bold) {
            Typeface.DEFAULT_BOLD
        } else {
            Typeface.DEFAULT
        }
        c.drawText(value, x, y, p)
    }

    private fun laneX(l: Int): Float = 60f + l * 120f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        scaleX = width / 360f
        scaleY = height / 640f

        val save = canvas.save()
        canvas.scale(scaleX, scaleY)

        drawWorld(canvas)

        if (!gameOver) {
            updateGame()
            drawEntities(canvas)
            drawPlayer(canvas)
            drawHUD(canvas)
            postInvalidateDelayed(16)
        } else {
            drawEntities(canvas)
            drawPlayer(canvas)
            drawHUD(canvas)
            drawGameOver(canvas)
        }

        canvas.restoreToCount(save)
    }

    private fun drawWorld(c: Canvas) {
        c.drawColor(sky)

        // Distant city buildings
        for (i in 0..8) {
            val x = i * 45f
            val h = 65f + (i % 3) * 22f

            rect(c, x, 210f - h, 36f, h + 210f - h,
                Color.rgb(75 + i * 3, 105, 140))

            for (r in 0..2) {
                for (col in 0..1) {
                    rect(c, x + 7 + col * 15f,
                        165f - h + r * 20f,
                        6f, 8f, Color.rgb(255, 220, 120))
                }
            }
        }

        // Road
        val path = Path()
        path.moveTo(15f, 640f)
        path.lineTo(80f, 180f)
        path.lineTo(280f, 180f)
        path.lineTo(345f, 640f)
        path.close()

        color(road)
        c.drawPath(path, p)

        // Roadside grass
        rect(c, 0f, 220f, 20f, 420f, Color.rgb(48, 135, 75))
        rect(c, 340f, 220f, 20f, 420f, Color.rgb(48, 135, 75))

        // Road edges
        color(white)
        p.strokeWidth = 3f
        c.drawLine(80f, 180f, 15f, 640f, p)
        c.drawLine(280f, 180f, 345f, 640f, p)

        // Moving lane markings
        val offset = roadScroll % 100f

        for (i in 0..7) {
            val y = 200f + i * 65f + offset
            val perspective = (y - 180f) / 460f
            val x1 = 80f + perspective * 65f
            val x2 = 280f - perspective * 65f

            rect(c, x1 + (x2 - x1) / 3f - 2f,
                y, 4f, 28f * perspective + 4f, white)

            rect(c, x1 + (x2 - x1) * 2f / 3f - 2f,
                y, 4f, 28f * perspective + 4f, white)
        }

        // Roadside lamps
        for (i in 0..3) {
            val y = 240f + i * 120f + offset
            val h = 40f + i * 10f
            rect(c, 24f, y - h, 4f, h, Color.DKGRAY)
            circle(c, 26f, y - h, 6f, gold)

            rect(c, 332f, y - h, 4f, h, Color.DKGRAY)
            circle(c, 334f, y - h, 6f, gold)
        }
    }

    private fun updateGame() {
        frames++
        roadScroll += speed

        if (frames % 5 == 0) score += 1
        speed = (6f + score / 350f).coerceAtMost(13f)

        if (frames % 48 == 0) {
            val l = Random.nextInt(0, 3)
            obstacles.add(
                Obstacle(l, 185f, Random.nextInt(0, 2))
            )
        }

        if (frames % 24 == 0) {
            coinList.add(
                CoinItem(Random.nextInt(0, 3), 185f)
            )
        }

        if (jumpTimer > 0) jumpTimer--
        if (slideTimer > 0) slideTimer--

        val iterator = obstacles.iterator()

        while (iterator.hasNext()) {
            val o = iterator.next()
            o.y += speed

            if (o.y > 700f) {
                iterator.remove()
                continue
            }

            if (o.lane == lane &&
                abs(o.y - 505f) < 38f &&
                jumpTimer == 0 && slideTimer == 0
            ) {
                gameOver = true
            }
        }

        val coinIterator = coinList.iterator()

        while (coinIterator.hasNext()) {
            val coin = coinIterator.next()
            coin.y += speed

            if (coin.lane == lane &&
                abs(coin.y - 500f) < 35f &&
                !coin.taken
            ) {
                coin.taken = true
                coins++
                score += 25
            }

            if (coin.y > 700f || coin.taken) {
                coinIterator.remove()
            }
        }
    }

    private fun drawEntities(c: Canvas) {
        for (o in obstacles) {
            val x = laneX(o.lane)
            val size = (28f + o.y / 35f).coerceAtMost(45f)

            if (o.type == 0) {
                // Wooden road barrier
                rect(c, x - size, o.y, size * 2f, size,
                    Color.rgb(220, 90, 40))
                rect(c, x - size, o.y + size / 3f,
                    size * 2f, 7f, white)
                rect(c, x - size, o.y + size,
                    6f, 12f, Color.DKGRAY)
                rect(c, x + size - 6f, o.y + size,
                    6f, 12f, Color.DKGRAY)
            } else {
                // Traffic cone
                val path = Path()
                path.moveTo(x, o.y)
                path.lineTo(x - size, o.y + size)
                path.lineTo(x + size, o.y + size)
                path.close()

                color(Color.rgb(255, 125, 25))
                c.drawPath(path, p)

                rect(c, x - size, o.y + size - 5f,
                    size * 2f, 7f, white)
            }
        }

        for (coin in coinList) {
            val x = laneX(coin.lane)

            circle(c, x, coin.y, 13f, Color.rgb(190, 125, 0))
            circle(c, x, coin.y, 10f, gold)
            text(c, "$", x - 4f, coin.y + 5f, 14f,
                Color.rgb(150, 90, 0))
        }
    }

    private fun drawPlayer(c: Canvas) {
        val x = laneX(lane)
        val jumpHeight = if (jumpTimer > 0) {
            100f * jumpTimer / 30f
        } else 0f

        val y = 505f - jumpHeight
        val sliding = slideTimer > 0

        // Shadow
        circle(c, x, 548f, 23f, Color.rgb(35, 40, 45))

        if (sliding) {
            // Sliding body
            rect(c, x - 25f, y - 15f, 45f, 20f,
                Color.rgb(35, 110, 235))
            circle(c, x + 23f, y - 15f, 12f,
                Color.rgb(240, 180, 130))
        } else {
            // Legs
            rect(c, x - 12f, y + 5f, 9f, 28f,
                Color.rgb(30, 35, 65))
            rect(c, x + 4f, y + 5f, 9f, 28f,
                Color.rgb(30, 35, 65))

            // Shoes
            rect(c, x - 17f, y + 28f, 17f, 7f,
                Color.BLACK)
            rect(c, x + 2f, y + 28f, 17f, 7f,
                Color.BLACK)

            // Body
            rect(c, x - 17f, y - 24f, 34f, 34f,
                Color.rgb(30, 125, 245))

            // Arms
            rect(c, x - 25f, y - 20f, 8f, 25f,
                Color.rgb(240, 180, 130))
            rect(c, x + 17f, y - 20f, 8f, 25f,
                Color.rgb(240, 180, 130))

            // Head and hair
            circle(c, x, y - 36f, 15f,
                Color.rgb(240, 180, 130))
            circle(c, x, y - 46f, 14f,
                Color.rgb(65, 35, 20))

            // Eyes
            circle(c, x - 5f, y - 36f, 2f, Color.BLACK)
            circle(c, x + 5f, y - 36f, 2f, Color.BLACK)
        }

        // Police officer chasing behind
        val chaseY = 590f + sin(frames / 5f) * 3f

        circle(c, 110f, chaseY - 32f, 12f,
            Color.rgb(240, 180, 130))
        rect(c, 97f, chaseY - 20f, 26f, 35f,
            Color.rgb(25, 65, 155))
        rect(c, 101f, chaseY + 15f, 7f, 18f,
            Color.DKGRAY)
        rect(c, 113f, chaseY + 15f, 7f, 18f,
            Color.DKGRAY)
        rect(c, 98f, chaseY - 47f, 24f, 8f,
            Color.rgb(25, 65, 155))
        rect(c, 104f, chaseY - 53f, 12f, 7f,
            Color.rgb(220, 40, 40))

        // Small chasing dog
        circle(c, 148f, chaseY + 2f, 12f,
            Color.rgb(155, 100, 55))
        circle(c, 160f, chaseY - 5f, 9f,
            Color.rgb(180, 125, 75))
        circle(c, 165f, chaseY - 7f, 2f, Color.BLACK)
        circle(c, 156f, chaseY - 13f, 5f,
            Color.rgb(115, 70, 40))
        rect(c, 141f, chaseY + 10f, 5f, 12f,
            Color.rgb(95, 60, 35))
        rect(c, 152f, chaseY + 10f, 5f, 12f,
            Color.rgb(95, 60, 35))
    }

    private fun drawHUD(c: Canvas) {
        // Top score panel
        color(Color.rgb(25, 35, 55))
        c.drawRoundRect(12f, 12f, 180f, 85f,
            16f, 16f, p)

        text(c, "CITY CHASE", 24f, 36f, 15f, gold)
        text(c, "Score: $score", 24f, 60f, 17f, white)
        text(c, "Coins: $coins", 24f, 80f, 15f, gold)

        // Speed indicator
        color(Color.rgb(25, 35, 55))
        c.drawRoundRect(240f, 15f, 348f, 48f,
            12f, 12f, p)
        text(c, "SPEED ${speed.toInt()}", 253f, 36f,
            14f, white)

        // Swipe hint
        text(c, "SWIPE TO MOVE  •  UP TO JUMP",
            50f, 625f, 13f, white)
    }

    private fun drawGameOver(c: Canvas) {
        color(Color.argb(190, 8, 15, 30))
        c.drawRect(0f, 0f, 360f, 640f, p)

        color(Color.rgb(25, 35, 55))
        c.drawRoundRect(25f, 190f, 335f, 455f,
            24f, 24f, p)

        text(c, "CHASE OVER!", 70f, 245f, 28f,
            Color.rgb(255, 100, 80))
        text(c, "Score: $score", 105f, 295f,
            22f, white)
        text(c, "Coins: $coins", 105f, 330f,
            20f, gold)

        color(Color.rgb(30, 150, 100))
        c.drawRoundRect(65f, 360f, 295f, 415f,
            18f, 18f, p)
        text(c, "TAP TO PLAY AGAIN", 83f, 393f,
            17f, white)
    }

    private fun restartGame() {
        lane = 1
        score = 0
        coins = 0
        speed = 6f
        frames = 0
        roadScroll = 0f
        jumpTimer = 0
        slideTimer = 0
        obstacles.clear()
        coinList.clear()
        gameOver = false
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x / scaleX
        val y = event.y / scaleY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = x
                startY = y
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (gameOver) {
                    restartGame()
                    return true
                }

                val dx = x - startX
                val dy = y - startY

                if (abs(dx) > abs(dy)) {
                    if (dx > 35f) lane = (lane + 1).coerceAtMost(2)
                    if (dx < -35f) lane = (lane - 1).coerceAtLeast(0)
                } else {
                    if (dy < -35f) {
                        jumpTimer = 30
                        slideTimer = 0
                    } else if (dy > 35f) {
                        slideTimer = 25
                        jumpTimer = 0
                    }
                }

                invalidate()
                return true
            }
        }

        return true
    }
}
