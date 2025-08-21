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

    // Star Field variables - ĐÃ SỬA
    private val starCount = 300 // Tăng số lượng sao
    private val stars = mutableListOf<Star>()
    private lateinit var starTexture: Texture

    data class Star(
        var x: Float,
        var y: Float,
        val size: Float,
        var speedX: Float, // Thêm di chuyển theo trục X
        var speedY: Float,
        val brightness: Float,
        var direction: Int // Hướng di chuyển: 0 = từ trên, 1 = từ dưới, 2 = từ trái, 3 = từ phải
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
        // Skin mặc định cho touchpad
        val skin = Skin()
        skin.add("touchBackground", Texture("control/joystick_circle_pad_c.png"))
        skin.add("touchKnob", Texture("control/joystick_circle_nub_b.png"))

        val touchpadStyle = TouchpadStyle().apply {
            background = skin.getDrawable("touchBackground")
            knob = skin.getDrawable("touchKnob")
        }

        touchpad = Touchpad(10f, TouchpadStyle().apply {
            background = skin.getDrawable("touchBackground")
            knob = skin.getDrawable("touchKnob")
        })
        touchpad.setBounds(50f, 50f, 150f, 150f)
        stage.addActor(touchpad)
    }

    private fun setupStarField() {
        // Tạo texture cho sao lớn hơn
        val pixmap = com.badlogic.gdx.graphics.Pixmap(4, 4, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        starTexture = Texture(pixmap)
        pixmap.dispose()

        // Khởi tạo các ngôi sao từ cả 4 hướng
        val mapWidth = getMapWidth()
        val mapHeight = getMapHeight()

        repeat(starCount) {
            val direction = MathUtils.random(0, 3) // Random từ 0-3 cho 4 hướng

            when (direction) {
                0 -> { // Từ trên xuống
                    stars.add(Star(
                        x = MathUtils.random(-100f, mapWidth + 100f),
                        y = mapHeight + MathUtils.random(0f, 200f),
                        size = MathUtils.random(2f, 6f), // Kích thước lớn hơn
                        speedX = MathUtils.random(-1f, 1f) * 0.5f, // Di chuyển ngang nhẹ
                        speedY = -MathUtils.random(1f, 3f), // Tốc độ rơi
                        brightness = MathUtils.random(0.4f, 1f),
                        direction = direction
                    ))
                }
                1 -> { // Từ dưới lên
                    stars.add(Star(
                        x = MathUtils.random(-100f, mapWidth + 100f),
                        y = -MathUtils.random(0f, 200f),
                        size = MathUtils.random(2f, 6f),
                        speedX = MathUtils.random(-1f, 1f) * 0.5f,
                        speedY = MathUtils.random(1f, 3f),
                        brightness = MathUtils.random(0.4f, 1f),
                        direction = direction
                    ))
                }
                2 -> { // Từ trái sang
                    stars.add(Star(
                        x = -MathUtils.random(0f, 200f),
                        y = MathUtils.random(-100f, mapHeight + 100f),
                        size = MathUtils.random(2f, 6f),
                        speedX = MathUtils.random(1f, 3f),
                        speedY = MathUtils.random(-1f, 1f) * 0.5f,
                        brightness = MathUtils.random(0.4f, 1f),
                        direction = direction
                    ))
                }
                3 -> { // Từ phải sang
                    stars.add(Star(
                        x = mapWidth + MathUtils.random(0f, 200f),
                        y = MathUtils.random(-100f, mapHeight + 100f),
                        size = MathUtils.random(2f, 6f),
                        speedX = -MathUtils.random(1f, 3f),
                        speedY = MathUtils.random(-1f, 1f) * 0.5f,
                        brightness = MathUtils.random(0.4f, 1f),
                        direction = direction
                    ))
                }
            }
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
        val margin = 200f // Khoảng cách reset sao

        stars.forEach { star ->
            // Di chuyển sao theo cả 2 trục
            star.x += star.speedX * delta * 80f
            star.y += star.speedY * delta * 80f

            // Reset sao khi ra khỏi màn hình với khoảng cách xa hơn
            when (star.direction) {
                0 -> { // Từ trên xuống
                    if (star.y < -margin || star.x < -margin || star.x > mapWidth + margin) {
                        resetStar(star, mapWidth, mapHeight)
                    }
                }
                1 -> { // Từ dưới lên
                    if (star.y > mapHeight + margin || star.x < -margin || star.x > mapWidth + margin) {
                        resetStar(star, mapWidth, mapHeight)
                    }
                }
                2 -> { // Từ trái sang
                    if (star.x > mapWidth + margin || star.y < -margin || star.y > mapHeight + margin) {
                        resetStar(star, mapWidth, mapHeight)
                    }
                }
                3 -> { // Từ phải sang
                    if (star.x < -margin || star.y < -margin || star.y > mapHeight + margin) {
                        resetStar(star, mapWidth, mapHeight)
                    }
                }
            }
        }
    }

    private fun resetStar(star: Star, mapWidth: Float, mapHeight: Float) {
        val newDirection = MathUtils.random(0, 3)
        star.direction = newDirection

        when (newDirection) {
            0 -> { // Từ trên xuống
                star.x = MathUtils.random(-100f, mapWidth + 100f)
                star.y = mapHeight + MathUtils.random(0f, 200f)
                star.speedX = MathUtils.random(-1f, 1f) * 0.8f
                star.speedY = -MathUtils.random(2f, 5f) // Tốc độ nhanh hơn
            }
            1 -> { // Từ dưới lên
                star.x = MathUtils.random(-100f, mapWidth + 100f)
                star.y = -MathUtils.random(0f, 200f)
                star.speedX = MathUtils.random(-1f, 1f) * 0.8f
                star.speedY = MathUtils.random(2f, 5f)
            }
            2 -> { // Từ trái sang
                star.x = -MathUtils.random(0f, 200f)
                star.y = MathUtils.random(-100f, mapHeight + 100f)
                star.speedX = MathUtils.random(2f, 5f)
                star.speedY = MathUtils.random(-1f, 1f) * 0.8f
            }
            3 -> { // Từ phải sang
                star.x = mapWidth + MathUtils.random(0f, 200f)
                star.y = MathUtils.random(-100f, mapHeight + 100f)
                star.speedX = -MathUtils.random(2f, 5f)
                star.speedY = MathUtils.random(-1f, 1f) * 0.8f
            }
        }
    }

    private fun renderStars() {
        batch.use {
            stars.forEach { star ->
                val color = Color(star.brightness, star.brightness, star.brightness, 1f)
                it.color = color
                it.draw(starTexture, star.x, star.y, star.size, star.size)
            }
            it.color = Color.WHITE // Reset color
        }
    }

    override fun render(delta: Float) {
        clearScreen(0.08f, 0.08f, 0.12f) // Màu nền tối hơn

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
