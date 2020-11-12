package gdx.fivewayshome;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

import java.util.Arrays;
import java.util.List;


public class Box2DCollision implements ContactListener {
	GameScreen screen;
	Player player;

	Box2DCollision(GameScreen s) {
		this.screen = s;
		player = s.player;
	}

	void updatePlayer(Player p) {
		this.player = p;
	}

	@Override
	public void beginContact(Contact contact) {
		Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
		Entity b = (Entity) contact.getFixtureB().getBody().getUserData();

		if (checkCollisionWith(a, b, "EndPoint") && !GameScreen.complete) screen.levelComplete();
		if (checkCollisionWith(a, b, "WaveTile") && player.getClass() == Frog.class) player.fellIntoWater();
		if (checkCollisionWith(a, b, "Ground") && player.getClass() == Frog.class) player.touchGround();
		if (checkCollisionWith(a, b, "Checkpoint")) screen.insideCheckpoint = true;
		if (checkCollisionWith(a, b, "Thorns")) {
			player.die(false);
			screen.playerDied();
		}
		if (checkCollisionWith(a, b, "TutorialSign")) {
			screen.tutorial.setVisible(true);
			screen.tutorialImage.setVisible(true);
		}

		if (checkCollisionWith(a, b, "WaveTile") && player.getClass() != Frog.class) {
			player.die(true);
			screen.playerDied();
		}

		if (playerTouchGround(contact, a, b)) player.numContacts += 1;

		if (checkCollisionWith(a, b, "Enemy")) playerHitEnemy(contact, a, b);

		updateGoatSensors(contact, a, b, true);
		updateCatSensors(contact, a, b, true);
	}

	@Override
	public void endContact(Contact contact) {
		Entity a = (Entity) contact.getFixtureA().getBody().getUserData();
		Entity b = (Entity) contact.getFixtureB().getBody().getUserData();

		if (checkCollisionWith(a, b, "Checkpoint")) screen.insideCheckpoint = false;
		if (checkCollisionWith(a, b, "TutorialSign")) {
			screen.tutorial.setVisible(false);
			screen.tutorialImage.setVisible(false);
		}

		if (playerTouchGround(contact, a, b)) player.numContacts -= 1;

		updateGoatSensors(contact, a, b, false);
		updateCatSensors(contact, a, b, false);
		updateEnemySensors(contact, a, b);
	}

	boolean playerTouchGround(Contact c, Entity a, Entity b) {
		List<String> groundTiles = Arrays.asList("Ground", "WaveTile");

		if (a.toString().equals("Player")) {
			return (c.getFixtureA().isSensor() && groundTiles.contains(b.toString()));
		}
		else if (b.toString().equals("Player")) {
			return (c.getFixtureB().isSensor() && groundTiles.contains(a.toString()));
		}
		else return false;

		/*if (a.toString().equals("Player")) {
			return (c.getFixtureA().isSensor() && !c.getFixtureB().isSensor());
		}
		else if (b.toString().equals("Player")) {
			return (c.getFixtureB().isSensor() && !c.getFixtureA().isSensor());
		}
		else return false; */
	}

	boolean checkCollisionWith(Entity a, Entity b, String e) {
		boolean b1 = (a.toString().equals("Player")) && b.toString().equals(e);
		boolean b2 = (b.toString().equals("Player")) && a.toString().equals(e);
		return b1 || b2;
	}

	void updateCatSensors(Contact c, Entity a, Entity b, boolean setToValue) {
		if (a.toString().equals("Player") && a.getClass() == Cat.class && !c.getFixtureB().isSensor()) {
			Cat cat = (Cat) a;
			if (c.getFixtureA().getUserData().equals("right")) cat.canJumpRight = setToValue; // can jump from the right
			else if (c.getFixtureA().getUserData().equals("left")) cat.canJumpLeft = setToValue; // can jump from the left
			else if (c.getFixtureA().getUserData().equals("ground")) cat.onGround = setToValue;

		} else if (b.toString().equals("Player") && b.getClass() == Cat.class  && !c.getFixtureA().isSensor()) {
			Cat cat = (Cat) b;
			if (c.getFixtureB().getUserData().equals("right")) cat.canJumpRight = setToValue;
			else if (c.getFixtureB().getUserData().equals("left")) cat.canJumpLeft = setToValue;
			else if (c.getFixtureB().getUserData().equals("ground")) cat.onGround = setToValue;
		}
	}

