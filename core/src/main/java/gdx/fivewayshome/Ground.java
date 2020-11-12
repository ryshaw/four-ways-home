package gdx.fivewayshome;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public class Ground extends Entity {
	int size;

	Ground(Body b, Vector2 p, int s) {
		this.body = b;
		body.setUserData(this);
		this.size = s;
	}

	@Override
	public String toString() { return "Ground"; }
}
