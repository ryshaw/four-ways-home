package gdx.fivewayshome;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

public abstract class Entity {
	Sprite sprite;
	Body body;
	Vector2 position;

	void update(SpriteBatch batch, float delta) {}

	void dispose() {
		if (sprite != null) {
			sprite.getTexture().dispose();
		}
	}

	public String toString() { return "Entity"; }
}
