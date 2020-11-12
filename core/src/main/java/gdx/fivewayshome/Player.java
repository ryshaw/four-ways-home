package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

public class Player extends Entity {
	float deltaX, speed, accel, timeBetweenJumps, maxJumpTime;
	Vector2 spawnPosition;
	Sound jump;
	int numContacts;
	int direction; // -1 = left, 1 = right
	boolean dead;

	// the player is 2x2 tiles (64x64)

	Player() {
		deltaX = 0;
		numContacts = 0;
		direction = 1;
	}

	@Override
	void update(SpriteBatch batch, float delta) {
		float previousX = position.x;
		position = body.getPosition();
		draw(batch);

		Vector2 p = body.getPosition();
		if (GameScreen.time < 1.8f) return;
		if (GameScreen.complete) {
			body.setTransform(p.x + 0.12f, p.y, 0);
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

		timeBetweenJumps -= delta;
		if (numContacts > 0 && Gdx.input.isKeyPressed(Main.JUMP_KEY) && timeBetweenJumps < 0) jump();

		deltaX = MathUtils.clamp(deltaX, -1.0f, 1.0f);
		float newX = p.x + (speed / 100f) * deltaX;
		newX = MathUtils.clamp(newX, 1, GameScreen.mapWidth - 1);
		body.setTransform(newX, p.y, 0);

		// next two lines attempt to fix the running-into-wall glitch look
		float distance = Math.abs(position.x - previousX);
		if (Math.abs(deltaX) > (accel * 2) && MathUtils.isEqual(distance, 0, 1e-2f)) deltaX = 0;
	}

	void initialize(Body b, Vector2 spawn) {}

	void draw(SpriteBatch batch) {
		float spriteX = position.x - sprite.getWidth() / 2;
		float spriteY = position.y - sprite.getHeight() / 2;
		sprite.setPosition(spriteX, spriteY);
		sprite.setRotation(MathUtils.radiansToDegrees * (body.getAngle()));
		sprite.draw(batch);
	}

	void jump() {
		timeBetweenJumps = maxJumpTime;
		jump.play(Main.SOUND_VOLUME);
		body.applyLinearImpulse(0, 12f, position.x, position.y, true);
	}

	void fellIntoWater() {}
	void touchGround() {}

	void initializePigBody(BodyEditorLoader loader) {
		if (body.getFixtureList().isEmpty()) {
			FixtureDef fd = new FixtureDef();
			fd.density = 0.8f;
			fd.friction = 0.1f;
			fd.restitution = 0.1f;

			loader.attachFixture(body, "main", fd, 2);

			fd = new FixtureDef();
			fd.isSensor = true;
			fd.density = 0f;
			fd.friction = 0f;
			fd.restitution = 0f;

			loader.attachFixture(body, "leg1", fd, 2);

			fd = new FixtureDef();
			fd.isSensor = true;
			fd.density = 0f;
			fd.friction = 0f;
			fd.restitution = 0f;

			loader.attachFixture(body, "leg2", fd, 2);
		}

		body.setActive(true);
		body.setTransform(position, 0);
		body.setLinearVelocity(0, 0);

		body.setLinearDamping(1f);
		sprite.setAlpha(1f);
		body.setFixedRotation(true);
	}

	void initializeGoatBody(BodyEditorLoader loader) {
		if (body.getFixtureList().isEmpty()) {
			FixtureDef fd = new FixtureDef();
			fd.density = 0.8f;
			fd.friction = 0.1f;
			fd.restitution = 0.1f;

			loader.attachFixture(body, "main", fd, 2);

			fd = new FixtureDef();
			fd.isSensor = true;
			fd.density = 0f;
			fd.friction = 0f;
			fd.restitution = 0f;

			loader.attachFixture(body, "ground", fd, 2);

			fd = new FixtureDef();
			fd.isSensor = true;
			fd.density = 0f;
			fd.friction = 0f;
			fd.restitution = 0f;

			loader.attachFixture(body, "right", fd, 2);

			fd = new FixtureDef();
			fd.isSensor = true;
			fd.density = 0f;
			fd.friction = 0f;
			fd.restitution = 0f;

			loader.attachFixture(body, "left", fd, 2);
		}

		body.setActive(true);
		body.setTransform(position, 0);
		body.setLinearVelocity(0, 0);

		body.setLinearDamping(1f);
		sprite.setAlpha(1f);
		body.setFixedRotation(true);

		Array<Fixture> fixtures = body.getFixtureList();
		fixtures.get(0).setUserData("main");
		fixtures.get(1).setUserData("ground");
		fixtures.get(2).setUserData("right");
		fixtures.get(3).setUserData("left");
	}

	boolean checkIfDead() { // must be done during update loop, not physics step
		if (dead) {
			for (Fixture f : body.getFixtureList()) body.destroyFixture(f);
			return true;
		}
		return false;
	}

	void die(boolean inWater) { // if fell in water, don't jump out
		body.setLinearVelocity(0, 0);
		if (!inWater) body.applyLinearImpulse(0, 10f, position.x, position.y, true);
		dead = true;

	}

	@Override
	void dispose() {
		jump.dispose();
		super.dispose();
	}

	@Override
	public String toString() { return "Player"; }
}
