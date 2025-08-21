package com.example.mygame1

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
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

    // Star Field variables - TỐC ĐỘ CỐ ĐỊNH NHƯ CŨ
    private val starCount = 400
    private val stars = mutableListOf<Star>()
    private lateinit var starTexture: Texture

    data class Star(
        var position: Vector2,
        var velocity: Vector2,
        val size: Float,
        val brightness: Float,
        var rotation: Float,
        val rotationSpeed: Float
    )

    init {
        camera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.zoom = 0.5f
        camera.update()

        setupTouchpad()
        setupStarField()

        // Cấu hình input
        inputMultiplexer.addProcessor(stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun setupTouchpad() {
        val skin = Skin()
        val backgroundTexture = Texture("control/joystick_circle_pad_c.png")
        val knobTexture = Texture("control/joystick_circle_nub_b.png")

        skin.add("touchBackground", backgroundTexture)
        skin.add("touchKnob", knobTexture)

        val touchpadStyle = TouchpadStyle().apply {
            background = skin.getDrawable("touchBackground")
            knob = skin.getDrawable("touchKnob")
        }

        // JOYSTICK TO GẤP ĐÔI VÀ CÁCH XA VIỀN GẤP ĐÔI
        val touchpadSize = 300f // Gấp đôi 150f
        val margin = 100f // Gấp đôi 50f

        touchpad = Touchpad(10f, touchpadStyle)
        touchpad.setBounds(margin, margin, touchpadSize, touchpadSize)
        stage.addActor(touchpad)
    }

    private fun setupStarField() {
        // Tạo texture cho sao
        val pixmap = com.badlogic.gdx.graphics.Pixmap(8, 8, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)

        for (i in 0 until 8) {
            for (j in 0 until 8) {
                if (MathUtils.randomBoolean(0.7f)) {
                    pixmap.drawPixel(i, j)
                }
            }
        }
        starTexture = Texture(pixmap)
        pixmap.dispose()

        val mapWidth = getMapWidth()
        val mapHeight = getMapHeight()

        repeat(starCount) {
            val posX = MathUtils.random(-200f, mapWidth + 200f)
            val posY = MathUtils.random(-200f, mapHeight + 200f)

            val angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians
            // TỐC ĐỘ CỐ ĐỊNH NHƯ CŨ (1-3 thay vì 1-6)
            val speed = MathUtils.random(1f, 3f)

            stars.add(Star(
                position = Vector2(posX, posY),
                velocity = Vector2(
                    MathUtils.cos(angle) * speed,
                    MathUtils.sin(angle) * speed
                ),
                size = MathUtils.random(1.5f, 8f),
                brightness = MathUtils.random(0.3f, 1f),
                rotation = MathUtils.random(0f, 360f),
                rotationSpeed = MathUtils.random(-2f, 2f)
            ))
        }
    }

    private fun getMapWidth(): Float {
        return map.properties.get("width", Int::class.java) * map.properties.get("tilewidth", Int::class.java).toFloat()
    }

    private fun getMapHeight(): Float {
        return map.properties.get("height", Int::class.java) * map.properties.get("tileheight", Int::class.java).toFloat()
    }

    private fun updateStars(delta: Float) {
        val mapWidth = getMapWidth()
        val mapHeight = getMapHeight()
        val margin = 300f

        stars.forEach { star ->
            // TỐC ĐỘ CỐ ĐỊNH NHƯ CŨ (80f thay vì 60f)
            star.position.x += star.velocity.x * delta * 80f
            star.position.y += star.velocity.y * delta * 80f

            star.rotation += star.rotationSpeed * delta * 30f
            if (star.rotation > 360f) star.rotation -= 360f
            if (star.rotation < 0f) star.rotation += 360f

            // Thỉnh thoảng thay đổi hướng bay ngẫu nhiên
            if (MathUtils.random(100) < 5) {
                val angle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians
                // Giữ nguyên tốc độ, chỉ thay đổi hướng
                star.velocity.set(
                    MathUtils.cos(angle) * star.velocity.len(),
                    MathUtils.sin(angle) * star.velocity.len()
                )
            }

            // Reset sao nếu bay quá xa
            if (star.position.x < -margin || star.position.x > mapWidth + margin ||
                star.position.y < -margin || star.position.y > mapHeight + margin) {

                if (star.position.x < -margin) star.position.x = mapWidth + MathUtils.random(0f, 100f)
                else if (star.position.x > mapWidth + margin) star.position.x = -MathUtils.random(0f, 100f)
                else if (star.position.y < -margin) star.position.y = mapHeight + MathUtils.random(0f, 100f)
                else if (star.position.y > mapHeight + margin) star.position.y = -MathUtils.random(0f, 100f)

                else {
                    star.position.set(
                        MathUtils.random(-100f, mapWidth + 100f),
                        MathUtils.random(-100f, mapHeight + 100f)
                    )
                }

                // Đôi khi thay đổi hướng bay khi reset
                if (MathUtils.randomBoolean(0.3f)) {
                    val newAngle = MathUtils.random(0f, 360f) * MathUtils.degreesToRadians
                    // TỐC ĐỘ CỐ ĐỊNH NHƯ CŨ
                    star.velocity.set(
                        MathUtils.cos(newAngle) * MathUtils.random(1f, 3f),
                        MathUtils.sin(newAngle) * MathUtils.random(1f, 3f)
                    )
                }
            }
        }
    }

    private fun renderStars() {
        batch.use {
            stars.forEach { star ->
                val color = Color(star.brightness, star.brightness, star.brightness, 1f)
                it.color = color

                it.draw(
                    starTexture,
                    star.position.x - star.size/2,
                    star.position.y - star.size/2,
                    star.size/2,
                    star.size/2,
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

    override fun render(delta: Float) {
        clearScreen(0.06f, 0.06f, 0.1f)

        // Cập nhật các ngôi sao
        updateStars(delta)

        // Render các ngôi sao
        batch.projectionMatrix = camera.combined
        renderStars()

        // Cập nhật camera theo nhân vật
        camera.position.set(player.position.x + player.sprite.width / 2, player.position.y + player.sprite.height / 2, 0f)
        camera.update()

        // Thiết lập view cho renderer
        renderer.setView(camera)
        renderer.render()

        // Render player
        batch.use {
            player.sprite.draw(it)
        }

        // Update player theo touchpad
        player.update(delta, touchpad, mapWidth = getMapWidth(), mapHeight = getMapHeight())

        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        batch.disposeSafely()
        map.disposeSafely()
        renderer.disposeSafely()
        player.dispose()
        stage.dispose()
        starTexture.disposeSafely()
    }
}
