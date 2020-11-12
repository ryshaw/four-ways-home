package gdx.fivewayshome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class EndPoint extends Entity {

	EndPoint(Body b, Vector2 p) {
		this.body = b;
		createBody(p);
	}

	void createBody(Vector2 p) {
		PolygonShape box = new PolygonShape();
		box.setAsBox(2f, 1.5f, new Vector2(0.5f, 1f), 0);
		body.setTransform(p, 0);
		body.createFixture(box, 0.0f);
		body.setUserData(this);
		box.dispose();
		body.getFixtureList().get(0).setSensor(true);
		position = body.getPosition();
	}

	@Override
	public String toString() { return "EndPoint"; }
}
