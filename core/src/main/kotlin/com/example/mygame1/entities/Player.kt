package com.example.mygame1.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import ktx.assets.disposeSafely
import ktx.assets.toInternalFile

class Player {

    // Sprite nhân vật
    private val texture = Texture("character/Characters/green_character.png".toInternalFile())
    val sprite = Sprite(texture)
    var position = Vector2(100f, 100f)

    private val speed = 100f

    /**
     * Update vị trí nhân vật dựa trên Touchpad
     */
    fun update(
        delta: Float,
        touchpad: Touchpad,
        mapWidth: Float = 800f,
        mapHeight: Float = 600f
    ) {
        // Lấy hướng từ touchpad (dx, dy trong khoảng -1..1)
        val dx = touchpad.knobPercentX
        val dy = touchpad.knobPercentY

        // Nếu có di chuyển
        if (dx != 0f || dy != 0f) {
            val angle = MathUtils.atan2(dy, dx)

            val vx = MathUtils.cos(angle) * speed * delta
            val vy = MathUtils.sin(angle) * speed * delta

            position.x = (position.x + vx).coerceIn(0f, mapWidth - sprite.width)
            position.y = (position.y + vy).coerceIn(0f, mapHeight - sprite.height)

            // Xoay nhân vật theo hướng
            sprite.rotation = angle * MathUtils.radiansToDegrees
        }

        // Cập nhật sprite theo position
        sprite.setPosition(position.x, position.y)
    }

    fun dispose() {
        texture.disposeSafely()
    }
}
