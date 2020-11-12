package gdx.fivewayshome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class TutorialSign extends Entity {

	TutorialSign(Body b, Vector2 p) {
		this.body = b;
		createBody(p);
	}

	void createBody(Vector2 p) {
		PolygonShape box = new PolygonShape();
		box.setAsBox(1f, 0.5f, new Vector2(0f, 0f), 0);
		body.setTransform(p, 0);
		body.createFixture(box, 0.0f);
		body.setUserData(this);
		box.dispose();
		body.getFixtureList().get(0).setSensor(true);
		position = body.getPosition();
	}

	@Override
	public String toString() { return "TutorialSign"; }
}
