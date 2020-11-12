package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;


public class Goat extends Player {
	boolean climbingR, climbingL;
	float deltaY, timeAllowedOnWall, adorableHopTime;

	Goat() {
		super();
		sprite = new Sprite(new Texture("images/goat.png"));
		sprite.setScale(GameScreen.unitScale);

		speed = 7f;
		accel = 0.06f;
		maxJumpTime = 0.5f;
		timeBetweenJumps = maxJumpTime;
		climbingL = false;
		climbingR = false;
		deltaY = 0;
		timeAllowedOnWall = 1f;
		adorableHopTime = 0.3f;

		jump = Gdx.audio.newSound(Gdx.files.internal("audio/goat_jump.wav"));
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

	@Override
	void update(SpriteBatch batch, float delta) {
		position = body.getPosition();
		draw(batch);

		Vector2 p = body.getPosition();
		if (GameScreen.time < 1.8f) return;
		if (GameScreen.complete) {
			body.setTransform(p.x + 0.05f, p.y, 0);
			adorableHopTime -= delta;
			if (adorableHopTime < 0) {
				body.applyLinearImpulse(0, 4f, position.x, position.y, true);
				adorableHopTime = 0.35f;
			}
			if (p.x > GameScreen.mapWidth + 1) body.setActive(false);
			return;
		}
		if (checkIfDead()) return;

		body.applyForce(1e-20f, 0, position.x, position.y, true);
		// above line fixes a bug where walking off a platform means you stay in the air

		Vector2 v = body.getLinearVelocity();
		if (Gdx.input.isKeyPressed(Main.LEFT_KEY)) {
			if (direction == 1) sprite.flip(true, false);
			direction = -1;

			if (climbingL) { // if climbing, climb up
				deltaY += accel * 5;
				deltaY = MathUtils.clamp(deltaY, 0, 1f);
				body.setLinearVelocity(v.x, 3 * deltaY);
				timeAllowedOnWall = 1;
			} else { // move to the left
				if (deltaX > 0) deltaX = 0;
				deltaX -= accel;
			}
		} else if (Gdx.input.isKeyPressed(Main.RIGHT_KEY)) {
			if (direction == -1) sprite.flip(true, false);
			direction = 1;

			if (climbingR) { // if climbing, climb up
				deltaY += accel * 5;
				deltaY = MathUtils.clamp(deltaY, 0, 1f);
				body.setLinearVelocity(v.x, 3 * deltaY);
				timeAllowedOnWall = 1;
			} else { // move to the right
				if (deltaX < 0) deltaX = 0;
				deltaX += accel;
			}
		} else {
			if (climbingL || climbingR) {
				deltaY -= accel / (timeAllowedOnWall - 0.4f);
				deltaY = MathUtils.clamp(deltaY, -1f, 1f);
				body.setLinearVelocity(v.x, 2 * deltaY);
				timeAllowedOnWall += 0.1f;
				if (numContacts >= 2) deltaY = 0;
			} else {
				if (deltaX > 0) deltaX -= accel;
				else if (deltaX < 0) deltaX += accel;
				if (Math.abs(deltaX) < 0.10f) deltaX = 0;
			}
		}

		timeBetweenJumps -= delta;
		if (numContacts > 0 && Gdx.input.isKeyPressed(Main.JUMP_KEY) && timeBetweenJumps < 0) jump();

		deltaX = MathUtils.clamp(deltaX, -1.0f, 1.0f);

		float newX = p.x + (speed / 100f) * deltaX;
		newX = MathUtils.clamp(newX, 1, GameScreen.mapWidth - 1);
		body.setTransform(newX, p.y, 0);
	}

	void jump() {
		timeBetweenJumps = maxJumpTime;
		jump.play(Main.SOUND_VOLUME);
		body.applyLinearImpulse(0, 4f, position.x, position.y, true);
	}
}
