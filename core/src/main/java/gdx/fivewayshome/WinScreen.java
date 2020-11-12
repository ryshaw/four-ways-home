package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class WinScreen implements Screen {
	private final Stage stage;
	Image[] bars;
	TextLabel ending;
	float y, animationTime;
	Group group;
	TextLabel menu;
	Music music;
	Sound click;
	boolean returningToMenu;
	Animation<Texture> walk;
	Image walkImage;

	WinScreen(final Main game) {
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

		group = new Group();
		Gdx.input.setInputProcessor(stage);
		click = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));

		Image bg = new Image(new Texture("images/credits-bg.png"));
		bg.setWidth(w);
		bg.setHeight(h);
		bg.setScaling(Scaling.fill);
		stage.addActor(bg);
		float columnWidth = w * 0.06f;
		float columnWidth2 = w * 0.5f;

		Array<Texture> walking = new Array<>();
		for (int i = 1; i < 3; i++) {
			Texture t = new Texture("images/loading" + i + ".png");
			walking.add(t);
		}
		walk = new Animation<>(0.3f, walking, Animation.PlayMode.LOOP);
		walkImage = new Image(walk.getKeyFrame(0));
		walkImage.setScale(1);
		walkImage.setPosition(w * 0.4f, h * 0.05f);
		stage.addActor(walkImage);
		walkImage.setVisible(false);
		animationTime = 0;

		ending = StoryNarrator.getEnding();
		ending.setPosition(w * 0.04f,0);
		stage.addActor(ending);
		y = -h*3.8f;

		Label title = new TextLabel("thanks for playing :)", 1, 2f, Align.center);
		title.setPosition(columnWidth, h * 0.88f);
		group.addActor(title);

		Label levelScore;
		for (int i = 1; i < 5; i++) {
			double partTime = Main.levelTimes[(2*i) - 2] + Main.levelTimes[(2*i) - 1];
			String time = Main.convertTimeToString(partTime);
			String text = "";
			switch (i) {
				case 1:
					text = "the grasslands ";
					break;
				case 2:
					text = "the marshlands ";
					break;
				case 3:
					text = "the mountains ";
					break;
				case 4:
					text = "the forest ";
					break;
			}
			levelScore = new TextLabel(text + time, 1, 1f, Align.center);
			levelScore.setPosition(columnWidth, h * (0.92f) - (h * (i / 6f)));
			group.addActor(levelScore);
		}

		double totalTimeToComplete = 0f;
		for (double f : Main.levelTimes) { totalTimeToComplete += f; }
		Label totalTime = new TextLabel("total " + Main.convertTimeToString(totalTimeToComplete), 1, 1f, Align.center);
		totalTime.setPosition(columnWidth, h * 0.12f);
		group.addActor(totalTime);

		Label totalDeaths = new TextLabel("game overs: " + Main.totalDeaths, 1, 1f, Align.center);
		totalDeaths.setPosition(columnWidth, h * 0.06f);
		group.addActor(totalDeaths);

		Label ryan = new TextLabel("a game by ryan shaw", 1, 1.5f, Align.center);
		ryan.setPosition(columnWidth2, h * 0.75f);
		group.addActor(ryan);

		Label gabyyu = new TextLabel("music by gabyyu", 1, 1.5f, Align.center);
		gabyyu.setPosition(columnWidth2, h * 0.6f);
		group.addActor(gabyyu);

		Label art = new TextLabel("art by uni & zip", 1, 1.5f, Align.center);
		art.setPosition(columnWidth2, h * 0.45f);
		group.addActor(art);

		Label sprites = new TextLabel("sprites by ryan shaw", 1, 1.5f, Align.center);
		sprites.setPosition(columnWidth2, h * 0.3f);
		group.addActor(sprites);

		returningToMenu = false;
		menu = new TextLabel("continue >", 1, 1f, Align.center);
		menu.setPosition(w * 0.8f, menu.getHeight() / 2);
		group.addActor(menu);
		menu.addListener(new LabelListener(menu) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (returningToMenu) return true;
				click.play(Main.SOUND_VOLUME);
				transitionToMenu();
				menu.addAction(Actions.sequence(Actions.delay(4.5f), Actions.run(() -> {
					music.stop();
					game.currentScreen = new StartScreen(game, true);
					game.setScreen(game.currentScreen);
				})));

				returningToMenu = true;
				return true;
			}
		});
		stage.addActor(group);

		Group transitionBars = new Group();
		bars = new Image[10];
		for (int i = 0; i < 10; i++) {
			bars[i] = new Image(new Texture("images/transition-bar.png"));
			bars[i].setPosition(i * (w / 10f), h);
			bars[i].setWidth(w / 10f);
			bars[i].setHeight(h);
			bars[i].setScaling(Scaling.fill);
			transitionBars.addActor(bars[i]);
		}
		stage.addActor(transitionBars);

		music = Gdx.audio.newMusic(Gdx.files.internal("audio/river.mp3"));
		music.setVolume(Main.MUSIC_VOLUME);
		music.setLooping(true);
		music.play();

		for (Actor a : group.getChildren()) a.setVisible(false);

		stage.addAction(Actions.sequence(Actions.fadeOut(0), Actions.delay(0.5f), Actions.fadeIn(2f)));
	}


	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1f, 1f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		y += 1;
		ending.setPosition(ending.getX(), y);

		if (y > Main.HEIGHT * 0.74f) {
			int s = group.getChildren().size;
			for (int i = 0; i < s - 1; i++) {
				Actor a = group.getChild(i);
				Action delay;
				if (i <= 6) {
					delay = Actions.delay(i*(1.5f) + 1); // stats
				} else {
					delay = Actions.delay(i*(1.5f) + 1.2f); // credits
				}

				a.addAction(Actions.sequence(Actions.delay(0.5f), delay, Actions.run(() -> a.setVisible(true))));
			}
			menu.addAction(Actions.sequence(Actions.delay(0.5f), Actions.delay(s*(1.5f) + 3f), Actions.run(() -> menu.setVisible(true))));
			walkImage.addAction(Actions.sequence(Actions.delay(0.5f), Actions.delay(s*(1.5f) + 0.5f), Actions.run(() -> walkImage.setVisible(true))));
		}

		animationTime += delta;
		TextureRegionDrawable texture = new TextureRegionDrawable(walk.getKeyFrame(animationTime));
		walkImage.setDrawable(texture);

		stage.act();
		stage.draw();
	}

	void transitionToMenu() {
		for (int i = 1; i < 11; i++) {
			Image b = bars[i - 1];
			if (b.getY() > 0) b.addAction(Actions.moveBy(0, -Main.HEIGHT, 1.5f + (i / 4f), Interpolation.smooth2));
		}
	}

	@Override
	public void dispose() {
		stage.dispose();
		music.dispose();
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