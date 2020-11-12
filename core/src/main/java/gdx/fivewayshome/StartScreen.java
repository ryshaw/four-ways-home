package gdx.fivewayshome;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FitViewport;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class StartScreen implements Screen {
	private final Stage stage;
	Music music;
	Sound click;
	boolean credits, options;
	Image sleepImage;
	Image[] bars;
	Animation<Texture> sleep;
	boolean readyToStart;
	float animationTime;

	StartScreen(final Main game, boolean comingFromWin) {
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
		Main.WIDTH = stage.getViewport().getScreenWidth();
		Main.HEIGHT = stage.getViewport().getScreenHeight();
		w = Main.WIDTH;
		h = Main.HEIGHT;
		// that was insanely hard for no reason. anyway,

		Gdx.input.setInputProcessor(stage);
		click = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));
		credits = false;
		options = false;
		readyToStart = false;

		Image bg = new Image(new Texture("images/colored_land.png"));
		bg.setWidth(w);
		bg.setHeight(h);
		bg.setScaling(Scaling.fill);
		stage.addActor(bg);

		Array<Texture> sleeps = new Array<>();
		for (int i = 1; i < 6; i++) {
			Texture t = new Texture("images/pig-asleep/pig-asleep" + i + ".png");
			sleeps.add(t);
		}
		sleep = new Animation<>(0.6f, sleeps, Animation.PlayMode.LOOP_PINGPONG);
		sleepImage = new Image(sleep.getKeyFrame(0));
		sleepImage.setScale(4);
		sleepImage.setPosition(w * 0.4f, h * 0.05f);
		stage.addActor(sleepImage);
		animationTime = 0;

		Label title = new TextLabel("Four Ways Home", 1, 2.5f, Align.center);
		title.setPosition(w / 2f - title.getWidth() / 2, h * 0.85f);
		stage.addActor(title);

		Label start = new TextLabel("new game", 1, 1f, Align.left) {
			public void act(float delta) { this.setVisible(!credits && !options); }
		};
		start.setPosition(w * (0.02f), h * (0.6f));
		stage.addActor(start);
		start.addListener(new LabelListener(start) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				if (readyToStart) return true;
				music.stop();
				click.play(Main.SOUND_VOLUME);
				stage.addAction(sequence(Actions.fadeOut(0.5f), run(() -> {
					game.currentScreen = new StoryScreen(game); //START GAME
					game.setScreen(game.currentScreen);
				})));
				readyToStart = true;

				return true;
			}
		});

		Label optionsButton = new TextLabel("options", 1, 1f, Align.left) {
			public void act(float delta) { this.setVisible(!credits && !options); }
		};
		optionsButton.setPosition(w * (0.02f), h * (0.5f));
		stage.addActor(optionsButton);
		optionsButton.addListener(new LabelListener(optionsButton) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				options = true;
				return true;
			}
		});

		Label creditButton = new TextLabel("credits", 1, 1f, Align.left) {
			public void act(float delta) { this.setVisible(!credits && !options); }
		};
		creditButton.setPosition(w * (0.02f), h * (0.4f));
		stage.addActor(creditButton);
		creditButton.addListener(new LabelListener(creditButton) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				credits = true;
				return true;
			}
		});

		Label version = new TextLabel("v1.3", 1, 0.5f, Align.center);
		version.setPosition(w - 10 - version.getWidth(), h * 0.02f);
		version.setAlignment(Align.center);
		stage.addActor(version);


		String attribution = getCredits();
		Label longCredits = new TextLabel(attribution, 1, 1f, Align.left) {
			public void act(float delta) { this.setVisible(credits); }
		};
		longCredits.setPosition(w * 0.1f, h * 0.4f);
		stage.addActor(longCredits);

		Label back = new TextLabel("back", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(credits || options); }
		};
		back.setPosition(w * 0.8f, h * 0.1f);
		stage.addActor(back);
		back.addListener(new LabelListener(back) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				credits = false;
				options = false;
				return true;
			}
		});

		Label musicOption = new TextLabel("music:", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		musicOption.setPosition(w * 0.35f, h * 0.7f);
		stage.addActor(musicOption);

		Label musicText = new TextLabel("", 1, 1.5f, Align.center) {
			public void act(float delta) {
				this.setVisible(options);
				this.setText("" + Math.round(Main.MUSIC_VOLUME * 100));
			}
		};
		musicText.setPosition(w * 0.581f, h * 0.7f);
		stage.addActor(musicText);

		Label musicMinus = new TextLabel("-", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		musicMinus.setPosition(w * 0.51f, h * 0.7f);
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

		Label musicPlus = new TextLabel("+", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		musicPlus.setPosition(w * 0.63f, h * 0.7f);
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

		Label soundOption = new TextLabel("sound:", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		soundOption.setPosition(w * 0.35f, h * 0.6f);
		stage.addActor(soundOption);

		Label soundText = new TextLabel("", 1, 1.5f, Align.center) {
			public void act(float delta) {
				this.setVisible(options);
				this.setText("" + Math.round(Main.SOUND_VOLUME * 100));
			}
		};
		soundText.setPosition(w * 0.581f, h * 0.6f);
		stage.addActor(soundText);

		Label soundMinus = new TextLabel("-", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		soundMinus.setPosition(w * 0.51f, h * 0.6f);
		soundMinus.addListener(new LabelListener(soundMinus) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Main.SOUND_VOLUME -= 0.1f;
				Main.SOUND_VOLUME = MathUtils.clamp(Main.SOUND_VOLUME, 0f, 1.0f);
				click.play(Main.SOUND_VOLUME);
				return true;
			}
		});
		stage.addActor(soundMinus);

		Label soundPlus = new TextLabel("+", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		soundPlus.setPosition(w * 0.63f, h * 0.6f);
		soundPlus.addListener(new LabelListener(soundPlus) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				Main.SOUND_VOLUME += 0.1f;
				Main.SOUND_VOLUME = MathUtils.clamp(Main.SOUND_VOLUME, 0f, 1.0f);
				click.play(Main.SOUND_VOLUME);
				return true;
			}
		});
		stage.addActor(soundPlus);

		Label fullscreenOption = new TextLabel("fullscreen:", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		fullscreenOption.setPosition(w * 0.35f, h * 0.5f);
		if (Gdx.app.getType() == Application.ApplicationType.WebGL) stage.addActor(fullscreenOption);

		Label fullscreenButton = new TextLabel("off", 1, 1.5f, Align.center) {
			public void act(float delta) {
				this.setVisible(options);
				if (Gdx.graphics.isFullscreen()) this.setText("on");
				else this.setText("off");
			}
		};
		fullscreenButton.setPosition(w * 0.6f, h * 0.5f);
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

		Label translateOption = new TextLabel("language:", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		translateOption.setPosition(w * 0.31f, h * 0.4f);
		stage.addActor(translateOption);

		Label translateButton = new TextLabel("english", 1, 1.5f, Align.left) {
			public void act(float delta) {
				this.setVisible(options);
				if (Main.translation == 0) this.setText("english");
				else this.setText("portuguese (br)");
			}
		};
		translateButton.setPosition(w * 0.53f, h * 0.4f);
		translateButton.addListener(new LabelListener(translateButton) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				if (Main.translation == 0) Main.translation = 1;
				else Main.translation = 0;
				return true;
			}
		});
		stage.addActor(translateButton);

		Label inputOption = new TextLabel("controls:", 1, 1.5f, Align.center) {
			public void act(float delta) { this.setVisible(options); }
		};
		inputOption.setPosition(w * 0.16f, h * 0.3f);
		stage.addActor(inputOption);

		Label inputButton = new TextLabel("WASD", 1, 1.5f, Align.left) {
			public void act(float delta) {
				this.setVisible(options);
				if (Main.JUMP_KEY == Input.Keys.W) this.setText("WASD");
				else {
					if (Main.translation == 0) this.setText("arrow keys and space");
					else this.setText("setas e espaço");
				}
			}
		};
		inputButton.setPosition(w * 0.36f, h * 0.3f);
		inputButton.addListener(new LabelListener(inputButton) {
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				click.play(Main.SOUND_VOLUME);
				if (Main.JUMP_KEY == Input.Keys.W) {
					Main.JUMP_KEY = Input.Keys.SPACE;
					Main.RIGHT_KEY = Input.Keys.RIGHT;
					Main.LEFT_KEY = Input.Keys.LEFT;
				}
				else {
					Main.JUMP_KEY = Input.Keys.W;
					Main.RIGHT_KEY = Input.Keys.D;
					Main.LEFT_KEY = Input.Keys.A;				}
				return true;
			}
		});
		stage.addActor(inputButton);

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

		Image fadeIn = new Image(new Texture("images/black.png"));
		fadeIn.setWidth(w);
		fadeIn.setHeight(h);
		fadeIn.setScaling(Scaling.fill);
		stage.addActor(fadeIn);

		music = Gdx.audio.newMusic(Gdx.files.internal("audio/river.mp3"));
		music.setVolume(Main.MUSIC_VOLUME);
		music.setLooping(true);
		music.play();

		Gdx.input.setInputProcessor(stage);
		if (Gdx.app.getType() == Application.ApplicationType.WebGL) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayModes()[0]);
		if (comingFromWin) {
			fadeIn.setVisible(false);
			transitionToLevel();
		} else {
			transitionBars.setVisible(false);
			fadeIn.addAction(Actions.sequence(Actions.fadeOut(1f), Actions.run(() -> fadeIn.setVisible(false))));
		}
	}

	private String getCredits() {
		return "creator: ryan shaw          composer: gabyyu lovelity \n" +
				"   ryshaw.itch.io         soundcloud.com/gabyyu_lovelity\n" +
				"\n            story panels done by uni & zip\n" +
				"\n full attribution at: ryshaw.itch.io/four-ways-home";
	}


	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(255 / 255f, 255 / 255f, 255 / 255f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		animationTime += delta;
		TextureRegionDrawable texture = new TextureRegionDrawable(sleep.getKeyFrame(animationTime));
		sleepImage.setDrawable(texture);

		stage.act();
		stage.draw();
	}

	void transitionToLevel() {
		for (int i = 1; i < 11; i++) {
			Image b = bars[i - 1];
			SequenceAction s = Actions.sequence(Actions.moveBy(0, -Main.HEIGHT, 1f + (i / 4f), Interpolation.smoother));
			b.addAction(s);
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