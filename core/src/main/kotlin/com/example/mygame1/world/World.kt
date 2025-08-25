package com.example.mygame1.world

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import ktx.assets.disposeSafely
import ktx.graphics.use
import com.example.mygame1.entities.Player
import com.example.mygame1.input.InputHandler

class World {

    private val map: TiledMap = TmxMapLoader().load("map/sampleMap.tmx")
    private val renderer = OrthogonalTiledMapRenderer(map)
    private val camera = OrthographicCamera()
    val player = Player()
    private val starField = StarField(400)


    init {
        camera.setToOrtho(false)
        camera.zoom = 0.5f
        camera.update()
    }

    fun update(delta: Float, touchpad: Touchpad) {
        val input = InputHandler(touchpad)

        player.update(delta, input, getMapWidth(), getMapHeight())
        starField.update(delta, getMapWidth(), getMapHeight())

        camera.position.set(
            player.position.x + player.sprite.width / 2,
            player.position.y + player.sprite.height / 2,
            0f
        )
        camera.update()
    }

    fun render(batch: SpriteBatch) {
        // render sao trước
        batch.projectionMatrix = camera.combined
        starField.render(batch)

        // render map
        renderer.setView(camera)
        renderer.render()

        // render player
        batch.use {
            player.sprite.draw(it)
        }
    }

    fun dispose() {
        map.disposeSafely()
        renderer.disposeSafely()
        player.dispose()
        starField.dispose()
    }

    private fun getMapWidth(): Float =
        map.properties.get("width", Int::class.java) * map.properties.get("tilewidth", Int::class.java).toFloat()

    private fun getMapHeight(): Float =
        map.properties.get("height", Int::class.java) * map.properties.get("tileheight", Int::class.java).toFloat()
}
