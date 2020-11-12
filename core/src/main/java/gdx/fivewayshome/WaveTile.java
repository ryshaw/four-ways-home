package gdx.fivewayshome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class WaveTile extends Entity {

	WaveTile(Body b, Vector2 p) {
		this.body = b;
		createBody(p);
	}

	void createBody(Vector2 p) {
		PolygonShape box = new PolygonShape();
		box.setAsBox(0.5f, 0.1f);
		body.setTransform(p.sub(0, 0.9f), 0);
		body.createFixture(box, 1.0f);
		body.setUserData(this);
		box.dispose();
		position = body.getPosition();
	}

	@Override
	public String toString() { return "WaveTile"; }
}
