package com.example.mygame1.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.example.mygame1.Main
import com.example.mygame1.audio.AudioManager
import ktx.app.KtxScreen

class MainMenuScreen(private val game: Main) : KtxScreen {

    private val stage = Stage(ScreenViewport())
    private val skin = Skin(Gdx.files.internal("ui/uiskin.json")) // cần file uiskin.json (có sẵn trong libGDX setup)

    override fun show() {
        Gdx.input.inputProcessor = stage
        AudioManager.playMusic("sounds/menu_music.mp3")

        val table = Table()
        table.setFillParent(true)
        stage.addActor(table)

        // Tiêu đề game
        val title = Label("Survival - Die", skin, "default").apply {
            setFontScale(2f)
            color = Color.WHITE
        }

        // Nút Play
        val playButton = TextButton("Play", skin).apply {
            label.setFontScale(1.5f)
            label.color = Color.WHITE // chữ trắng
        }

        // Sự kiện bấm nút
        playButton.addListener { _ ->
            game.setScreen<GameScreen>()
            true
        }

        table.add(title).padBottom(50f).row()
        table.add(playButton).width(200f).height(80f)
    }

    override fun render(delta: Float) {
        stage.act(delta)
        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
        skin.dispose()
    }
}