	void updateGoatSensors(Contact c, Entity a, Entity b, boolean setToValue) {
		if (a.toString().equals("Player") && a.getClass() == Goat.class && !c.getFixtureB().isSensor()) {
			Goat g = (Goat) a;
			if (c.getFixtureA().getUserData().equals("right")) setClimbing(g, "right", setToValue);
			else if (c.getFixtureA().getUserData().equals("left")) setClimbing(g, "left", setToValue);

		} else if (b.toString().equals("Player") && b.getClass() == Goat.class  && !c.getFixtureA().isSensor()) {
			Goat g = (Goat) b;
			if (c.getFixtureB().getUserData().equals("right")) setClimbing(g, "right", setToValue);
			else if (c.getFixtureB().getUserData().equals("left")) setClimbing(g, "left", setToValue);
		}
	}

	void setClimbing(Goat g, String s, boolean setToValue) {
		if (setToValue && g.numContacts >= 2) {
			if (s.equals("right")) g.climbingR = true;
			else if (s.equals("left")) g.climbingL = true;
		} else if (!setToValue) {
			if (s.equals("right")) g.climbingR = false;
			else if (s.equals("left")) g.climbingL = false;
		}
	}

	void playerHitEnemy(Contact c, Entity a, Entity b) {
		if (b.toString().equals("Player") && c.getFixtureA().getUserData().equals("top")) { // player jumped on enemy
			if (!player.dead) {
				((Enemy) a).die();
				if (player.getClass() == Goat.class) {
					player.body.setLinearVelocity(player.body.getLinearVelocity().x, 4f);
				} else {
					player.body.setLinearVelocity(player.body.getLinearVelocity().x, 10f);
				}
			}
		}
		else if (a.toString().equals("Player") && c.getFixtureB().getUserData().equals("top")) {
			if (!player.dead) {
				((Enemy) b).die();
				if (player.getClass() == Goat.class) {
					player.body.setLinearVelocity(player.body.getLinearVelocity().x, 4f);
				} else {
					player.body.setLinearVelocity(player.body.getLinearVelocity().x, 10f);
				}
			}
		}
		else { // player collided with enemy
			if (a.toString().equals("Enemy") && !((Enemy) a).dead && c.getFixtureA().getUserData().equals("main") && !player.dead) {
				player.die(false);
				screen.playerDied();
			} else if (b.toString().equals("Enemy") && !((Enemy) b).dead && c.getFixtureB().getUserData().equals("main") && !player.dead) {
				player.die(false);
				screen.playerDied();
			}
		}
	}

	void updateEnemySensors(Contact c, Entity a, Entity b) {
		if (a.toString().equals("Enemy") && b.toString().equals("Ground")) {
			Enemy enemy = (Enemy) a;
			if (c.getFixtureA().getUserData().equals("right")) {
				if (!enemy.hasStoppedRight) enemy.stoppedRight = true; // hit end on right side
			}
			else if (c.getFixtureA().getUserData().equals("left")) {
				if (!enemy.hasStoppedLeft) enemy.stoppedLeft = true; // hit end on left side
			}
		} else if (b.toString().equals("Enemy") && a.toString().equals("Ground")) {
			Enemy enemy = (Enemy) b;
			if (c.getFixtureB().getUserData().equals("right")) {
				if (!enemy.hasStoppedRight) enemy.stoppedRight = true; // hit end on right side
			}
			else if (c.getFixtureB().getUserData().equals("left")) {
				if (!enemy.hasStoppedLeft) enemy.stoppedLeft = true; // hit end on left side
			}

		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}
}
