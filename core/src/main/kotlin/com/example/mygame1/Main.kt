package com.example.mygame1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle
import com.badlogic.gdx.scenes.scene2d.utils.Drawable
import com.example.mygame1.entities.Player
import ktx.app.KtxScreen
import ktx.app.KtxGame
import ktx.app.clearScreen
import ktx.assets.disposeSafely
import ktx.graphics.use
import com.badlogic.gdx.InputMultiplexer

class Main : KtxGame<KtxScreen>() {
    override fun create() {
        addScreen(FirstScreen())
        setScreen<FirstScreen>()
    }
}

class FirstScreen : KtxScreen {
    private val batch = SpriteBatch()
    private val mapLoader = TmxMapLoader()
    private val map = mapLoader.load("map/sampleMap.tmx")
    private val renderer = OrthogonalTiledMapRenderer(map)
    private val player = Player()
    private val stage = Stage()
    private val inputMultiplexer = InputMultiplexer()
    private val camera = OrthographicCamera()

    // Touchpad
    private lateinit var touchpad: Touchpad

    init {
        camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.zoom = 0.5f
        camera.update()

        setupTouchpad()

        // Cấu hình input
        inputMultiplexer.addProcessor(stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun setupTouchpad() {
        // Skin mặc định cho touchpad
        val skin = Skin()
        skin.add("touchBackground", Texture("control/joystick_circle_pad_c.png"))
        skin.add("touchKnob", Texture("control/joystick_circle_nub_b.png"))

        val touchpadStyle = TouchpadStyle().apply {
            background = skin.getDrawable("touchBackground")
            knob = skin.getDrawable("touchKnob")
        }

        touchpad = Touchpad(10f, Touchpad.TouchpadStyle().apply {
            background = skin.getDrawable("touchBackground")
            knob = skin.getDrawable("touchKnob")
        })
        touchpad.setBounds(50f, 50f, 150f, 150f)
        stage.addActor(touchpad)

    }

    override fun render(delta: Float) {
        clearScreen(0.7f, 0.7f, 0.7f)

        // Cập nhật camera theo nhân vật
        camera.position.set(player.position.x + player.sprite.width / 2, player.position.y + player.sprite.height / 2, 0f)
        camera.update()

        // Thiết lập view cho renderer
        renderer.setView(camera)
        renderer.render()

        // Cập nhật projection matrix
        batch.projectionMatrix = camera.combined
        batch.use {
            player.sprite.draw(it)
        }

        // Update player theo touchpad
        player.update(delta, touchpad, mapWidth = map.properties.get("width", Int::class.java) * map.properties.get("tilewidth", Int::class.java).toFloat(),
            mapHeight = map.properties.get("height", Int::class.java) * map.properties.get("tileheight", Int::class.java).toFloat())

        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        batch.disposeSafely()
        map.disposeSafely()
        renderer.disposeSafely()
        player.dispose()
        stage.dispose()
    }
}
