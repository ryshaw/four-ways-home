package gdx.fivewayshome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Checkpoint extends Entity {

	Checkpoint(Body b, Vector2 p) {
		this.body = b;
		createBody(p);
	}

	void createBody(Vector2 p) {
		PolygonShape box = new PolygonShape();
		box.setAsBox(1f, 0.5f);
		body.setTransform(p, 0);
		body.createFixture(box, 0.0f);
		body.setUserData(this);
		box.dispose();
		body.getFixtureList().get(0).setSensor(true);
		position = body.getPosition();
	}

	@Override
	public String toString() { return "Checkpoint"; }
}
