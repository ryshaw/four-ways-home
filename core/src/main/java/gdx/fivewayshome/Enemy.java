package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Array;

public class Enemy extends Entity {
	float deltaX, speed, accel;
	Vector2 spawnPosition;
	int numContacts;
	Sound die;
	int direction; // -1 = left, 1 = right
	boolean dead, stoppedRight, stoppedLeft, hasStoppedRight, hasStoppedLeft;
	int character; // 0 = pig, 1 = frog, 2 = goat, 3 = cat
	float stoppedTimer, deathTimer, hopTimer;

	// the player is 2x2 tiles (64x64)

	Enemy(Body b, Vector2 p) {
		super();
		sprite = new Sprite(new Texture("images/enemy-pig.png"));
		character = 0;
		sprite.setScale(GameScreen.unitScale);

		speed = 8f;
		accel = 0.08f;

		deltaX = MathUtils.random(0.3f, 0.8f);
		numContacts = 0;
		direction = 1;
		this.body = b;
		body.setUserData(this);
		BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("enemy.json"));
		spawnPosition = p;
		position = p;
		initializeBody(loader);
		stoppedTimer = 0;
		stoppedRight = false;
		stoppedLeft = false;
		hasStoppedRight = false;
		hasStoppedLeft = false;
		deathTimer = 10f; // allows the enemy to completely fall off the map before being set inactive
		hopTimer = 0;
		die = Gdx.audio.newSound(Gdx.files.internal("audio/stomp.mp3"));
	}

	@Override
	void update(SpriteBatch batch, float delta) {
		position = body.getPosition();
		draw(batch);
		if (GameScreen.time < 1.8f) return;

		if (dead) {
			deathTimer -= delta;
			if (deathTimer < 0) body.setActive(false);
			for (Fixture f : body.getFixtureList()) body.destroyFixture(f);
			return;
		}

		if (character != GameScreen.characterIndex) switchCharacter();

		body.applyForce(1e-20f, 0, position.x, position.y, true);
		// above line fixes a bug where walking off a platform means you stay in the air

		stoppedTimer -= delta;
		if ((stoppedLeft || stoppedRight) && direction != 0) { // if supposed to stop and haven't stopped yet
			direction = 0;
			stoppedTimer = MathUtils.random(1f, 2.0f);
		}

		if ((stoppedLeft || stoppedRight) && stoppedTimer < 0) { // if done stopping
			sprite.flip(true, false);

			if (stoppedLeft) {
				direction = 1; // go back to the right
				hasStoppedLeft = true;
				hasStoppedRight = false;
			} else if (stoppedRight) {
				direction = -1; // go back to the left
				hasStoppedRight = true;
				hasStoppedLeft = false;
			}

			stoppedLeft = false;
			stoppedRight = false;
		}

		if (character == 1) { // frog does hops
			hopTimer -= delta;
			if (hopTimer < 0) {
				hopTimer = 1.5f;
				body.setLinearVelocity(6f * direction * deltaX, 6f * Math.abs(direction));
			}
		} else { // other characters just move
			float newX = position.x + (speed / 100f) * deltaX * direction;
			newX = MathUtils.clamp(newX, 1, GameScreen.mapWidth - 1);
			body.setTransform(newX, position.y, 0);
		}

	}

	void switchCharacter() {
		character = GameScreen.characterIndex;
		switch (character) {
			case 0:
				sprite = new Sprite(new Texture("images/enemy-pig.png"));
				deltaX = MathUtils.random(0.3f, 0.9f);
				break;
			case 1:
				sprite = new Sprite(new Texture("images/enemy-frog.png"));
				deltaX = MathUtils.random(0.5f, 0.7f);
				break;
			case 2:
				sprite = new Sprite(new Texture("images/enemy-goat.png"));
				deltaX = MathUtils.random(0.2f, 0.6f);
				break;
			case 3:
				sprite = new Sprite(new Texture("images/enemy-cat.png"));
				deltaX = MathUtils.random(0.8f, 1.4f);
				break;
		}
		if (direction == -1 || stoppedLeft) sprite.flip(true, false);
		sprite.setScale(GameScreen.unitScale);
		body.setLinearVelocity(0, 0);
	}


	void draw(SpriteBatch batch) {
		float spriteX = position.x - sprite.getWidth() / 2;
		float spriteY = position.y - sprite.getHeight() / 2;
		sprite.setPosition(spriteX, spriteY);
		sprite.draw(batch);
	}

	void initializeBody(BodyEditorLoader loader) {
		if (body.getFixtureList().isEmpty()) {
			FixtureDef fd = new FixtureDef();
			fd.density = 0.8f;
			fd.friction = 0.3f;
			fd.restitution = 0.1f;

			loader.attachFixture(body, "main", fd, 2);

			fd = new FixtureDef();
			fd.isSensor = true;
			fd.density = 0f;
			fd.friction = 0f;
			fd.restitution = 0f;

			loader.attachFixture(body, "top", fd, 2);

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
		fixtures.get(1).setUserData("top");
		fixtures.get(2).setUserData("right");
		fixtures.get(3).setUserData("left");
	}

	void die() {
		die.play(Main.SOUND_VOLUME);
		body.setLinearVelocity(0, 0);
		body.applyLinearImpulse(0, 4f, position.x, position.y, true);
		dead = true;
	}

	@Override
	void dispose() {
		super.dispose();
		die.dispose();
	}

	@Override
	public String toString() { return "Enemy"; }
}
