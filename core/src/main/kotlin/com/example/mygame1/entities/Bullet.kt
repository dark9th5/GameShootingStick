package com.example.mygame1.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

enum class BulletOwner {
    PLAYER, ENEMY
}

sealed class BulletType(val assetPath: String) {
    data object Gun : BulletType("character/Characters/green_hand.png")
    data object Machine : BulletType("character/Characters/red_hand.png")
    data object Silencer : BulletType("character/Characters/yellow_hand.png")
    companion object {
        fun values(): Array<BulletType> = arrayOf(Gun, Machine, Silencer)
        fun valueOf(value: String): BulletType = when (value) {
            "Gun" -> Gun
            "Machine" -> Machine
            "Silencer" -> Silencer
            else -> throw IllegalArgumentException("No object com.example.mygame1.entities.BulletType.$value")
        }
    }
}

class Bullet(
    val type: BulletType,
    val position: Vector2,
    val direction: Vector2,
    val owner: BulletOwner,
    val speed: Float = 200f,
    val maxDistance: Float = 600f,
    val size: Float = 8f,
    val damage: Int = 10
) {
    private val texture = Texture(type.assetPath)
    private val sprite = Sprite(texture).apply { setOriginCenter() }
    private val velocity = direction.nor().scl(speed)

    var isActive = true

    private var traveledDistance = 0f
    private var lastPosition = position.cpy()

    init {
        updateSpritePosition()
    }

    // Sửa lại để viên đạn thành hình bầu dục (kéo dãn chiều ngang gấp đôi)
    private fun updateSpritePosition() {
        sprite.setSize(size * 2f, size) // Chiều ngang gấp đôi chiều dọc
        sprite.setPosition(
            position.x - sprite.width / 2f,
            position.y - sprite.height / 2f
        )
        sprite.rotation = direction.angleDeg()
    }

    fun bounds(): Rectangle {
        return Rectangle(
            position.x - sprite.width / 2f,
            position.y - sprite.height / 2f,
            sprite.width,
            sprite.height
        )
    }

    fun update(delta: Float) {
        if (!isActive) return
        position.add(velocity.x * delta, velocity.y * delta)
        updateSpritePosition()

        traveledDistance += lastPosition.dst(position)
        lastPosition.set(position)
        if (traveledDistance > maxDistance) {
            isActive = false
        }
    }

    fun render(batch: SpriteBatch) {
        if (isActive) {
            updateSpritePosition()
            sprite.draw(batch)
        }
    }

    fun dispose() {
        texture.dispose()
    }
}
