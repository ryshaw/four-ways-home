package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Cat extends Player {
	float stopTime;
	boolean canJumpRight, canJumpLeft, onGround, hasJumpedRight, hasJumpedLeft;
	// if you're on the right wall, you canJumpRight.
	// if you've jumped from the right wall, hasJumpedRight will be true until either
	// you land or you jump off a left wall. only then can you jump from right again

	Cat() {
		super();
		sprite = new Sprite(new Texture("images/cat.png"));
		sprite.setScale(GameScreen.unitScale);

		speed = 10f;
		accel = 0.1f;
		maxJumpTime = 0.4f;
		timeBetweenJumps = maxJumpTime;
		canJumpRight = false;
		canJumpLeft = false;
		onGround = false;
		hasJumpedLeft = false;
		hasJumpedRight = false;
		stopTime = 0.8f;

		jump = Gdx.audio.newSound(Gdx.files.internal("audio/cat_jump.wav"));
	}

	@Override
	void update(SpriteBatch batch, float delta) {
		position = body.getPosition();
		draw(batch);

		Vector2 p = body.getPosition();
		if (GameScreen.time < 1.8f) return;
		if (GameScreen.complete) {
			body.setTransform(p.x + (speed / 100f) * deltaX, p.y, 0);
			deltaX = deltaX * 0.95f;
			if (Math.abs(deltaX) < 0.05f) deltaX = 0;
			if (Math.abs(body.getLinearVelocity().y) < 0.1f) stopTime -= delta;
			if (stopTime < 0) body.setTransform(p.x + 0.3f, p.y, 0);
			if (p.x > GameScreen.mapWidth + 1) body.setActive(false);
			return;
		}
		if (checkIfDead()) return;

		body.applyForce(1e-20f, 0, position.x, position.y, true);
		// above line fixes a bug where walking off a platform means you stay in the air

		if (Gdx.input.isKeyPressed(Main.LEFT_KEY)) {
			if (direction == 1) sprite.flip(true, false);
			direction = -1;
			if (deltaX > 0) deltaX = 0;
			deltaX -= accel;
		} else if (Gdx.input.isKeyPressed(Main.RIGHT_KEY)) {
			if (direction == -1) sprite.flip(true, false);
			direction = 1;
			if (deltaX < 0) deltaX = 0;
			deltaX += accel;
		} else {
			if (deltaX > 0) deltaX -= accel;
			else if (deltaX < 0) deltaX += accel;
			if (Math.abs(deltaX) < 0.10f) deltaX = 0;
		}

		if (onGround) {
			hasJumpedLeft = false;
			hasJumpedRight = false;
		}

		timeBetweenJumps -= delta;
		if (Gdx.input.isKeyPressed(Main.JUMP_KEY) && timeBetweenJumps < 0) {
			if (numContacts > 0 && !canJumpRight && !canJumpLeft) jump(0); // jump straight up
			else if (canJumpLeft && !hasJumpedLeft) {
				deltaX = 0;
				jump(1); // jump from left
				hasJumpedLeft = true;
				hasJumpedRight = false;
			}
			else if (canJumpRight && !hasJumpedRight) {
				deltaX = 0;
				jump(-1); // jump from right
				hasJumpedRight = true;
				hasJumpedLeft = false;
			}
		}

		deltaX = MathUtils.clamp(deltaX, -1.0f, 1.0f);

		float newX;
		if (onGround) newX = p.x + (speed / 100f) * deltaX; // if on ground, move faster
		else newX = p.x + (speed / 120f) * deltaX; // if in air, move slower

		newX = MathUtils.clamp(newX, 1, GameScreen.mapWidth - 1);
		body.setTransform(newX, p.y, 0);
	}

	void jump(int dir) {
		timeBetweenJumps = maxJumpTime;
		jump.play(Main.SOUND_VOLUME);
		body.setLinearVelocity(dir * 2f, 12f);
	}

	@Override
	void initialize(Body b, Vector2 p) {
		this.body = b;
		body.setUserData(this);
		BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("goat.json"));
		spawnPosition = p;
		position = p;
		initializeGoatBody(loader);
	}

}
