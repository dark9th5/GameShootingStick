package com.example.mygame1.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.Stage
import com.example.mygame1.Main
import com.example.mygame1.audio.AudioManager
import com.example.mygame1.world.World
import com.example.mygame1.ui.Joystick
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.assets.disposeSafely

class GameScreen(private val game: Main) : KtxScreen {
    private val batch = SpriteBatch()
    private val stage = Stage()
    val world = World()
    private val touchpad = Joystick.create(stage)

    private var paused = false



    override fun show() {
        Gdx.input.inputProcessor = stage
        AudioManager.playMusic("sounds/game_music.mp3")
    }

    override fun render(delta: Float) {
        clearScreen(0.06f, 0.06f, 0.1f)

        // Nếu bấm ESC thì chuyển sang PauseMenuScreen
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen<PauseMenuScreen>()
            return
        }

        if (!paused) {
            world.update(delta, touchpad)

            // Nếu player chết → sang GameOver
            if (world.player.isDead()) {
                game.setScreen<GameOverScreen>()
                return
            }
        }

        world.render(batch)
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
    }
}
