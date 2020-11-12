package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;

public class StoryNarrator {
	private static final FileHandle ENGLISH = Gdx.files.internal("story.txt");
	private static final FileHandle PORT = Gdx.files.internal("ptbr.txt");


	public static Array<TextLabel> getOpening() {
		Array<TextLabel> array = new Array<>();
		FileHandle story;
		if (Main.translation == 0) story = ENGLISH;
		else story = PORT;

		String[] reader = story.readString("UTF-8").split("\n");
		int index = 0;
		while (!reader[index].contains("Opening")) {
			index++; // keep moving along the script until we get to Opening
		}

		index++;
		while (!reader[index].contains("End")) {
			String[] words = reader[index].split(" ");
			String line = "";
			int length = 0;
			int numLines = 1;
			for (String word : words) {
				line = line.concat(word + " ");
				length += word.length() + 1;

				if (length > 47 && numLines < 4) {
					line = line.concat("\n");
					length = 0;
					numLines++;
				}
			}

			// this fixes a bug so that all of the lines start at the same exact spot when drawn
			while (numLines < 4) {
				line = line.concat("\n");
				numLines++;
			}

			array.add(new TextLabel(line, 2, 1.1f, Align.left));
			index++;
		}

		return array;
	}

	public static Array<Image> getOpeningFrames() {
		int w = Main.WIDTH;

		Array<Image> array = new Array<>();
		for (int i = 1; i <= 10; i++) {
			Image image;
			if (i <= 5) image = new Image(new Texture("images/opening/FWH_" + i + ".png"));
			else image = new Image(new Texture("images/opening/FWH_" + i + ".jpg"));
			image.setScale((w / 1600f) * 2);

			image.setScaling(Scaling.fill);
			array.add(image);
		}
		return array;
	}

	public static TextLabel getTutorial(int level) {
		FileHandle story = ENGLISH;
		if (Main.translation == 1) story = PORT;

		String[] reader = story.readString("UTF-8").split("\n");
		int index = 0;

		String option = "";
		if (level == 1) {
			if (Main.JUMP_KEY == Input.Keys.W) option = " WASD";
			else option = " Arrows";
		}

		while (!reader[index].contains("Tutorial Level " + level + option)) {
			index++; // keep moving along the script until we get there
		}

		index++;
		String line = "";
		String[] words = reader[index].split(" ");
		int length = 0;
		int numLines = 1;
		for (String word : words) {
			line = line.concat(word + " ");
			length += word.length() + 1;

			if (length > 74 && numLines < 3) {
				line = line.concat("\n");
				length = 0;
				numLines++;
			}
		}

		// this fixes a bug so that all of the lines start at the same exact spot when drawn
		while (numLines < 3) {
			line = line.concat("\n");
			numLines++;
		}


		return new TextLabel(line, 1, 0.5f, Align.left);
	}

	public static Array<TextLabel[]> getCutscene(int level) {
		Array<TextLabel[]> array = new Array<>();
		FileHandle story = ENGLISH;
		if (Main.translation == 1) story = PORT;

		String[] reader = story.readString("UTF-8").split("\n");
		int index = 0;
		while (!reader[index].contains("Before Level " + level)) {
			index++;
		}

		index++;
		while (!reader[index].contains("End")) {
			TextLabel[] labels = new TextLabel[2];
			String[] words = reader[index].split(" ");
			String line = "";
			int length = 0;
			int numLines = 1;

			String speaker = words[0]; // either N, P, F, G, or C
			labels[0] = new TextLabel(speaker, 2, 0.8f, Align.center);

			for (int i = 1; i < words.length; i++) {
				String word = words[i];
				line = line.concat(word + " ");
				length += word.length() + 1;

				if (length > 60) {
					line = line.concat("\n");
					length = 0;
					numLines++;
				}
			}

			// this fixes a bug so that all of the lines start at the same exact spot when drawn
			while (numLines < 3) {
				line = line.concat("\n");
				numLines++;
			}

			labels[1] = new TextLabel(line, 2, 0.8f, Align.left);
			array.add(labels);
			index++;
		}

		return array;
	}

	public static TextLabel getEnding() {
		String text = "";
		FileHandle story = ENGLISH;
		if (Main.translation == 1) story = PORT;

		String[] reader = story.readString("UTF-8").split("\n");
		int index = 0;
		while (!reader[index].contains("Ending")) {
			index++;
		}

		index++;
		while (!reader[index].contains("End")) {
			String[] words = reader[index].split(" ");
			String line = "";
			int length = 0;
			int numLines = 1;
			for (String word : words) {
				length += word.length() + 1;

				if (length > 70) {
					line = line.concat("\n");
					line = line.concat(word + " ");
					length = word.length() + 1;
					numLines++;
				} else {
					line = line.concat(word + " ");
				}
			}

			// this fixes a bug so that all of the lines start at the same exact spot when drawn
			while (numLines < 3) {
				line = line.concat("\n");
				numLines++;
			}

			text = text.concat(line);
			index++;
		}

		return new TextLabel(text, 2, 0.8f, Align.left);
	}
}
