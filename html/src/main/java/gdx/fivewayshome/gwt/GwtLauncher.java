package gdx.fivewayshome.gwt;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.badlogic.gdx.backends.gwt.preloader.Preloader;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import gdx.fivewayshome.Main;

/** Launches the GWT application. */
public class GwtLauncher extends GwtApplication {
	////USE THIS CODE FOR A FIXED SIZE APPLICATION
		@Override
		public GwtApplicationConfiguration getConfig () {
			GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(1600, 900);
			//Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
			Window.enableScrolling(false);
			cfg.preferFlash = false;
			return cfg;
			//return new GwtApplicationConfiguration(1280, 720);
		}

	@Override
	public Preloader.PreloaderCallback getPreloaderCallback() {
		return createPreloaderPanel(GWT.getHostPageBaseURL() + "loading.gif");
	}

	@Override
	protected void adjustMeterPanel(Panel meterPanel, Style meterStyle) {
		meterPanel.setStyleName("gdx-meter");
		meterPanel.addStyleName("nostripes");
		Style meterPanelStyle = meterPanel.getElement().getStyle();
		meterPanelStyle.setBackgroundColor("#e7fdff");
		meterPanelStyle.setColor("09b3c0");
		meterStyle.setProperty("backgroundColor", "#45bdc6");
		meterStyle.setProperty("backgroundImage", "none");
	}

	//END CODE FOR FIXED SIZE APPLICATION
	//UNCOMMENT THIS CODE FOR A RESIZABLE APPLICATION
	/*//PADDING is to avoid scrolling in iframes, set to 20 if you have problems
	private static final int PADDING = 0;

	@Override
	public GwtApplicationConfiguration getConfig() {
		int w = Window.getClientWidth() - PADDING;
		int h = Window.getClientHeight() - PADDING;
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(w, h);
		Window.enableScrolling(false);
		Window.setMargin("0");
		Window.addResizeHandler(new ResizeListener());
		cfg.preferFlash = false;
		return cfg;
	}

	class ResizeListener implements ResizeHandler {
		@Override
		public void onResize(ResizeEvent event) {
			if (Gdx.graphics.isFullscreen()) return;
			int width = event.getWidth() - PADDING;
			int height = event.getHeight() - PADDING;
			getRootPanel().setWidth("" + width + "px");
			getRootPanel().setHeight("" + height + "px");
			getApplicationListener().resize(width, height);
			Gdx.graphics.setWindowedMode(width, height);
		}
	}*/
	//END OF CODE FOR RESIZABLE APPLICATION


	@Override
	public ApplicationListener createApplicationListener () { return new Main(); }
}
