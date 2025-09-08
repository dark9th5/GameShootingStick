package com.example.mygame1.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.example.mygame1.Main
import com.example.mygame1.audio.AudioManager
import com.example.mygame1.world.World
import com.example.mygame1.ui.Joystick
import com.example.mygame1.ui.AttackPad
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely

class GameScreen(private val game: Main) : KtxScreen {
    private val batch = SpriteBatch()
    private val stage = Stage()
    private val skin = Skin(Gdx.files.internal("ui/uiskin.json")) // Đảm bảo bạn có file này trong assets

    // Sửa: truyền stage và skin vào World
    val world = World(stage, skin)
    private val touchpad = Joystick.create(stage)

    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()

    private var paused = false

    private val attackPad = AttackPad.create(
        stage,
        onAttackDirection = {},
        onAttackRelease = {}
    ).apply {
        setBounds(
            screenWidth - 350f,
            150f,
            200f, 200f
        )
    }

    private val blankTexture: Texture by lazy {
        val pixmap = com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        Texture(pixmap, false)
    }
    private val font: BitmapFont = BitmapFont()

    override fun show() {
        Gdx.input.inputProcessor = stage
        AudioManager.playMusic("sounds/game_music.mp3")

        if (!attackPad.hasParent()) {
            stage.addActor(attackPad)
        }
    }

    override fun render(delta: Float) {
        clearScreen(0.06f, 0.06f, 0.1f)

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen<PauseMenuScreen>()
            return
        }

        if (!paused) {
            world.update(delta, touchpad)

            // Xử lý hướng bắn từ attackPad
            val knobX = attackPad.knobPercentX
            val knobY = attackPad.knobPercentY
            val isPadActive = attackPad.isTouched && (Math.abs(knobX) > 0.1f || Math.abs(knobY) > 0.1f)

            if (isPadActive) {
                val direction = Vector2(knobX, knobY).nor()
                world.player.sprite.rotation = direction.angleDeg()
                world.player.attack(
                    bulletsOnMap = world.player.bullets + world.enemies.flatMap { it.bullets },
                    enemyPosition = world.enemies.firstOrNull()?.position ?: Vector2.Zero
                )
            }

            if (world.player.isDead()) {
                game.setScreen<GameOverScreen>()
                return
            }
        }

        // Vẽ thế giới theo camera
        batch.projectionMatrix = world.camera.combined
        batch.begin()
        world.render(batch, font, blankTexture)
        batch.end()

        // Vẽ UI theo màn hình
        batch.projectionMatrix = Matrix4().setToOrtho2D(0f, 0f, screenWidth, screenHeight)
        batch.begin()
        world.player.renderUI(batch, blankTexture, font, screenWidth, screenHeight)
        batch.end()

        // Stage cho các nút, joystick, swapWeaponButton...
        stage.act(delta)
        stage.draw()
    }

    override fun pause() {
        paused = true
        AudioManager.stopMusic()
    }

    override fun resume() {
        paused = false
        AudioManager.playMusic("sounds/game_music.mp3")
    }

    override fun dispose() {
        batch.disposeSafely()
        stage.dispose()
        world.dispose()
        blankTexture.disposeSafely()
        font.disposeSafely()
    }
}
