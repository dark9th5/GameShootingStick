package com.example.mygame1.world

import GameState
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.InputListener
import ktx.assets.disposeSafely
import com.example.mygame1.entities.Player
import com.example.mygame1.entities.Enemy
import com.example.mygame1.input.InputHandler
import com.example.mygame1.entities.Bullet

class World(
    val stage: Stage,                  // Truyền vào Stage UI
    val skin: Skin                     // Truyền vào skin cho các button
) {

    private val map: TiledMap = TmxMapLoader().load("map/sampleMap.tmx")
    val tileLayer = map.layers.get("Object") as TiledMapTileLayer

    private val renderer = OrthogonalTiledMapRenderer(map)
    val camera = OrthographicCamera()
    val player = Player()
    val enemies = mutableListOf<Enemy>()
    private val starField = StarField(400)

    // Thêm nút đổi súng
    private val swapWeaponButton: TextButton

    init {
        camera.setToOrtho(false)
        camera.zoom = 0.5f
        camera.update()

        val mapW = getMapWidth()
        val mapH = getMapHeight()
        for (i in 0 until 7) {
            val spawnPos = Vector2(
                mapW * 0.1f + i * (mapW * 0.8f / 6),
                mapH * 0.8f
            )
            enemies.add(
                Enemy(
                    characterIndex = player.characterIndex,
                    weaponIndex = player.weaponIndex,
                    spawnPosition = spawnPos
                )
            )
        }

        // Tạo nút swapWeaponButton
        swapWeaponButton = TextButton("Đổi súng", skin)
        swapWeaponButton.setSize(120f, 60f)
        swapWeaponButton.setPosition(1700f, 200f) // Điều chỉnh vị trí theo UI của bạn
        swapWeaponButton.addListener(object : InputListener() {
            override fun touchDown(
                event: InputEvent?,
                x: Float, y: Float, pointer: Int, button: Int
            ): Boolean {
                val nextIndex = (player.weaponIndex + 1) % player.weapons.size
                player.selectWeapon(
                    nextIndex,
                    enemyPosition = player.position,
                    bulletsOnMap = player.bullets
                )
                return true
            }
        })
        stage.addActor(swapWeaponButton)
    }

    fun update(delta: Float, touchpad: Touchpad) {
        val input = InputHandler(touchpad)

        val dx = input.dx
        val dy = input.dy
        if (dx != 0f || dy != 0f) {
            val angle = com.badlogic.gdx.math.MathUtils.atan2(dy, dx)
            val vx = com.badlogic.gdx.math.MathUtils.cos(angle) * player.speed * delta
            val vy = com.badlogic.gdx.math.MathUtils.sin(angle) * player.speed * delta

            val newX = (player.position.x + vx).coerceIn(0f, getMapWidth() - player.sprite.width)
            val newY = (player.position.y + vy).coerceIn(0f, getMapHeight() - player.sprite.height)
            val newRect = Rectangle(newX, newY, player.sprite.width, player.sprite.height)
            if (!isBlocked(newRect)) {
                player.position.x = newX
                player.position.y = newY
                player.sprite.rotation = angle * com.badlogic.gdx.math.MathUtils.radiansToDegrees
            }
            player.sprite.setPosition(player.position.x, player.position.y)
        }

        player.update(
            delta,
            input,
            enemyPosition = enemies.firstOrNull()?.position ?: player.position,
            bulletsOnMap = player.bullets + enemies.flatMap { it.bullets },
            mapWidth = getMapWidth(),
            mapHeight = getMapHeight()
        )
        starField.update(delta, getMapWidth(), getMapHeight())

        enemies.forEach { enemy ->
            val state = GameState(
                enemyPosition = enemy.position,
                playerPosition = player.position.cpy(),
                bullets = player.bullets + enemies.flatMap { it.bullets }
            )
            val action = enemy.ai.decideAction(state)
            if (action is EnemyAction.Move) {
                val dir = action.direction.nor()
                val vx = dir.x * enemy.speed * delta
                val vy = dir.y * enemy.speed * delta
                val newX = (enemy.position.x + vx).coerceIn(0f, getMapWidth() - enemy.sprite.width)
                val newY = (enemy.position.y + vy).coerceIn(0f, getMapHeight() - enemy.sprite.height)
                val newRect = Rectangle(newX, newY, enemy.sprite.width, enemy.sprite.height)
                if (!isBlocked(newRect)) {
                    enemy.position.x = newX
                    enemy.position.y = newY
                    enemy.sprite.rotation = dir.angleDeg()
                }
                enemy.sprite.setPosition(enemy.position.x, enemy.position.y)
            }
            enemy.update(
                delta,
                state = state,
                mapWidth = getMapWidth(),
                mapHeight = getMapHeight()
            )
        }

        handleCollisions()

        camera.position.set(
            player.position.x + player.sprite.width / 2,
            player.position.y + player.sprite.height / 2,
            0f
        )
        camera.update()
    }

    fun render(batch: SpriteBatch, font: BitmapFont, blankTexture: Texture) {
        batch.projectionMatrix = camera.combined
        starField.render(batch)

        renderer.setView(camera)
        renderer.render()

        player.render(batch)
        enemies.forEach { enemy -> enemy.render(batch, font) }
    }

    fun dispose() {
        map.disposeSafely()
        renderer.disposeSafely()
        player.dispose()
        enemies.forEach { it.dispose() }
        starField.dispose()
    }

    private fun getMapWidth(): Float =
        map.properties.get("width", Int::class.java) * map.properties.get("tilewidth", Int::class.java).toFloat()

    private fun getMapHeight(): Float =
        map.properties.get("height", Int::class.java) * map.properties.get("tileheight", Int::class.java).toFloat()

    private fun isBlocked(rect: Rectangle): Boolean {
        val tileWidth = tileLayer.tileWidth
        val tileHeight = tileLayer.tileHeight

        val minTileX = (rect.x / tileWidth).toInt()
        val maxTileX = ((rect.x + rect.width) / tileWidth).toInt()
        val minTileY = (rect.y / tileHeight).toInt()
        val maxTileY = ((rect.y + rect.height) / tileHeight).toInt()

        for (tx in minTileX..maxTileX) {
            for (ty in minTileY..maxTileY) {
                val cell = tileLayer.getCell(tx, ty)
                val tile = cell?.tile
                val collision = tile?.properties?.get("Collision") as? String
                if (collision == "true") {
                    return true
                }
            }
        }
        return false
    }

    private fun handleCollisions() {
        for (bullet in player.bullets) {
            if (!bullet.isActive) continue
            for (enemy in enemies) {
                if (enemy.isDead()) continue
                if (bullet.bounds().overlaps(enemy.sprite.boundingRectangle)) {
                    enemy.takeDamage(bullet.damage)
                    bullet.isActive = false
                    break
                }
            }
        }
        player.bullets.removeAll { !it.isActive }

        for (enemy in enemies) {
            for (bullet in enemy.bullets) {
                if (!bullet.isActive) continue
                if (bullet.bounds().overlaps(player.sprite.boundingRectangle)) {
                    player.takeDamage(bullet.damage)
                    bullet.isActive = false
                }
            }
            enemy.bullets.removeAll { !it.isActive }
        }

        enemies.removeAll { it.isDead() }
    }
}
