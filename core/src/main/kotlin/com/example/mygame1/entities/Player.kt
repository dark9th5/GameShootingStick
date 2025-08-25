package com.example.mygame1.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.example.mygame1.input.InputHandler
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile

class Player {

    // Sprite nhân vật
    private val texture = Texture("character/Characters/green_character.png".toInternalFile())
    val sprite = Sprite(texture)
    var position = Vector2(100f, 100f)

    var health: Int = 100
        private set

    private val speed = 100f

    /**
     * Update vị trí nhân vật dựa trên Touchpad
     */
    fun update(
        delta: Float,
        input: InputHandler,
        mapWidth: Float = 800f,
        mapHeight: Float = 600f
    ) {
        val dx = input.dx
        val dy = input.dy

        if (dx != 0f || dy != 0f) {
            val angle = MathUtils.atan2(dy, dx)
            val vx = MathUtils.cos(angle) * speed * delta
            val vy = MathUtils.sin(angle) * speed * delta

            position.x = (position.x + vx).coerceIn(0f, mapWidth - sprite.width)
            position.y = (position.y + vy).coerceIn(0f, mapHeight - sprite.height)

            sprite.rotation = angle * MathUtils.radiansToDegrees
        }

        sprite.setPosition(position.x, position.y)
    }

    fun takeDamage(amount: Int) {
        health = (health - amount).coerceAtLeast(0)
    }
    fun isDead(): Boolean = health <= 0
    fun dispose() {
        texture.disposeSafely()
    }
}
