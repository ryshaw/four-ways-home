package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class StoryScreen implements Screen {
	private final Main game;
	private final Stage stage;
	Music music;
	Sound click;
	Array<TextLabel> dialogue;
	int currentFrame;
	Array<Image> frames;
	Image storyBackground;
	float coolDown, maxTime = 8.6f, fadeTime = 2.0f, skipTime;

	StoryScreen(final Main game) {
		this.game = game;
		//Camera camera = new OrthographicCamera();

		// okay... now just simply adjust the resolution to 16:9
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		int adjustedWidth = Math.round(h * (16 / 9f)) / 16; // out of 100%, where 100% = 1600
		int adjustedHeight = Math.round(w * (9 / 16f)) / 9; // out of 100%, where 100% = 900
		// if adjustedW > adjustedHeight, the resolution was sized down horizontally and we have less width
		// to work with. otherwise, it was sized down vertically and we have less height to work with.
		if (adjustedWidth < adjustedHeight) {
			// we have less height, so size down the width
			stage = new Stage(new FitViewport(h * (16 / 9f), h));
		} else {
			// we have less width, so size down the height
			stage = new Stage(new FitViewport(w, w * (9 / 16f)));
		}
		//viewport.getCamera().update();
		Main.WIDTH = stage.getViewport().getScreenWidth();
		Main.HEIGHT = stage.getViewport().getScreenHeight();
		w = Main.WIDTH;
		h = Main.HEIGHT;
		// that was insanely hard for no reason. anyway,

		Gdx.input.setInputProcessor(stage);
		click = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));
		storyBackground = new Image(new Texture("images/opening/opening-background.png"));
		storyBackground.setWidth(w);
		storyBackground.setHeight(h);
		storyBackground.setScaling(Scaling.fill);
		stage.addActor(storyBackground);
		storyBackground.setPosition(0, 0);

		Label skip = new TextLabel("hold enter to skip", 1, 0.5f, Align.left) {
			public void act(float delta) {
				if (currentFrame >= 2) this.setVisible(false); }
		};
		skip.setPosition(w * (0.85f), h * (0.02f));
		skip.setVisible(false);
		stage.addActor(skip);
		stage.addAction(Actions.sequence(Actions.delay(7f), Actions.run(() -> skip.setVisible(true))));

		dialogue = StoryNarrator.getOpening();
		for (TextLabel t : dialogue) {
			t.setPosition(80 * (w / 1600f), 60 * (h / 900f));
			stage.addActor(t);
		}

		frames = StoryNarrator.getOpeningFrames();
		for (int i = 1; i <= 10; i++) {
			Image f = frames.get(i - 1);
			f.setPosition(72 * (w / 1600f), 492 * (h / 900f));
			stage.addActor(f);
		}

		currentFrame = 0;
		this.setDialogVisible(currentFrame);

		coolDown = maxTime + 4f;

		stage.addAction(Actions.sequence(Actions.fadeOut(0), Actions.delay(1f), Actions.fadeIn(3f)));

		dialogue.get(currentFrame).setColor(0f, 0f, 0f, 0f);
		Color c = frames.get(currentFrame).getColor();
		frames.get(currentFrame).setColor(c.r, c.g, c.b, 0f);
		dialogue.get(currentFrame).addAction(Actions.sequence(Actions.delay(5f), Actions.fadeIn(fadeTime)));
		frames.get(currentFrame).addAction(Actions.sequence(Actions.delay(5f), Actions.fadeIn(fadeTime)));

		music = Gdx.audio.newMusic(Gdx.files.internal("audio/music_box_v3.mp3"));
		if (!MathUtils.isEqual(Main.MUSIC_VOLUME, 0f, 0.01f)) music.setVolume(Main.MUSIC_VOLUME + 0.3f);
		else music.setVolume(Main.MUSIC_VOLUME);
		music.setLooping(true);
		music.play();
		skipTime = 1f;
	}

	void setDialogVisible(int n) {
		for (int i = 0; i < dialogue.size; i++) {
			dialogue.get(i).setVisible(i == n);
			frames.get(i).setVisible(i == n);
		}
	}



	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(255 / 255f, 255 / 255f, 255 / 255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		coolDown -= delta;
		if (coolDown < 0 && dialogue.get(currentFrame).getActions().isEmpty()) {
			dialogue.get(currentFrame).addAction(Actions.sequence(Actions.fadeOut(fadeTime), Actions.run(this::toNextFrame)));
			frames.get(currentFrame).addAction(Actions.fadeOut(fadeTime));
		}

		this.setDialogVisible(currentFrame);

		if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
			skipTime -= delta;
			if (skipTime < 0) {
				stage.addAction(Actions.sequence(Actions.fadeOut(1f), Actions.delay(2f), Actions.run(this::startGame)));
				skipTime = 1000; // lazy fix to a bug where the above line runs more than once
			}
		} else {
			skipTime = 1f;
		}

		stage.act();
		stage.draw();

	}

	private void toNextFrame() {
		coolDown = maxTime + fadeTime; // give extra time for the new frame to fade in

		if (currentFrame >= 9) {
			stage.addAction(Actions.sequence(Actions.fadeOut(1.5f), Actions.delay(2.5f), Actions.run(this::startGame)));
			return;
		}
		currentFrame++;
		dialogue.get(currentFrame).setColor(0f, 0f, 0f, 0f);
		Color c = frames.get(currentFrame).getColor();
		frames.get(currentFrame).setColor(c.r, c.g, c.b, 0f);
		dialogue.get(currentFrame).addAction(Actions.fadeIn(fadeTime));
		frames.get(currentFrame).addAction(Actions.fadeIn(fadeTime));
	}

	private void startGame() {
		music.stop();
		game.currentScreen = new MapScreen(game, 1);
		game.setScreen(game.currentScreen);
	}

	@Override
	public void dispose() {
		stage.dispose();
		music.dispose();
		click.dispose();
	}

	@Override
	public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

	@Override
	public void show() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

}