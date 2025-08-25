package com.example.mygame1.screen

import com.badlogic.gdx.Gdx
import com.example.mygame1.Main
import com.example.mygame1.audio.AudioManager
import com.example.mygame1.screen.GameScreen
import ktx.app.KtxScreen
import ktx.app.clearScreen

class MainMenuScreen(private val game: Main) : KtxScreen {
    override fun show() {
        AudioManager.playMusic("sounds/menu_music.mp3")
    }

    override fun render(delta: Float) {
        clearScreen(0.1f, 0.1f, 0.2f)
        if (Gdx.input.isTouched) {
            game.setScreen<GameScreen>()
        }
    }
}

