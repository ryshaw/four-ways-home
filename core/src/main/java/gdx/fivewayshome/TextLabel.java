package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class TextLabel extends Label {
	BitmapFont font1 = new BitmapFont(Gdx.files.internal("aprilsans.fnt"));

	TextLabel(CharSequence text, int s, float scale, int align) {
		super(text, new LabelStyle(new BitmapFont(), Color.BLACK));

		int w = Main.WIDTH;
		float trueScale = (w * scale) / 1600;

		this.setStyle(getStyle(s));
		GlyphLayout layout = new GlyphLayout(this.getStyle().font, this.getText());
		this.setSize(layout.width * trueScale, layout.height * trueScale);
		this.setFontScale(trueScale);
		this.setAlignment(align);
		this.setBounds(0, 0, this.getWidth(), this.getHeight());
	}

	private LabelStyle getStyle(int s) {
		LabelStyle labelStyle = new LabelStyle();

		switch (s) {
			case 1: // normal text
				labelStyle.font = font1;
				labelStyle.fontColor = Color.BLACK;
				break;
			case 2: // opening story text
				labelStyle.font = font1;
				labelStyle.font.getData().setLineHeight(100f);
				labelStyle.fontColor = Color.BLACK;
				break;
			case 3: // level complete text
				labelStyle.font = font1;
				labelStyle.fontColor = Color.NAVY;
				break;
			case 4: // splash screen text
				labelStyle.font = font1;
				labelStyle.fontColor = Color.WHITE;
				break;
		}
		return labelStyle;
	}
}
