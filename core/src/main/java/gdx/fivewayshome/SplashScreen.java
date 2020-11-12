package gdx.fivewayshome;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class SplashScreen implements Screen {
	private final Stage stage;
	int counter;
	boolean switchCount;
	Sound click;

	SplashScreen(final Main game) {
		Camera camera = new OrthographicCamera();
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		stage = new Stage(new FitViewport(w, h, camera));
		Gdx.input.setInputProcessor(stage);
		click = Gdx.audio.newSound(Gdx.files.internal("audio/click.wav"));
		counter = 0;
		switchCount = false;

		// the next series of lines creates the opening animation:
		// "ryshaw" gets typed out on the screen, in the style of a word processor.
		// then it gets deleted and the screen fades out.

		Label ryshaw = new TextLabel("|", 4, 2f, Align.left);
		ryshaw.setPosition(w * 0.4f, h * 0.5f);
		Action type = Actions.sequence(Actions.delay(0.3f), Actions.run(() -> ryshaw.setText(getText())));
		RepeatAction typeAction = Actions.repeat(6, type);
		Action stay = Actions.sequence(Actions.delay(0.5f), Actions.run(() -> ryshaw.setText("ryshaw")));
		Action stay2 = Actions.sequence(Actions.delay(0.5f), Actions.run(() -> ryshaw.setText("ryshaw|")));
		Action stay3 = Actions.sequence(Actions.delay(0.5f), Actions.run(() -> ryshaw.setText("ryshaw")));
		Action stay4 = Actions.sequence(Actions.delay(0.5f), Actions.run(() -> ryshaw.setText("ryshaw|")));
		Action delete = Actions.sequence(Actions.delay(0.15f), Actions.run(() -> ryshaw.setText(getText())));
		RepeatAction deleteAction = Actions.repeat(6, delete);
		Action finish1 = Actions.sequence(Actions.delay(0.5f), Actions.run(() -> ryshaw.setText("")));
		Action finish2 = Actions.sequence(Actions.delay(0.5f), Actions.run(() -> ryshaw.setText("|")));
		SequenceAction finishSequence = Actions.sequence(finish1, finish2);
		RepeatAction finishAction = Actions.repeat(5, finishSequence);
		ryshaw.setVisible(false);
		SequenceAction startSequence = Actions.sequence(Actions.delay(1.5f), Actions.run(() -> ryshaw.setVisible(true)));
		Action d = Actions.delay(0.3f);

		SequenceAction sq = Actions.sequence(startSequence, d, typeAction, stay, stay2, stay3, stay4, deleteAction, finishAction);
		ryshaw.addAction(sq);
		stage.addActor(ryshaw);

		Gdx.input.setInputProcessor(stage);
		if (Gdx.app.getType() == Application.ApplicationType.WebGL) Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayModes()[0]);
		stage.addAction(Actions.sequence(Actions.delay(8.5f), Actions.fadeOut(1.5f), Actions.run(() -> {
			game.currentScreen = new StartScreen(game, false);
			game.setScreen(game.currentScreen);
		})));
	}

	String getText() {
		if (counter < 6 && !switchCount) counter += 1;
		if (switchCount && counter > 0) counter -= 1;
		if (counter == 6) switchCount = true;
		return "ryshaw".substring(0, counter) + "|";
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act();
		stage.draw();
	}

	@Override
	public void dispose() {
		stage.dispose();
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