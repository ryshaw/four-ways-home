package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Pig extends Player {

	Pig() {
		super();
		sprite = new Sprite(new Texture("images/pig.png"));
		sprite.setScale(GameScreen.unitScale);

		speed = 8f;
		accel = 0.08f;
		maxJumpTime = 0.5f;
		timeBetweenJumps = maxJumpTime;

		jump = Gdx.audio.newSound(Gdx.files.internal("audio/pig_jump.wav"));
	}

	@Override
	void initialize(Body b, Vector2 p) {
		this.body = b;
		body.setUserData(this);
		BodyEditorLoader loader = new BodyEditorLoader(Gdx.files.internal("pig.json"));
		spawnPosition = p;
		position = p;
		initializePigBody(loader);
	}

}
