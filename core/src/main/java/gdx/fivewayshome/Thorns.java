package gdx.fivewayshome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public class Thorns extends Entity {
	public Thorns(Body b, Vector2 p) {
		this.body = b;
		createBody(p);
	}

	void createBody(Vector2 p) {
		PolygonShape trapezoid = new PolygonShape();
		Vector2[] vertices = new Vector2[4];
		vertices[0] = new Vector2(-0.5f, -0.5f);
		vertices[1] = new Vector2(-0.25f, 0f);
		vertices[2] = new Vector2(0.25f, 0f);
		vertices[3] = new Vector2(0.5f, -0.5f);
		trapezoid.set(vertices);
		body.setTransform(p, 0);
		body.createFixture(trapezoid, 1.0f);
		body.setUserData(this);
		trapezoid.dispose();
		position = body.getPosition();
	}

	@Override
	public String toString() { return "Thorns"; }
}
