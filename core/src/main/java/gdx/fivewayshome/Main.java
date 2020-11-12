package gdx.fivewayshome;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Arrays;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
	SpriteBatch batch;
	Screen currentScreen;
	static float MUSIC_VOLUME = 0.6f;
	static float SOUND_VOLUME = 0.4f;
	static final double[] levelTimes = new double[10];
	static int totalDeaths, levelsUnlocked, WIDTH, HEIGHT, translation, LEFT_KEY, RIGHT_KEY, JUMP_KEY;
	//translation: 0 = english, 1 = portuguese

	@Override
	public void create() {
		batch = new SpriteBatch();
		Arrays.fill(levelTimes, 0);
		levelsUnlocked = 0;
		totalDeaths = 0;
		translation = 0;
		WIDTH = Gdx.graphics.getWidth();
		HEIGHT = Gdx.graphics.getHeight();
		LEFT_KEY = Input.Keys.A;
		RIGHT_KEY = Input.Keys.D;
		JUMP_KEY = Input.Keys.W;

		this.currentScreen = new SplashScreen(this);
		this.setScreen(currentScreen);
	}

	@Override
	public void render() { super.render(); }

	public static String convertTimeToString(double t) {
		int min = 0;
		while (t > 59) {
			min++;
			t -= 60;
		}
		long sec = Math.round(t);
		if (sec < 10) return "time: " + min + ":0" + sec;
		else return "time: " + min + ":" + sec;
	}


	@Override
	public void dispose() {
		batch.dispose();
		currentScreen.dispose();
	}
}