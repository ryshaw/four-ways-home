package gdx.fivewayshome;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class LabelListener extends InputListener {
	Label label;

	LabelListener(Label l) { label = l; }

	public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		label.setFontScale(label.getFontScaleX() + 0.2f);
	}

	public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
		label.setFontScale(label.getFontScaleX() - 0.2f);
	}
}
