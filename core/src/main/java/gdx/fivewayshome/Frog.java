package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;

public class Frog extends Player {
	float jumpCharge, hopTimer, jumpBoolean;
	boolean inAir, jumping, inWater;

	Frog() {
		super();
		sprite = new Sprite(new Texture("images/frog.png"));
		sprite.setScale(GameScreen.unitScale);

		speed = 6f;
		accel = 0.05f;
		maxJumpTime = 0.8f;
		timeBetweenJumps = maxJumpTime;
		jumpCharge = 0f;
		hopTimer = 0f;
		inAir = true;
		jumping = false;
		jump = Gdx.audio.newSound(Gdx.files.internal("audio/frog_jump.wav"));
	}

	@Override
	void initialize(Body b, Vector2 p) {
		this.body = b;
		body.setUserData(this);
		BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("pig.json"));
		spawnPosition = p;
		position = p;
		initializePigBody(loader);
		for (Fixture f : body.getFixtureList()) {
			f.setFriction(1f);
		}
	}

	@Override
	void update(SpriteBatch batch, float delta) {
		position = body.getPosition();
		draw(batch);

		if (GameScreen.time < 1.8f) return;
		if (GameScreen.complete) {
			if (body.getLinearVelocity().epsilonEquals(new Vector2(0, 0), 0.01f)) {
				body.applyLinearImpulse(20f, 20f, position.x, position.y, true);
				jump.play(Main.SOUND_VOLUME);
			}
			if (position.x > GameScreen.mapWidth + 1) body.setActive(false);
			return;
		}
		if (checkIfDead()) return;

		body.applyForce(1e-20f, 0, position.x, position.y, true);
		// above line fixes a bug where walking off a platform means you stay in the air

		hopTimer -= delta;
		inAir = (numContacts == 0);

		if (jumpBoolean > 0) {
			jumpBoolean -= delta;
			if (jumpBoolean <= 0) jumping = false;
		}

		if (jumping && jumpBoolean <= 0) jumpBoolean = 0.5f;


		if (!inAir && hopTimer < 0 && jumpCharge < 0.2f && !jumping) {
			if (Gdx.input.isKeyPressed(Main.LEFT_KEY)) {
				if (direction == 1) sprite.flip(true, false);
				direction = -1;
				if (inWater) {
					hopTimer = 1f;
					body.applyLinearImpulse(-6f, 2f, position.x, position.y, true);
				} else {
					hopTimer = 0.4f;
					body.applyLinearImpulse(-4f, 4f, position.x, position.y, true);
				}

			} else if (Gdx.input.isKeyPressed(Main.RIGHT_KEY)) {
				if (direction == -1) sprite.flip(true, false);
				direction = 1;
				if (inWater) {
					hopTimer = 1f;
					body.applyLinearImpulse(6f, 2f, position.x, position.y, true);
				} else {
					hopTimer = 0.4f;
					body.applyLinearImpulse(4f, 4f, position.x, position.y, true);
				}

			}
		}

		if (inAir && hopTimer < 0) {
			if (Gdx.input.isKeyPressed(Main.LEFT_KEY)) {
				if (direction == 1) sprite.flip(true, false);
				direction = -1;
				float newX = position.x - 0.05f;
				newX = MathUtils.clamp(newX, 1, GameScreen.mapWidth - 1);
				body.setTransform(newX, position.y, 0);
			} else if (Gdx.input.isKeyPressed(Main.RIGHT_KEY)) {
				if (direction == -1) sprite.flip(true, false);
				direction = 1;
				float newX = position.x + 0.05f;
				newX = MathUtils.clamp(newX, 1, GameScreen.mapWidth - 1);
				body.setTransform(newX, position.y, 0);
			}
		}

		timeBetweenJumps -= delta;
		if (numContacts > 0 && Gdx.input.isKeyPressed(Main.JUMP_KEY) && timeBetweenJumps < 0) jumpCharge += 2 * delta;
		else if (numContacts == 0) jumpCharge -= delta * 4;

		jumpCharge = MathUtils.clamp(jumpCharge, 0, 1f);

		if (jumpCharge > 0 && !Gdx.input.isKeyPressed(Main.JUMP_KEY)) jump();

		float newX = body.getPosition().x;
		newX = MathUtils.clamp(newX, 1, GameScreen.mapWidth - 1);
		body.setTransform(newX, position.y, 0);
	}

	@Override
	void fellIntoWater() { inWater = true; }

	@Override
	void touchGround() { inWater = false; }

	@Override
	void draw(SpriteBatch batch) {
		sprite.setRotation(MathUtils.radiansToDegrees * (body.getAngle()));
		float spriteX = position.x - sprite.getWidth() / 2;
		float scale = MathUtils.lerp(GameScreen.unitScale, 1/40f, jumpCharge);
		float yOffset = MathUtils.lerp(0, 0.23f, jumpCharge);
		float spriteY = position.y - sprite.getHeight() / 2 - yOffset;
		sprite.setPosition(spriteX, spriteY);
		sprite.setScale(GameScreen.unitScale, scale);

		sprite.draw(batch);
	}


	@Override
	void jump() {
		timeBetweenJumps = maxJumpTime;
		jump.play(Main.SOUND_VOLUME);
		body.applyLinearImpulse(0, 20f * jumpCharge, position.x, position.y, true);
		jumpCharge = 0;
		jumping = true;
	}
}
