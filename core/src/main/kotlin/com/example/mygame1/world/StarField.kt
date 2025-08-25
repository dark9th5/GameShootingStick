package com.example.mygame1.world

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import ktx.assets.disposeSafely
import ktx.graphics.use

class StarField(
    private val starCount: Int = 400
) {
    private val stars = mutableListOf<Star>()
    private val starTexture: Texture

    data class Star(
        var position: Vector2,
        var velocity: Vector2,
        var size: Float,
        var brightness: Float,
        var rotation: Float,
        var rotationSpeed: Float
    )

    init {
        val pixmap = Pixmap(8, 8, Pixmap.Format.RGBA8888).apply {
            setColor(Color.WHITE)
            for (i in 0 until 8) {
                for (j in 0 until 8) {
                    if (MathUtils.randomBoolean(0.7f)) drawPixel(i, j)
                }
            }
        }
        starTexture = Texture(pixmap)
        pixmap.dispose()
    }

    fun update(delta: Float, mapWidth: Float, mapHeight: Float) {
        val margin = 300f

        // Nếu danh sách rỗng thì tạo mới
        if (stars.isEmpty()) {
            repeat(starCount) {
                stars.add(createRandomStar(mapWidth, mapHeight))
            }
        }

        // Cập nhật vị trí, reset sao nếu bay quá xa
        stars.forEach { star ->
            star.position.add(star.velocity.cpy().scl(delta * 80f))
            star.rotation = (star.rotation + star.rotationSpeed * delta * 30f) % 360

            if (star.position.x < -margin || star.position.x > mapWidth + margin ||
                star.position.y < -margin || star.position.y > mapHeight + margin) {
                resetStar(star, mapWidth, mapHeight)
            }
        }
    }

    private fun createRandomStar(mapWidth: Float, mapHeight: Float): Star {
        val posX = MathUtils.random(0f, mapWidth)
        val posY = MathUtils.random(0f, mapHeight)
        val angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians
        val speed = MathUtils.random(1f, 3f)
        return Star(
            position = Vector2(posX, posY),
            velocity = Vector2(MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed),
            size = MathUtils.random(1.5f, 8f),
            brightness = MathUtils.random(0.3f, 1f),
            rotation = MathUtils.random(0f, 360f),
            rotationSpeed = MathUtils.random(-2f, 2f)
        )
    }

    private fun resetStar(star: Star, mapWidth: Float, mapHeight: Float) {
        val posX = MathUtils.random(0f, mapWidth)
        val posY = MathUtils.random(0f, mapHeight)
        val angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians
        val speed = MathUtils.random(1f, 3f)

        star.position.set(posX, posY)
        star.velocity.set(MathUtils.cos(angle) * speed, MathUtils.sin(angle) * speed)
        star.size = MathUtils.random(1.5f, 8f)
        star.brightness = MathUtils.random(0.3f, 1f)
        star.rotation = MathUtils.random(0f, 360f)
        star.rotationSpeed = MathUtils.random(-2f, 2f)
    }

    fun render(batch: SpriteBatch) {
        batch.use {
            for (star in stars) {
                val color = Color(star.brightness, star.brightness, star.brightness, 1f)
                it.color = color
                it.draw(
                    starTexture,
                    star.position.x - star.size / 2,
                    star.position.y - star.size / 2,
                    star.size / 2,
                    star.size / 2,
                    star.size,
                    star.size,
                    1f,
                    1f,
                    star.rotation,
                    0,
                    0,
                    starTexture.width,
                    starTexture.height,
                    false,
                    false
                )
            }
            it.color = Color.WHITE
        }
    }

    fun dispose() {
        starTexture.disposeSafely()
    }
}
