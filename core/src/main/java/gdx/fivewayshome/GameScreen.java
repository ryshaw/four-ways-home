package gdx.fivewayshome;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.Arrays;

public class GameScreen implements Screen {
	private final Main game;
	private final World world;
	private final Box2DDebugRenderer debugRenderer;
	private final FitViewport viewport;
	private final Stage stage, backgroundStage;
	Group completeGroup, gameoverGroup;
	Image[] bars, bars2;
	Image backgroundImage, tutorialImage;
	TextLabel tutorial;
	Player player;
	float accumulator = 0, switchCooldown;
	Array<Body> bodies;
	Music music;
	Sound click, soundComplete, soundGameOver;
	TiledMap map;
	static int mapWidth, mapHeight;
	OrthogonalTiledMapRenderer renderer;
	static final float unitScale = 1/32f;
	int lvl, barsFinished;
	static int characterIndex; // so enemies can switch too
	SpawnPoint spawn;
	EndPoint end;
	boolean paused, transitioning, insideCheckpoint, options;
	static float time;
	static boolean complete;
	int[] backgroundLayer, foregroundLayer;
	Array<Checkpoint> checkpoints;
	Array<Player> characters;
	Box2DCollision box2DCollision;

	GameScreen(final Main game, int level) {
		this.game = game;
		lvl = level;
		//if (Main.levelsUnlocked < level) Main.levelsUnlocked = level;
		Box2D.init();
		world = new World(new Vector2(0, -20f), true);
		debugRenderer = new Box2DDebugRenderer();

		// okay... now just simply adjust the resolution to 16:9
		// screen is 53.33 tiles wide by 30 tiles tall
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		int adjustedWidth = Math.round(h * (16 / 9f)) / 16; // out of 100%, where 100% = 1600
		int adjustedHeight = Math.round(w * (9 / 16f)) / 9; // out of 100%, where 100% = 900
		// if adjustedW > adjustedHeight, the resolution was sized down horizontally and we have less width
		// to work with. otherwise, it was sized down vertically and we have less height to work with.
		viewport = new FitViewport(20, 20 * (9 / 16f), new OrthographicCamera());
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

		backgroundStage = new Stage(new FitViewport(w, h));
		Gdx.input.setInputProcessor(stage);

		LevelLoader levelLoader = new LevelLoader(world, lvl);
		map = levelLoader.getMap();
		mapWidth = levelLoader.getMapWidth();
		mapHeight = levelLoader.getMapHeight();
		player = levelLoader.getPlayer();
		spawn = levelLoader.getSpawn();
		end = levelLoader.getEnd();
		music = levelLoader.getTrack();
		backgroundImage = levelLoader.getBgImage();
		checkpoints = levelLoader.getCheckpoints();
		characters = levelLoader.getCharacters();

		renderer = new OrthogonalTiledMapRenderer(map, unitScale);

		createStage(w, h);

		music.setVolume(Main.MUSIC_VOLUME);
		music.setLooping(true);
		music.play();

		click = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));
		soundComplete = Gdx.audio.newSound(Gdx.files.internal("audio/level-complete.mp3"));
		soundGameOver = Gdx.audio.newSound(Gdx.files.internal("audio/game-over.mp3"));

		bodies = new Array<>();

		barsFinished = 0;
		paused = true;
		time = 0;
		transitioning = false;
		complete = false;
		insideCheckpoint = false;
		options = false;
		switchCooldown = 1f;

		backgroundLayer = new int[]{0}; // ground
		if (map.getLayers().size() > 1) foregroundLayer = new int[]{1}; // water

		characterIndex = 0;
		player = characters.get(characterIndex);
		BodyDef playerDef = new BodyDef();
		playerDef.type = BodyDef.BodyType.DynamicBody;
		Body body = world.createBody(playerDef);
		Vector2 spawnPosition = spawn.position.sub(2, 0);
		player.initialize(body, spawnPosition);
		box2DCollision = new Box2DCollision(this);
		world.setContactListener(box2DCollision);

		viewport.getCamera().position.x = player.position.x;
		viewport.getCamera().position.y = player.position.y + 0.5f;
		viewport.getCamera().update();

		transitionToLevel();
	}


	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(1f, 1f, 1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float d = (float) MathUtils.clamp(delta, 0, 0.05);
		if (paused) d = 0;
		if (!complete) time += d;

		Vector3 cam = viewport.getCamera().position;

		updateParallaxBackground(cam);
		backgroundStage.getViewport().apply(false);
		backgroundStage.act();
		backgroundStage.draw();

		renderer.setView((OrthographicCamera) viewport.getCamera());
		renderer.render(backgroundLayer);

		world.getBodies(bodies);
		game.batch.setProjectionMatrix(viewport.getCamera().combined);
		game.batch.begin();
		for (Body b : bodies) {
			Entity e = (Entity) b.getUserData();
			e.update(game.batch, d);
		}
		game.batch.end();

		if (foregroundLayer != null) renderer.render(foregroundLayer);

		checkToSwitchCharacter(d);

		//debugRenderer.render(world, viewport.getCamera().combined); // DEBUG RENDERER

		float lerp = 5f;
		Vector2 pos = new Vector2(player.position.x, player.position.y + 1); // points camera above player
		cam.x += (pos.x - cam.x) * lerp * delta;
		cam.x = MathUtils.clamp(cam.x, viewport.getWorldWidth() / 2, mapWidth - viewport.getWorldWidth() / 2);
		if (time > 1.8f && !player.dead) cam.y += (pos.y - cam.y) * lerp * delta;
		viewport.getCamera().update();

		stage.getViewport().apply(true);
		stage.act();
		stage.draw();

		doPhysicsStep(d);
	}

	private void doPhysicsStep(float delta) {
		float frameTime = Math.min(delta, 0.25f);
		float timeStep = 1/60f;
		accumulator += frameTime;
		while (accumulator >= timeStep) {
			world.step(timeStep, 6, 2);
			accumulator -= timeStep;
		}
	}

	private void updateParallaxBackground(Vector3 cam) {
		float backgroundProgressX = cam.x / mapWidth;
		float backgroundProgressY = Math.abs(spawn.position.y - cam.y) / mapHeight;
		float backgroundPosX = MathUtils.lerp(0, Main.WIDTH / 2f, backgroundProgressX);
		float backgroundPosY = MathUtils.lerp(0, Main.HEIGHT / 2f, backgroundProgressY);
		backgroundStage.getActors().get(0).setPosition(-backgroundPosX, -backgroundPosY);
	}

	private void checkToSwitchCharacter(float delta) {
		switchCooldown -= delta;
		if (insideCheckpoint && switchCooldown < 0 && characters.size > 1)  {
			if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
				if (characterIndex > 0) characterIndex--;
				else characterIndex = characters.size - 1;
				switchCharacter();
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
				if (characterIndex < characters.size - 1) characterIndex++;
				else characterIndex = 0;
				switchCharacter();
			}
		}
	}

	private void switchCharacter() {
		world.destroyBody(player.body);
		BodyDef playerDef = new BodyDef();
		playerDef.type = BodyDef.BodyType.DynamicBody;
		Body b = world.createBody(playerDef);
		Vector2 p = player.position.add(0, 0.2f);
		player = characters.get(characterIndex);
		player.initialize(b, p);
		player.body.applyLinearImpulse(0f, 5f, p.x, p.y, true);
		box2DCollision.updatePlayer(player);
		switchCooldown = 1f;
		player.numContacts = 0;
	}

	private void createStage(int w, int h) {
		Label restart = new TextLabel("restart level", 1, 0.5f, Align.right) {
			public void act(float delta) { this.setVisible(options); }
		};
		restart.setPosition(w * 0.88f, h * 0.76f);
		stage.addActor(restart);
		restart.addListener(new LabelListener(restart) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (transitioning || time < 3f || GameScreen.complete || player.dead) return true;
				click.play(Main.SOUND_VOLUME);
				startTransition();
				stage.addAction(Actions.sequence(Actions.delay(3f), Actions.run(() -> {
					music.stop();
					game.currentScreen = new GameScreen(game, lvl);
					game.setScreen(game.currentScreen);
				})));

				transitioning = true;
				return true;
			}
		});

		Label optionsButton = new TextLabel("options", 1, 0.5f, Align.right);
		optionsButton.setPosition(w * 0.93f, h - optionsButton.getHeight());
		stage.addActor(optionsButton);
		optionsButton.addListener(new LabelListener(optionsButton) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				options = !options;
				return true;
			}
		});

		Label musicOption = new TextLabel("music:", 1, 0.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		musicOption.setPosition(w * 0.88f, h * 0.90f);
		stage.addActor(musicOption);

		Label musicText = new TextLabel("", 1, 0.5f, Align.center) {
			public void act(float delta) {
				this.setVisible(options);
				this.setText("" + Math.round(Main.MUSIC_VOLUME * 100));
			}
		};
		musicText.setPosition(w * 0.957f, h * 0.90f);
		stage.addActor(musicText);

		Label musicMinus = new TextLabel("-", 1, 0.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		musicMinus.setPosition(w * 0.934f, h * 0.90f);
		musicMinus.addListener(new LabelListener(musicMinus) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				Main.MUSIC_VOLUME -= 0.1f;
				Main.MUSIC_VOLUME = MathUtils.clamp(Main.MUSIC_VOLUME, 0f, 1.0f);
				music.setVolume(Main.MUSIC_VOLUME);
				return true;
			}
		});
		stage.addActor(musicMinus);

		Label musicPlus = new TextLabel("+", 1, 0.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		musicPlus.setPosition(w * 0.975f, h * 0.90f);
		musicPlus.addListener(new LabelListener(musicPlus) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				Main.MUSIC_VOLUME += 0.1f;
				Main.MUSIC_VOLUME = MathUtils.clamp(Main.MUSIC_VOLUME, 0f, 1.0f);
				music.setVolume(Main.MUSIC_VOLUME);
				return true;
			}
		});
		stage.addActor(musicPlus);

		Label soundOption = new TextLabel("sound:", 1, 0.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		soundOption.setPosition(w * 0.88f, h * 0.86f);
		stage.addActor(soundOption);

		Label soundText = new TextLabel("", 1, 0.5f, Align.center) {
			public void act(float delta) {
				this.setVisible(options);
				this.setText("" + Math.round(Main.SOUND_VOLUME * 100));
			}
		};
		soundText.setPosition(w * 0.957f, h * 0.86f);
		stage.addActor(soundText);

		Label soundMinus = new TextLabel("-", 1, 0.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		soundMinus.setPosition(w * 0.934f, h * 0.86f);
		soundMinus.addListener(new LabelListener(soundMinus) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Main.SOUND_VOLUME -= 0.1f;
				Main.SOUND_VOLUME = MathUtils.clamp(Main.SOUND_VOLUME, 0f, 1.0f);
				click.play(Main.SOUND_VOLUME);
				return true;
			}
		});
		stage.addActor(soundMinus);

		Label soundPlus = new TextLabel("+", 1, 0.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		soundPlus.setPosition(w * 0.975f, h * 0.86f);
		soundPlus.addListener(new LabelListener(soundPlus) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Main.SOUND_VOLUME += 0.1f;
				Main.SOUND_VOLUME = MathUtils.clamp(Main.SOUND_VOLUME, 0f, 1.0f);
				click.play(Main.SOUND_VOLUME);
				return true;
			}
		});
		stage.addActor(soundPlus);

		Label fullscreenOption = new TextLabel("fullscreen:", 1, 0.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		fullscreenOption.setPosition(w * 0.88f, h * 0.82f);
		if (Gdx.app.getType() == Application.ApplicationType.WebGL) stage.addActor(fullscreenOption);

		Label fullscreenButton = new TextLabel("off", 1, 0.5f, Align.center) {
			public void act(float delta) {
				this.setVisible(options);
				if (Gdx.graphics.isFullscreen()) this.setText("on");
				else this.setText("off");
			}
		};
		fullscreenButton.setPosition(w * 0.96f, h * 0.82f);
		fullscreenButton.addListener(new LabelListener(fullscreenButton) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				if (!Gdx.graphics.isFullscreen()) {
					if (Gdx.app.getType() == Application.ApplicationType.WebGL) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayModes()[0]);
					else Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
					fullscreenButton.setText("on");
				}
				else {
					Gdx.graphics.setWindowedMode(1600, 900);
					fullscreenButton.setText("off");
				}

				return true;
			}
		});
		if (Gdx.app.getType() == Application.ApplicationType.WebGL) stage.addActor(fullscreenButton);

		gameoverGroup = new Group();
		gameoverGroup.setVisible(true);
		Label over = new TextLabel("game over :(", 3, 2.5f, Align.center);
		over.setPosition(w / 2f - over.getWidth() / 2, h * (4/5f));
		gameoverGroup.addActor(over);

		Label question = new TextLabel("continue?", 3, 2f, Align.bottom);
		question.setPosition(w / 2f - question.getWidth() / 2, h * (2.5f/5f));
		gameoverGroup.addActor(question);

		Label yes = new TextLabel("yes", 3, 1.5f, Align.bottom);
		yes.setPosition(w * 0.4f - yes.getWidth() / 2f, h * (2/5f));
		gameoverGroup.addActor(yes);
		yes.addListener(new LabelListener(yes) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (transitioning || GameScreen.complete) return true;
				click.play(Main.SOUND_VOLUME);
				startTransition();
				yes.addAction(Actions.sequence(Actions.delay(3f), Actions.run(() -> {
					music.stop();
					game.currentScreen = new GameScreen(game, lvl);
					game.setScreen(game.currentScreen);
				})));

				transitioning = true;
				return true;
			}
		});

		Label no = new TextLabel("no", 3, 1.5f, Align.bottom);
		no.setPosition(w * 0.6f - no.getWidth() / 2, h * (2/5f));
		gameoverGroup.addActor(no);
		no.addListener(new LabelListener(no) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (transitioning) return true;
				click.play(Main.SOUND_VOLUME);
				transitionToMap();
				no.addAction(Actions.sequence(Actions.delay(3f), Actions.run(() -> {
					music.stop();
					game.currentScreen = new MapScreen(game, lvl);
					game.setScreen(game.currentScreen);
				})));

				transitioning = true;
				return true;
			}
		});

		for (Actor a : gameoverGroup.getChildren()) {
			a.setVisible(false);
		}
		stage.addActor(gameoverGroup);

		tutorialImage = new Image(new Texture("images/tutorial-image.png"));
		tutorialImage.setScale(w / 1600f);
		tutorialImage.setPosition(w / 2f - tutorialImage.getWidth() * tutorialImage.getScaleX() / 2f, h * 0.85f);
		tutorialImage.setVisible(false);
		tutorialImage.setScaling(Scaling.fill);
		stage.addActor(tutorialImage);

		if (Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 7}).contains(lvl)) {
			tutorial = StoryNarrator.getTutorial(lvl);
			tutorial.setPosition(w * 0.2f, h * 0.885f);
			tutorial.setVisible(false);
			stage.addActor(tutorial);
		}

		Group transitionBars = new Group();
		bars = new Image[10];
		for (int i = 0; i < 10; i++) {
			bars[i] = new Image(new Texture("images/transition-bar.png"));
			bars[i].setPosition(i * (w / 10f), 0);
			bars[i].setWidth(w / 10f);
			bars[i].setHeight(h);
			bars[i].setScaling(Scaling.fill);
			transitionBars.addActor(bars[i]);
		}
		stage.addActor(transitionBars);

		completeGroup = new Group();
		completeGroup.setVisible(true);
		Label complete = new TextLabel("level complete!", 3, 2.5f, Align.center);
		complete.setPosition(w / 2f - complete.getWidth() / 2, h * (4/5f));
		completeGroup.addActor(complete);

		Label timeLabel = new TextLabel("time", 3, 2f, Align.bottom);
		timeLabel.setPosition(w / 2f - timeLabel.getWidth() / 2, h * (3/5f));
		completeGroup.addActor(timeLabel);

		Label nextLevel = new TextLabel("continue >", 3, 1.5f, Align.bottom);
		nextLevel.setPosition(w / 2f - nextLevel.getWidth() / 2, h * (1/5f));
		completeGroup.addActor(nextLevel);
		nextLevel.addListener(new LabelListener(nextLevel) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (transitioning) return true;
				click.play(Main.SOUND_VOLUME);
				transitionToMap();
				nextLevel.addAction(Actions.sequence(Actions.delay(3f), Actions.run(() -> {
					music.stop();
					game.currentScreen = new MapScreen(game, lvl + 1);
					game.setScreen(game.currentScreen);
				})));

				transitioning = true;
				return true;
			}
		});
		for (Actor a : completeGroup.getChildren()) {
			a.setVisible(false);
		}
		stage.addActor(completeGroup);

		Group transitionBars2 = new Group();
		bars2 = new Image[5];
		for (int i = 0; i < 5; i++) {
			bars2[i] = new Image(new Texture("images/transition-bar2.png"));
			if (i % 2 == 0) bars2[i].setPosition(-w, i * (h / 5f)); // if even on left
			else bars2[i].setPosition(w, i * (h / 5f)); // if odd on right
			bars2[i].setWidth(w);
			bars2[i].setHeight(h / 5f);
			bars2[i].setScaling(Scaling.fill);

			transitionBars2.addActor(bars2[i]);
		}
		stage.addActor(transitionBars2);

		backgroundImage.setWidth(w * 1.5f);
		backgroundImage.setHeight(h * 1.5f);
		backgroundStage.addActor(backgroundImage);


	}

	void startTransition() {
		for (int i = 1; i < 11; i++) {
			Image b = bars[i - 1];
			bars[i - 1].setPosition((i - 1) * (Main.WIDTH / 10f), Main.HEIGHT);
			if (b.getY() > 0) b.addAction(Actions.moveBy(0, -Main.HEIGHT, 3 - i/5f, Interpolation.pow2));
		}
	}

	void transitionToLevel() {
		for (int i = 1; i < 11; i++) {
			Image b = bars[i - 1];
			SequenceAction s = Actions.sequence(Actions.moveBy(0, -Main.HEIGHT, 3 - i/5f, Interpolation.pow2),
					Actions.run(() -> barsFinished++), Actions.run(this::checkToSpawnPlayer));
			b.addAction(s);
		}
	}

	void transitionToMap() {
		for (int i = 0; i < 5; i++) {
			Image b = bars2[i];
			int dir;
			if (i % 2 == 0) dir = 1; // if even go right
			else dir = -1; // if odd go left
			SequenceAction s = Actions.sequence(Actions.moveBy(dir * Main.WIDTH, 0, 2, Interpolation.exp5));
			b.addAction(s);
		}
	}

	void checkToSpawnPlayer() {
		if (barsFinished == 5) {
			paused = false;
			player.body.applyLinearImpulse(12f, 12f, player.position.x, player.position.y, true);
		}
	}

	void levelComplete() {
		complete = true;
		soundComplete.play(Main.SOUND_VOLUME);
		for (int i = 0; i < completeGroup.getChildren().size; i++) {
			Actor a = completeGroup.getChild(i);
			if (i == 1) { // time
				double t = MathUtils.round(GameScreen.time * 100) / 100d;
				if (Main.levelTimes[lvl - 1] == 0) {
					Main.levelTimes[lvl - 1] = t;
				} else if (Main.levelTimes[lvl - 1] > t) {
					Main.levelTimes[lvl - 1] = t;
				}
				TextLabel l = (TextLabel) a;
				l.setText(Main.convertTimeToString(t));
			}
			a.addAction(Actions.sequence(Actions.delay((i + 1) * 1f), Actions.run(() -> a.setVisible(true))));
		}
	}

	void playerDied() {
		Main.totalDeaths += 1;
		soundGameOver.play(Main.SOUND_VOLUME);
		for (int i = 0; i < gameoverGroup.getChildren().size; i++) {
			Actor a = gameoverGroup.getChild(i);
			a.addAction(Actions.sequence(Actions.delay((i + 1) * 0.8f), Actions.run(() -> a.setVisible(true))));
		}
	}

	@Override
	public void dispose() {
		debugRenderer.dispose();
		for (Body b : bodies) {
			Entity e = (Entity) b.getUserData();
			e.dispose();
		}
		world.dispose();

		music.dispose();
		map.dispose();
		renderer.dispose();
		stage.dispose();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		stage.getViewport().update(width, height, true);
		backgroundStage.getViewport().update(width, height,true);
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