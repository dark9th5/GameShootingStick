package com.example.mygame1.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.example.mygame1.Main
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

class SplashScreen(private val game: Main) : KtxScreen {

    private var elapsed = 0f
    private val logo = Texture("logo/Logo.png")
    private val background = Texture(Gdx.files.internal("background/splash_bg.png"))

    private val screenRatio = Gdx.graphics.width.toFloat() / Gdx.graphics.height
    private val imageRatio = background.width.toFloat() / background.height

    private var scale: Float = 0.0f
    private var drawWidth: Float = 0.0f
    private var drawHeight: Float = 0.0f

    override fun render(delta: Float) {
        clearScreen(0f, 0f, 0f)
        elapsed += delta

        // Tính toán scale background để giữ tỉ lệ
        if (screenRatio < imageRatio) {
            scale = Gdx.graphics.height.toFloat() / background.height
            drawWidth = background.width * scale
            drawHeight = Gdx.graphics.height.toFloat()
        } else {
            scale = Gdx.graphics.width.toFloat() / background.width
            drawWidth = Gdx.graphics.width.toFloat()
            drawHeight = background.height * scale
        }

        game.batch.use { batch ->
            // Vẽ background (giữ tỉ lệ, căn giữa màn hình)
            batch.draw(
                background,
                (Gdx.graphics.width - drawWidth) / 2f,
                (Gdx.graphics.height - drawHeight) / 2f,
                drawWidth,
                drawHeight
            )

            // Vẽ logo, auto scale chiếm 30% chiều rộng màn hình
            val targetWidth = Gdx.graphics.width * 0.3f
            val logoScale = targetWidth / logo.width
            val targetHeight = logo.height * logoScale

            batch.draw(
                logo,
                (Gdx.graphics.width - targetWidth) / 2f,
                (Gdx.graphics.height - targetHeight) / 2f,
                targetWidth,
                targetHeight
            )
        }

        // Sau 5 giây chuyển sang MainMenuScreen
        if (elapsed > 5f) {
            game.setScreen<MainMenuScreen>()
        }
    }

    override fun dispose() {
        background.dispose()
        logo.dispose()
    }
}
