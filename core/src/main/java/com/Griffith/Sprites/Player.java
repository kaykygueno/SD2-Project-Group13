package com.Griffith.Sprites;

import com.Griffith.audio.SoundManager;
import com.Griffith.audio.SoundType;
import com.Griffith.gameConstants.GameConstants;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {

    private enum AnimationState {
        IDLE,
        RUN,
        JUMP,
        FALL,
        DEATH,
        RESPAWN
    }

    private static final String PUMPKIN_TEXTURE_PATH = "maps/images/Others/pumpkin_dudeCopy.png";
    private static final String DOC_TEXTURE_PATH = "maps/images/Others/docCopy.png";
    private static final String ANIMATION_FRAME_ROOT = "maps/images/0x72_DungeonTilesetII_v1.7/0x72_DungeonTilesetII_v1.7/frames/";
    private static final float ANIMATION_FRAME_DURATION = 0.12f;
    private static final float RUN_THRESHOLD = 0.01f;
    private static final float RUN_SOUND_INTERVAL = 0.24f;
    private static final float AIR_STATE_THRESHOLD = 15f;
    private static final float DEATH_ANIMATION_DURATION = 0.7f;
    private static final float RESPAWN_ANIMATION_DURATION = 0.7f;

    public float x, y;
    public float velocityY;
    public boolean isDead = false;

    private static final float SPEED = 150f;
    private static final float JUMP_POWER = 220f;
    private static final float GRAVITY = -500f;
    public static final float WIDTH = 16f;
    public static final float HEIGHT = 16f;

    private int leftKey, rightKey, jumpKey;
    private boolean onGround = false;

    public Rectangle bounds;
    private final Array<Texture> ownedTextures = new Array<>();
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> runAnimation;
    private TextureRegion staticFrame;
    private TextureRegion jumpFrame;
    private TextureRegion fallFrame;
    private TextureRegion deathFrame;
    private TextureRegion respawnFrame;
    private AnimationState animationState = AnimationState.IDLE;
    private float animationTime = 0f;
    private float deathAnimationTime = 0f;
    private float respawnAnimationTime = 0f;
    private float lastMoveX = 0f;
    private float runningSoundCooldown = 0f;
    private boolean facingRight = true;

    private float spawnX, spawnY;

    // Sets up the player at its spawn point, hooks up the controls, and loads the
    // visuals.
    public Player(float x, float y, String texturePath, int leftKey, int rightKey, int jumpKey) {
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.jumpKey = jumpKey;
        loadAnimations(texturePath);
        this.bounds = new Rectangle(x, y, WIDTH, HEIGHT);
        setAnimationState(AnimationState.RESPAWN);
        respawnAnimationTime = RESPAWN_ANIMATION_DURATION;
    }

    // Runs one frame of movement, jumping, gravity, collision checks, and animation
    // state updates.
    public void update(float delta, Array<Rectangle> ground) {
        if (isDead)
            return;

        runningSoundCooldown = Math.max(0f, runningSoundCooldown - delta);

        float previousX = x;
        float previousY = y;

        float moveX = 0f;
        if (Gdx.input.isKeyPressed(leftKey)) {
            moveX -= SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(rightKey)) {
            moveX += SPEED * delta;
        }

        lastMoveX = moveX;
        if (moveX < 0f) {
            facingRight = false;
        } else if (moveX > 0f) {
            facingRight = true;
        }

        x += moveX;
        bounds.setPosition(x, y);

        boolean hitWall = false;
        for (Rectangle tile : ground) {
            if (bounds.overlaps(tile)) {
                hitWall = true;
                break;
            }
        }

        if (hitWall) {
            // Revert to the last known safe horizontal position instead of
            // repeatedly snapping across multiple colliders.
            x = previousX;
            bounds.setPosition(x, y);
        }

        if (Gdx.input.isKeyJustPressed(jumpKey) && onGround) {
            velocityY = JUMP_POWER;
            onGround = false;
            SoundManager.play(SoundType.PLAYER_JUMP, 0.75f);
        }

        velocityY += GRAVITY * delta;
        y += velocityY * delta;
        bounds.setPosition(x, y);

        onGround = false;

        boolean resolvedVertical = false;
        for (Rectangle tile : ground) {
            if (bounds.overlaps(tile)) {
                if (velocityY < 0) {
                    y = tile.y + tile.height;
                    velocityY = 0;
                    onGround = true;
                    resolvedVertical = true;
                } else if (velocityY > 0) {
                    y = tile.y - HEIGHT;
                    velocityY = 0;
                    resolvedVertical = true;
                } else {
                    // If already embedded with no vertical velocity, return to the
                    // previous safe Y to prevent sudden teleport-like corrections.
                    y = previousY;
                    resolvedVertical = true;
                }
                bounds.setPosition(x, y);
            }
        }

        if (!resolvedVertical && y <= 0) {
            y = 0;
            velocityY = 0;
            onGround = true;
            bounds.setPosition(x, y);
        }

        // Keep player inside the designed map area.
        if (x < 0f) {
            x = 0f;
        } else if (x > GameConstants.MAP_WIDTH - WIDTH) {
            x = GameConstants.MAP_WIDTH - WIDTH;
        }

        if (y < 0f) {
            y = 0f;
            velocityY = 0f;
            onGround = true;
        } else if (y > GameConstants.MAP_HEIGHT - HEIGHT) {
            y = GameConstants.MAP_HEIGHT - HEIGHT;
            velocityY = 0f;
        }
        bounds.setPosition(x, y);

        playRunningSoundIfNeeded(x - previousX);
        setAnimationState(selectAnimationState());
    }

    // Moves the player by a fixed offset, mainly so lifts can carry them around.
    public void moveBy(float dx, float dy) {
        x += dx;
        y += dy;

        if (x < 0f) {
            x = 0f;
        } else if (x > GameConstants.MAP_WIDTH - WIDTH) {
            x = GameConstants.MAP_WIDTH - WIDTH;
        }

        if (y < 0f) {
            y = 0f;
        } else if (y > GameConstants.MAP_HEIGHT - HEIGHT) {
            y = GameConstants.MAP_HEIGHT - HEIGHT;
        }

        bounds.setPosition(x, y);
    }

    // Draws the current player frame, or the vapor effect if the player has just
    // died.
    public void draw(SpriteBatch batch) {
        advanceAnimation(Gdx.graphics.getDeltaTime());

        if (animationState == AnimationState.DEATH) {
            drawEvaporation(batch);
            return;
        }

        TextureRegion currentFrame = getCurrentFrame();
        if (currentFrame == null) {
            return;
        }

        float originalR = batch.getColor().r;
        float originalG = batch.getColor().g;
        float originalB = batch.getColor().b;
        float originalA = batch.getColor().a;
        float rotation = 0f;
        float drawX = x;
        float drawY = y;
        float scaleX = facingRight ? 1f : -1f;
        float scaleY = 1f;

        if (animationState == AnimationState.RESPAWN) {
            float pulse = 0.65f + 0.35f
                    * Math.abs((float) Math.sin((RESPAWN_ANIMATION_DURATION - respawnAnimationTime) * 18f));
            batch.setColor(1f, 1f, 1f, pulse);
        }

        batch.draw(
                currentFrame,
                drawX,
                drawY,
                WIDTH * 0.5f,
                HEIGHT * 0.5f,
                WIDTH,
                HEIGHT,
                scaleX,
                scaleY,
                rotation);

        batch.setColor(originalR, originalG, originalB, originalA);
    }

    // Puts the player into the death state and kicks off the evaporation effect.
    public void die() {
        die(true);
    }

    // Puts the player into the death state and optionally plays the defeat sound.
    public void die(boolean playSound) {
        if (isDead) {
            return;
        }
        isDead = true;
        velocityY = 0;
        runningSoundCooldown = 0f;
        deathAnimationTime = 0f;
        setAnimationState(AnimationState.DEATH);
        if (playSound) {
            SoundManager.play(SoundType.PLAYER_DEATH, 0.85f);
        }
    }

    // Sends the player back to spawn and starts the short respawn flash.
    public void respawn() {
        x = spawnX;
        y = spawnY;
        velocityY = 0;
        isDead = false;
        onGround = false;
        runningSoundCooldown = 0f;
        bounds.setPosition(x, y);
        deathAnimationTime = 0f;
        respawnAnimationTime = RESPAWN_ANIMATION_DURATION;
        setAnimationState(AnimationState.RESPAWN);
    }

    // Lets other systems override whether the player should count as grounded.
    public void setOnGround(boolean val) {
        this.onGround = val;
    }

    // Gives back the collision rectangle used by the rest of the game.
    public Rectangle getBounds() {
        return bounds;
    }

    public float getLastMoveX() {
        return lastMoveX;
    }

    // Cleans up every texture this player loaded.
    public void dispose() {
        for (Texture texture : ownedTextures) {
            texture.dispose();
        }
    }

    // Plays a soft repeating footstep while the player is genuinely moving on the ground.
    private void playRunningSoundIfNeeded(float actualMoveX) {
        if (!onGround || Math.abs(actualMoveX) <= RUN_THRESHOLD || runningSoundCooldown > 0f) {
            return;
        }

        SoundManager.play(SoundType.PLAYER_RUN_STEP, 0.35f);
        runningSoundCooldown = RUN_SOUND_INTERVAL;
    }

    // Loads the base sprite plus any matching idle and run frames for this
    // character.
    private void loadAnimations(String texturePath) {
        Texture texture = new Texture(texturePath);
        ownedTextures.add(texture);
        staticFrame = new TextureRegion(texture);

        String characterKey = resolveCharacterKey(texturePath);
        if (characterKey == null) {
            initializeFallbackAnimations();
            return;
        }

        Array<TextureRegion> idleFrames = loadFrameSequence(characterKey, "idle");
        Array<TextureRegion> runFrames = loadFrameSequence(characterKey, "run");

        if (idleFrames.size == 0) {
            idleFrames.add(staticFrame);
        }
        if (runFrames.size == 0) {
            runFrames.add(idleFrames.first());
        }

        idleAnimation = new Animation<>(ANIMATION_FRAME_DURATION, idleFrames, Animation.PlayMode.LOOP);
        runAnimation = new Animation<>(ANIMATION_FRAME_DURATION, runFrames, Animation.PlayMode.LOOP);

        jumpFrame = runFrames.get(Math.min(1, runFrames.size - 1));
        fallFrame = runFrames.peek();
        deathFrame = idleFrames.peek();
        respawnFrame = idleFrames.first();
    }

    // Matches the old texture paths to the animation frame names already in assets.
    private String resolveCharacterKey(String texturePath) {
        if (PUMPKIN_TEXTURE_PATH.equals(texturePath)) {
            return "pumpkin_dude";
        }
        if (DOC_TEXTURE_PATH.equals(texturePath)) {
            return "doc";
        }
        return null;
    }

    // Pulls a small numbered frame sequence from disk if those files exist.
    private Array<TextureRegion> loadFrameSequence(String characterKey, String action) {
        Array<TextureRegion> frames = new Array<>();
        for (int frameIndex = 0; frameIndex < 4; frameIndex++) {
            String framePath = ANIMATION_FRAME_ROOT + characterKey + "_" + action + "_anim_f" + frameIndex + ".png";
            if (!Gdx.files.internal(framePath).exists()) {
                continue;
            }

            Texture texture = new Texture(framePath);
            ownedTextures.add(texture);
            frames.add(new TextureRegion(texture));
        }
        return frames;
    }

    // Falls back to a single still frame when no animation set is available.
    private void initializeFallbackAnimations() {
        Array<TextureRegion> singleFrame = new Array<>();
        singleFrame.add(staticFrame);
        idleAnimation = new Animation<>(ANIMATION_FRAME_DURATION, singleFrame, Animation.PlayMode.LOOP);
        runAnimation = new Animation<>(ANIMATION_FRAME_DURATION, singleFrame, Animation.PlayMode.LOOP);
        jumpFrame = staticFrame;
        fallFrame = staticFrame;
        deathFrame = staticFrame;
        respawnFrame = staticFrame;
    }

    // Chooses the animation state that best matches how the player is moving right
    // now.
    private AnimationState selectAnimationState() {
        if (isDead) {
            return AnimationState.DEATH;
        }
        if (respawnAnimationTime > 0f) {
            return AnimationState.RESPAWN;
        }
        if (!onGround) {
            if (velocityY > AIR_STATE_THRESHOLD) {
                return AnimationState.JUMP;
            }
            if (velocityY < -AIR_STATE_THRESHOLD) {
                return AnimationState.FALL;
            }
            return AnimationState.JUMP;
        }
        if (Math.abs(lastMoveX) > RUN_THRESHOLD) {
            return AnimationState.RUN;
        }
        return AnimationState.IDLE;
    }

    // Swaps to a new animation state and restarts that state's timer.
    private void setAnimationState(AnimationState nextState) {
        if (animationState == nextState) {
            return;
        }
        animationState = nextState;
        animationTime = 0f;
    }

    // Advances the running timers for the current animation and any temporary
    // effects.
    private void advanceAnimation(float delta) {
        animationTime += delta;

        if (animationState == AnimationState.DEATH) {
            deathAnimationTime = Math.min(DEATH_ANIMATION_DURATION, deathAnimationTime + delta);
        }

        if (respawnAnimationTime > 0f) {
            respawnAnimationTime = Math.max(0f, respawnAnimationTime - delta);
            if (respawnAnimationTime == 0f && !isDead) {
                setAnimationState(selectAnimationState());
            }
        }
    }

    // Returns the frame that should be shown for the current animation state.
    private TextureRegion getCurrentFrame() {
        switch (animationState) {
            case RUN:
                return runAnimation.getKeyFrame(animationTime, true);
            case JUMP:
                return jumpFrame;
            case FALL:
                return fallFrame;
            case DEATH:
                return deathFrame;
            case RESPAWN:
                return respawnFrame != null ? respawnFrame : idleAnimation.getKeyFrame(animationTime, true);
            case IDLE:
            default:
                return idleAnimation.getKeyFrame(animationTime, true);
        }
    }

    // Draws a quick vapor sweep over the death spot after the character disappears.
    private void drawEvaporation(SpriteBatch batch) {
        if (deathFrame == null) {
            return;
        }

        float originalR = batch.getColor().r;
        float originalG = batch.getColor().g;
        float originalB = batch.getColor().b;
        float originalA = batch.getColor().a;

        float progress = Math.min(1f, deathAnimationTime / DEATH_ANIMATION_DURATION);
        float baseX = x;
        float baseY = y + HEIGHT * 0.15f;

        for (int i = 0; i < 3; i++) {
            float offsetFactor = i / 2f;
            float sweep = -4f + progress * (12f + i * 4f);
            float rise = progress * (5f + i * 2f);
            float wobble = (float) Math.sin(progress * (10f + i * 3f) + i) * (1.2f + i * 0.3f);
            float alpha = Math.max(0f, (0.55f - offsetFactor * 0.12f) * (1f - progress));
            float puffWidth = WIDTH * (0.45f + progress * (0.55f + offsetFactor * 0.2f));
            float puffHeight = HEIGHT * (0.25f + progress * (0.35f + offsetFactor * 0.15f));
            float puffX = baseX + sweep + wobble;
            float puffY = baseY + rise + offsetFactor * 4f;

            batch.setColor(0.88f, 0.97f, 1f, alpha);
            batch.draw(
                    deathFrame,
                    puffX,
                    puffY,
                    puffWidth * 0.5f,
                    puffHeight * 0.5f,
                    puffWidth,
                    puffHeight,
                    facingRight ? 1f : -1f,
                    1f,
                    0f);
        }

        batch.setColor(originalR, originalG, originalB, originalA);
    }
}
