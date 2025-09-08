    package com.example.mygame1.entities

    import GameState
    import com.badlogic.gdx.graphics.Texture
    import com.badlogic.gdx.graphics.g2d.Sprite
    import com.badlogic.gdx.graphics.g2d.SpriteBatch
    import com.badlogic.gdx.graphics.Color
    import com.badlogic.gdx.graphics.g2d.BitmapFont
    import com.badlogic.gdx.math.MathUtils
    import com.badlogic.gdx.math.Vector2
    import com.example.mygame1.input.InputHandler
    import com.example.mygame1.input.PlayerAction
    import ktx.assets.disposeSafely
    import ktx.assets.toInternalFile
    import kotlin.random.Random

    val characterTextures = listOf(
        "character/Characters/hitman1_hold.png",
        "character/Characters/manBlue_hold.png",
        "character/Characters/manBrown_hold.png",
        "character/Characters/manOld_hold.png",
        "character/Characters/robot1_hold.png",
        "character/Characters/soldier1_hold.png",
        "character/Characters/survivor1_hold.png",
        "character/Characters/womanGreen_hold.png",
        "character/Characters/zombie1_hold.png"
    )

    class Player(
        characterIndex: Int = -1
    ) {
        private var currentCharacterIndex =
            if (characterIndex in characterTextures.indices) characterIndex else randomizeCharacter()
        private var texture = Texture(characterTextures[currentCharacterIndex].toInternalFile())
        val sprite = Sprite(texture).apply { setOriginCenter() }
        var position = Vector2(120f, 120f)

        var health: Int = 100
        val maxHealth: Int = 100
        val speed = 200f

        private val detectRange: Float = 600f

        val maxBullets = 20
        var isReloading = false
        private var reloadTimer = 0f
        private val reloadTime = 2f

        val weapons: List<Weapon> = listOf(
            Weapon(GunType.Gun),
            Weapon(GunType.Machine),
            Weapon(GunType.Silencer)
        )
        private var currentWeaponIndex: Int = randomizeWeapon()

        val weapon: Weapon
            get() = weapons[currentWeaponIndex]

        val bullets = mutableListOf<Bullet>()
        val actionHistory = mutableListOf<Pair<GameState, PlayerAction>>()

        val characterIndex: Int
            get() = currentCharacterIndex

        val weaponIndex: Int
            get() = currentWeaponIndex

        private var shootCooldown = 0f

        private fun randomizeWeapon(): Int = Random.nextInt(weapons.size)
        private fun randomizeCharacter(): Int = Random.nextInt(characterTextures.size)

        fun selectCharacter(index: Int) {
            if (index in characterTextures.indices) {
                currentCharacterIndex = index
                texture.disposeSafely()
                texture = Texture(characterTextures[currentCharacterIndex].toInternalFile())
                sprite.setTexture(texture)
                sprite.setOriginCenter()
            }
        }

        fun selectWeapon(
            index: Int,
            enemyPosition: Vector2,
            bulletsOnMap: List<Bullet>
        ) {
            if (index in weapons.indices) {
                currentWeaponIndex = index
                shootCooldown = 0f // Sửa: reset cooldown khi đổi súng
                val currentState = GameState(
                    enemyPosition = enemyPosition,
                    playerPosition = position.cpy(),
                    bullets = bulletsOnMap
                )
                actionHistory.add(currentState to PlayerAction.ChangeWeapon(index))
            }
        }

        fun update(
            delta: Float,
            input: InputHandler,
            enemyPosition: Vector2,
            bulletsOnMap: List<Bullet>,
            mapWidth: Float = 800f,
            mapHeight: Float = 600f
        ) {
            if (bullets.size >= maxBullets && !isReloading) {
                isReloading = true
                reloadTimer = reloadTime
            }
            if (isReloading) {
                reloadTimer -= delta
                if (reloadTimer <= 0f) {
                    bullets.clear()
                    isReloading = false
                }
            }

            shootCooldown = (shootCooldown - delta).coerceAtLeast(0f)

            val dx = input.dx
            val dy = input.dy

            val moveAction = if (dx != 0f || dy != 0f) {
                PlayerAction.Move(Vector2(dx, dy))
            } else {
                PlayerAction.Idle
            }

            val currentState = GameState(
                enemyPosition = enemyPosition,
                playerPosition = position.cpy(),
                bullets = bulletsOnMap
            )
            actionHistory.add(currentState to moveAction)

            sprite.setPosition(position.x, position.y)

            bullets.forEach { it.update(delta) }
            bullets.removeAll { !it.isActive }
        }

        private fun getHandPosition(): Vector2 {
            val center = Vector2(position.x + sprite.width / 2f, position.y + sprite.height / 2f)
            val angleRad = sprite.rotation * MathUtils.degreesToRadians
            val handForward = 20f
            return Vector2(
                center.x + MathUtils.cos(angleRad) * handForward,
                center.y + MathUtils.sin(angleRad) * handForward
            )
        }

        fun render(batch: SpriteBatch) {
            sprite.draw(batch)
            val handPos = getHandPosition()
            weapon.render(batch, handPos, sprite.rotation)
            bullets.forEach { it.render(batch) }
        }

        fun renderUI(
            batch: SpriteBatch,
            blankTexture: Texture,
            font: BitmapFont,
            screenWidth: Float,
            screenHeight: Float
        ) {
            val barWidth = 360f
            val barHeight = 32f
            val barX = 40f
            val barY = screenHeight - barHeight - 40f

            val healthRatio = health / maxHealth.toFloat()
            batch.color = Color.DARK_GRAY
            batch.draw(blankTexture, barX - 3, barY - 3, barWidth + 6, barHeight + 6)
            batch.color = Color.RED
            batch.draw(blankTexture, barX, barY, barWidth, barHeight)
            batch.color = Color.GREEN
            batch.draw(blankTexture, barX, barY, barWidth * healthRatio, barHeight)
            batch.color = Color.WHITE

            font.color = Color.YELLOW
            font.data.setScale(2.0f)
            val currentAmmo = if (isReloading) 0 else maxBullets - bullets.size
            val ammoText = if (isReloading) "Reset..." else "$currentAmmo / $maxBullets"
            font.draw(batch, ammoText, barX, barY - 20f)
            font.data.setScale(1.0f)
            font.color = Color.WHITE
        }

        fun attack(
            enemyPosition: Vector2,
            bulletsOnMap: List<Bullet>
        ) {
            val stats = getGunStats(weapon.type)
            if (isReloading || bullets.size >= maxBullets) return
            if (shootCooldown > 0f) return

            val angleRad = sprite.rotation * MathUtils.degreesToRadians
            val bulletStart = getHandPosition()
            val direction = Vector2(MathUtils.cos(angleRad), MathUtils.sin(angleRad))

            bullets.add(
                Bullet(
                    type = when (weapon.type) {
                        GunType.Gun -> BulletType.Gun
                        GunType.Machine -> BulletType.Machine
                        GunType.Silencer -> BulletType.Silencer
                    },
                    position = bulletStart.cpy(),
                    direction = direction,
                    owner = BulletOwner.PLAYER,
                    maxDistance = stats.bulletRange,
                    size = stats.bulletSize,
                    damage = stats.damage
                )
            )

            shootCooldown = 1f / stats.fireRate

            val currentState = GameState(
                enemyPosition = enemyPosition,
                playerPosition = position.cpy(),
                bullets = bulletsOnMap
            )
            actionHistory.add(currentState to PlayerAction.Shoot)
        }

        fun takeDamage(amount: Int) {
            health = (health - amount).coerceAtLeast(0)
        }

        fun isDead(): Boolean = health <= 0

        fun dispose() {
            texture.disposeSafely()
            weapons.forEach { it.dispose() }
            bullets.forEach { it.dispose() }
        }
    }
