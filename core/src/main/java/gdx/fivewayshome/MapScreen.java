package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.Arrays;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;


public class MapScreen implements Screen {
	private final Stage stage1, stage2; // stage1 controls player, stage2 controls text
	private final FitViewport viewport;
	Music music;
	Sound click;
	Image player, blank;
	Image[] bars, bars2;
	float progress, mapSpeed;
	TiledMap map;
	OrthogonalTiledMapRenderer mapRenderer;
	Array<Integer> levelPositions;
	int atLevel, fromLevel, mapWidth, mapHeight;
	boolean canMove, readyToStart;
	TextLabel levelName, start, levelTime;

	MapScreen(final Main game, int level) {
		atLevel = level - 1;
		fromLevel = level - 1;

		TmxMapLoader mapLoader = new TmxMapLoader();
		TmxMapLoader.Parameters par = new TmxMapLoader.Parameters();
		par.textureMinFilter = Texture.TextureFilter.Nearest;
		par.textureMagFilter = Texture.TextureFilter.Nearest;

		map = mapLoader.load("map.tmx", par);
		MapProperties mP = map.getProperties();
		mapWidth = mP.get("width", Integer.class);
		mapHeight = mP.get("height", Integer.class);
		processLevelPositions();

		float screenMapWidth = mapHeight * (16 / 9f); // the screen should cover this amount

		// okay... now just simply adjust the resolution to 16:9
		// screen is 35.56 tiles wide by 20 tiles tall
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		int adjustedWidth = Math.round(h * (16 / 9f)) / 16; // out of 100%, where 100% = 1600
		int adjustedHeight = Math.round(w * (9 / 16f)) / 9; // out of 100%, where 100% = 900
		// if adjustedW > adjustedHeight, the resolution was sized down horizontally and we have less width
		// to work with. otherwise, it was sized down vertically and we have less height to work with.
		if (adjustedWidth < adjustedHeight) {
			// we have less height, so size down the width
			viewport = new FitViewport(mapHeight * (16 / 9f), mapHeight, new OrthographicCamera());
			stage2 = new Stage(new FitViewport(h * (16 / 9f), h));
		} else {
			// we have less width, so size down the height
			viewport = new FitViewport(screenMapWidth, screenMapWidth * (9 / 16f), new OrthographicCamera());
			stage2 = new Stage(new FitViewport(w, w * (9 / 16f)));
		}
		stage1 = new Stage(viewport);
		Main.WIDTH = stage2.getViewport().getScreenWidth();
		Main.HEIGHT = stage2.getViewport().getScreenHeight();
		w = Main.WIDTH;
		h = Main.HEIGHT;
		// that was insanely hard for no reason. anyway,

		Gdx.input.setInputProcessor(stage2); // for text inputs
		Group text = new Group();
		Group transitionBars = new Group();

		player = new Image(new Texture("images/pig.png"));
		player.setWidth(4f);
		player.setHeight(4f);

		//player.setScaling(Scaling.fit);
		float playerX = viewport.getWorldWidth() / 2f - player.getWidth() / 2;
		float playerY = viewport.getWorldHeight() * 0.62f;
		player.setPosition(playerX, playerY);
		stage1.addActor(player);

		blank = new Image(new Texture("images/blank.png")); // used to reveal the map when starting game
		blank.setWidth(w);
		blank.setHeight(h);
		blank.setScaling(Scaling.fill);
		blank.setPosition(0, 0);
		stage1.addActor(blank);

		bars = new Image[10];
		for (int i = 0; i < 10; i++) {
			bars[i] = new Image(new Texture("images/transition-bar.png"));
			bars[i].setPosition(i * (w / 10f), h);
			bars[i].setWidth(w / 10f);
			bars[i].setHeight(h);
			bars[i].setScaling(Scaling.fill);
			transitionBars.addActor(bars[i]);
		}

		Group transitionBars2 = new Group();
		bars2 = new Image[5];
		for (int i = 0; i < 5; i++) {
			bars2[i] = new Image(new Texture("images/transition-bar2.png"));
			bars2[i].setPosition(0, i * (h / 5f));
			bars2[i].setWidth(w);
			bars2[i].setHeight(h / 5f);
			bars2[i].setScaling(Scaling.fill);
			transitionBars2.addActor(bars2[i]);
		}
		stage2.addActor(transitionBars2);

		mapRenderer = new OrthogonalTiledMapRenderer(map, 1/64f);
		mapRenderer.setView((OrthographicCamera) stage1.getCamera());

		start = new TextLabel("START", 1, 1.5f, Align.center);
		start.setPosition(w * 0.8f, h * 0.14f);
		start.setVisible(false);
		text.addActor(start);
		start.addListener(new LabelListener(start) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (readyToStart) return true;
				click.play(Main.SOUND_VOLUME);
				transitionToLevel();
				start.addAction(Actions.sequence(Actions.delay(3f), Actions.run(() -> {
					music.stop();
					game.currentScreen = new GameScreen(game, atLevel);
					game.setScreen(game.currentScreen);
				})));

				readyToStart = true;

				return true;
			}
		});

		levelName = new TextLabel("The Grasslands, Part 1", 1, 2f, Align.center);
		levelName.setPosition(w * 0.03f, h * 0.20f);
		levelName.setVisible(false);
		text.addActor(levelName);

		levelTime = new TextLabel("time: 0:00", 1, 1.5f, Align.center);
		levelTime.setPosition(w * 0.08f, h * 0.06f);
		levelTime.setVisible(false);
		text.addActor(levelTime);

		stage2.addActor(text);
		stage2.addActor(transitionBars);

		progress = 0;
		mapSpeed = 0.4f;
		canMove = false;
		readyToStart = false;

		click = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));
		music = Gdx.audio.newMusic(Gdx.files.internal("audio/map.mp3"));
		music.setVolume(Main.MUSIC_VOLUME);
		music.setLooping(true);
		music.play();
		if (atLevel != 0) transitionToMap();
		else {
			transitionBars2.setVisible(false);
			blank.addAction(sequence(Actions.delay(0.2f), Actions.fadeOut(1.8f)));
		}

		if (Main.levelsUnlocked < level || level == 1) {
			if (Main.levelsUnlocked <= 8) Main.levelsUnlocked = level;
			if (level >= 9) { // reached end of game
				blank.setVisible(true);
				blank.addAction(sequence(Actions.fadeOut(0), Actions.delay(3f), Actions.fadeIn(4f)));
				SequenceAction sq = sequence(Actions.delay(2.5f), run(() -> atLevel = level),
						Actions.delay(6f), run(() -> {
							music.stop();
							game.currentScreen = new WinScreen(game);
							game.setScreen(game.currentScreen);
						}
						));
				stage1.addAction(sq);
			} else {
				if (Arrays.asList(new Integer[]{2, 3, 5, 7}).contains(level)) {
					stage1.addAction(sequence(Actions.delay(2f), run(() -> playCutscene(level)))); // wait until transition finishes
				} else {
					stage1.addAction(sequence(Actions.delay(2f), run(() -> atLevel = level))); // wait until transition finishes
				}
			}
		} else {
			stage1.addAction(sequence(Actions.delay(2f), run(() -> progress = 1))); // already completed level so stay there
		}

	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(255 / 255f, 255 / 255f, 255 / 255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Vector3 cam = viewport.getCamera().position;
		if (Gdx.input.isKeyJustPressed(Input.Keys.D) && canMove && atLevel + 1 <= Main.levelsUnlocked) {
			if (atLevel - 1 < levelPositions.size) {
				canMove = false;
				levelName.setVisible(false);
				levelTime.setVisible(false);
				start.setVisible(false);
				atLevel += 1;
				player.setDrawable(new TextureRegionDrawable(new Texture("images/pig.png")));
			}
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.A) && canMove) {
			if (atLevel > 1) {
				canMove = false;
				levelName.setVisible(false);
				start.setVisible(false);
				levelTime.setVisible(false);
				atLevel -= 1;
				player.setDrawable(new TextureRegionDrawable(new Texture("images/pig-flipped.png")));
			}
		}
		float newX = MathUtils.lerp(levelPositions.get(fromLevel), levelPositions.get(atLevel), progress);
		// the below lines make it so that the camera doesn't draw past the boundaries, instead moving the player
		float exactMiddle = (float) (viewport.getScreenWidth() / viewport.getScreenHeight()) * (viewport.getWorldWidth() / 2);
		//System.out.println(exactMiddle);
		cam.x = Math.max(newX, exactMiddle);
		viewport.getCamera().position.x = cam.x;

		if (cam.x > mapWidth - exactMiddle) cam.x = mapWidth - exactMiddle;
		player.setPosition(newX - player.getWidth() / 2, player.getY());
		cam.y = mapHeight / 2f;
		if (fromLevel != atLevel) progress += delta * mapSpeed;
		progress = MathUtils.clamp(progress, 0 ,1);
		if (progress == 1) {
			fromLevel = atLevel;
			progress = 0;
			if (atLevel != 9) { // if we aren't at the end
				levelName.setVisible(true);
				start.setVisible(true);
				levelTime.setVisible(true);
			}
			stage1.addAction(sequence(Actions.delay(0.2f), run(() -> canMove = true)));
		}
		viewport.getCamera().update();

		if (player.getActions().isEmpty()) {
			SequenceAction a = sequence(Actions.delay(0.4f), Actions.moveBy(0, 0.2f),
					Actions.delay(0.4f), Actions.moveBy(0, -0.2f));
			player.addAction(a);
		}

		updateText();

		mapRenderer.setView((OrthographicCamera) viewport.getCamera());
		mapRenderer.render();

		stage1.getViewport().apply();
		stage1.act();
		stage1.draw();
		stage2.getViewport().apply();
		stage2.act();
		stage2.draw();
	}

	void transitionToLevel() {
		for (int i = 1; i < 11; i++) {
			Image b = bars[i - 1];
			if (b.getY() > 0) b.addAction(Actions.moveBy(0, -Main.HEIGHT, 3 - i/5f, Interpolation.pow2));
		}
	}

	void transitionToMap() {
		blank.setVisible(false); // since we're not loading in for the first time
		for (int i = 0; i < 5; i++) {
			Image b = bars2[i];
			int dir;
			if (i % 2 == 0) dir = 1; // if even go right
			else dir = -1; // if odd go left
			SequenceAction s = Actions.sequence(Actions.moveBy(dir * Main.WIDTH, 0, 2, Interpolation.swing));
			b.addAction(s);
		}
	}

	void processLevelPositions() {
		levelPositions = new Array<>();
		TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(2);
		for (int c = 0; c < layer.getWidth(); c++) {
			for (int r = 0; r < layer.getHeight(); r++) {
				if (layer.getCell(c, r) != null) levelPositions.add(c + 1);
			}
		}
		levelPositions.add(122); // for end of game
	}

	void playCutscene(int level) {
		Array<TextLabel[]> cutscene = StoryNarrator.getCutscene(level);
		final int[] cutsceneIndex = {0};

		final TextLabel[] speaker = {cutscene.get(cutsceneIndex[0])[0]};
		speaker[0].setPosition(Main.WIDTH * 0.02f, Main.HEIGHT * 0.25f);
		stage2.addActor(speaker[0]);

		final TextLabel[] dialogue = {cutscene.get(cutsceneIndex[0])[1]};
		dialogue[0].setPosition(Main.WIDTH * 0.14f, Main.HEIGHT * 0.07f);
		stage2.addActor(dialogue[0]);


		TextLabel next = new TextLabel("next >", 1, 1f, Align.right);
		next.setPosition(Main.WIDTH * 0.88f, Main.HEIGHT * 0.05f);
		stage2.addActor(next);
		next.addListener(new LabelListener(next) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				cutsceneIndex[0] += 1;
				if (cutsceneIndex[0] < cutscene.size) {
					speaker[0].setText(cutscene.get(cutsceneIndex[0])[0].getText());
					dialogue[0].setText(cutscene.get(cutsceneIndex[0])[1].getText());
				} else {
					next.addAction(sequence(Actions.delay(0.5f), Actions.run(() -> atLevel = level)));
					next.setVisible(false);
					speaker[0].setVisible(false);
					dialogue[0].setVisible(false);
				}

				return true;
			}
		});
	}

	void updateText() {
		String t = "";
		switch (atLevel) {
			case 1:
				t = "The Grasslands, Part 1";
				break;
			case 2:
				t = "The Grasslands, Part 2";
				break;
			case 3:
				t = "The Marshlands, Part 1";
				break;
			case 4:
				t = "The Marshlands, Part 2";
				break;
			case 5:
				t = "The Mountains, Part 1";
				break;
			case 6:
				t = "The Mountains, Part 2";
				break;
			case 7:
				t = "The Forest, Part 1";
				break;
			case 8:
				t = "The Forest, Part 2";
				break;
		}
		levelName.setText(t);
		if (atLevel > 0) levelTime.setText(Main.convertTimeToString(Main.levelTimes[atLevel - 1]));
	}

	@Override
	public void dispose() {
		stage1.dispose();
		stage2.dispose();
		music.dispose();
		mapRenderer.dispose();
		map.dispose();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		stage1.getViewport().update(width, height, true);
		stage2.getViewport().update(width, height,true);
	}

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