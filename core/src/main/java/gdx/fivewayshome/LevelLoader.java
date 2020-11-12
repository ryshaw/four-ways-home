package gdx.fivewayshome;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

import java.util.*;

public class LevelLoader {
	World world;
	int mapWidth, mapHeight;
	Music track;
	TiledMap map;
	SpawnPoint spawn;
	EndPoint end;
	Player player;
	Image bgImage;
	Array<Checkpoint> checkpoints;
	Array<Player> characters;

	LevelLoader(World w, int level) {
		this.world = w;
		selectLevel(level);
		addCharacters(Main.levelsUnlocked);
		processMap(map);
	}

	void selectLevel(int level) {
		TmxMapLoader mapLoader = new TmxMapLoader();
		TmxMapLoader.Parameters par = new TmxMapLoader.Parameters();
		par.textureMinFilter = Texture.TextureFilter.Nearest;
		par.textureMagFilter = Texture.TextureFilter.Nearest;
		switch (level) {
			case 1:
				map = mapLoader.load("grasslands1.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/grasslands.wav"));
				bgImage = new Image(new Texture("images/grasslands-bg.png"));
				break;
			case 2:
				map = mapLoader.load("grasslands2.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/grasslands.wav"));
				bgImage = new Image(new Texture("images/grasslands-bg.png"));
				break;
			case 3:
				map = mapLoader.load("marshlands1.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/marshlands.mp3"));
				bgImage = new Image(new Texture("images/marshlands-bg.png"));
				break;
			case 4:
				map = mapLoader.load("marshlands2.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/marshlands.mp3"));
				bgImage = new Image(new Texture("images/marshlands-bg.png"));
				break;
			case 5:
				map = mapLoader.load("mountains1.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/mountains.mp3"));
				bgImage = new Image(new Texture("images/mountains-bg.png"));
				break;
			case 6:
				map = mapLoader.load("mountains2.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/mountains.mp3"));
				bgImage = new Image(new Texture("images/mountains-bg.png"));
				break;
			case 7:
				map = mapLoader.load("forest1.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/forest.mp3"));
				bgImage = new Image(new Texture("images/forest-bg.png"));
				break;
			case 8:
				map = mapLoader.load("forest2.tmx", par);
				track = Gdx.audio.newMusic(Gdx.files.internal("audio/forest.mp3"));
				bgImage = new Image(new Texture("images/forest-bg.png"));
				break;
		}
	}

	void addCharacters(int level) {
		characters = new Array<>();
		characters.add(new Pig());
		if (level >= 3) {
			characters.add(new Frog());
			if (level >= 5) {
				characters.add(new Goat());
				if (level >= 7) {
					characters.add(new Cat());
				}
			}
		}
	}

	public int getMapWidth() {
		return mapWidth;
	}

	public int getMapHeight() {
		return mapHeight;
	}

	public Music getTrack() {
		return track;
	}

	public TiledMap getMap() {
		return map;
	}

	public SpawnPoint getSpawn() {
		return spawn;
	}

	public EndPoint getEnd() {
		return end;
	}

	public Player getPlayer() {
		return player;
	}

	public Image getBgImage() {
		return bgImage;
	}

	public Array<Checkpoint> getCheckpoints() {
		return checkpoints;
	}

	public Array<Player> getCharacters() {
		return characters;
	}

	// processMap uses an algorithm to clump the tiles into column blocks and row blocks,
	// making it much more efficient to do the physics step each frame
	private void processMap(TiledMap map) {
		MapProperties mP = map.getProperties();
		mapWidth = mP.get("width", Integer.class);
		mapHeight = mP.get("height", Integer.class);
		BodyDef staticBodyDef = new BodyDef();
		staticBodyDef.type = BodyDef.BodyType.StaticBody;
		BodyDef kinematicBodyDef = new BodyDef();
		kinematicBodyDef.type = BodyDef.BodyType.KinematicBody;
		BodyDef dynamicBodyDef = new BodyDef();
		dynamicBodyDef.type = BodyDef.BodyType.DynamicBody;

		TiledMapTileLayer background = (TiledMapTileLayer) map.getLayers().get(0);

		Body body;
		HashMap<Vector2, Ground> oneTileBlocks = new HashMap<>();

		List<Integer> groundTileIDs = new ArrayList<>();
		for (int i = 1; i <= 10; i++) groundTileIDs.add(i);
		groundTileIDs.addAll(Arrays.asList(23, 24, 25, 26, 31, 32, 47));
		for (int i = 49; i <= 55; i++) groundTileIDs.add(i);

		checkpoints = new Array<>();

		for (int column = 0; column < background.getWidth(); column++) {
			int row = 0;
			Array<Integer> grassTileRows = new Array<>();


			//this first part creates all columns
			while (row < background.getHeight()) {
				TiledMapTileLayer.Cell nextCell = background.getCell(column, row);
				while (nextCell != null && groundTileIDs.contains(nextCell.getTile().getId())) { // grass is first row
					nextCell.getTile().setBlendMode(TiledMapTile.BlendMode.ALPHA);
					grassTileRows.add(row);
					row++;
					nextCell = background.getCell(column, row);
				}

				if (!grassTileRows.isEmpty()) {
					body = world.createBody(staticBodyDef);
					PolygonShape groundBox = new PolygonShape();
					groundBox.setAsBox(0.5f, 0.5f * grassTileRows.size);

					float average = 0;
					for (Integer integer : grassTileRows) average += integer;

					average = average / grassTileRows.size;
					Vector2 position = new Vector2(column + 0.5f, average + 0.5f);
					body.setTransform(position, 0);
					body.createFixture(groundBox, 0.0f);
					groundBox.dispose();
					Ground g = new Ground(body, position, grassTileRows.size);
					if (g.size == 1) oneTileBlocks.put(new Vector2(column, average), g);
				}

				if (grassTileRows.isEmpty()) row++;
				grassTileRows.clear();
			}
		}

		//this second part combines all the one-tile blocks into a row block
		for (int row = 0; row < background.getHeight(); row++) {
			int column = 0;
			Array<Integer> rowOfBlocks = new Array<>();
			while (column < background.getWidth()) {
				Vector2 coord = new Vector2(column, row);
				while (oneTileBlocks.containsKey(coord)) {
					rowOfBlocks.add(column);
					Ground g = oneTileBlocks.get(coord);
					world.destroyBody(g.body);
					g.dispose();
					column++;
					coord.set(column, row);
				}

				if (!rowOfBlocks.isEmpty()) {
					body = world.createBody(staticBodyDef);
					PolygonShape groundBox = new PolygonShape();
					groundBox.setAsBox(0.5f * rowOfBlocks.size, 0.5f);

					float average = 0;
					for (Integer integer : rowOfBlocks) average += integer;

					average = average / rowOfBlocks.size;
					Vector2 position = new Vector2(average + 0.5f, row + 0.5f);
					body.setTransform(position, 0);
					body.createFixture(groundBox, 0.0f);
					groundBox.dispose();
					new Ground(body, position, rowOfBlocks.size);
				}

				if (rowOfBlocks.isEmpty()) column++;
				rowOfBlocks.clear();
			}
		}

		for (int r = 0; r < background.getHeight(); r++) {
			for (int c = 0; c < background.getWidth(); c++) {
				TiledMapTileLayer.Cell cell = background.getCell(c, r);

				if (cell != null) {
					TiledMapTile t = cell.getTile();

					if (t.getId() == 14) { // start
						t.setBlendMode(TiledMapTile.BlendMode.ALPHA);
						body = world.createBody(staticBodyDef);
						spawn = new SpawnPoint(body, new Vector2(c + 0.5f, r + 1.5f));
						cell.setTile(null); // so you don't see the texture
					} else if (t.getId() == 15) { // end
						t.setBlendMode(TiledMapTile.BlendMode.ALPHA);
						body = world.createBody(staticBodyDef);
						end = new EndPoint(body, new Vector2(c + 0.5f, r + 0.5f));
					} else if (t.getId() == 60) { // checkpoint
						t.setBlendMode(TiledMapTile.BlendMode.ALPHA);
						body = world.createBody(staticBodyDef);
						checkpoints.add(new Checkpoint(body, new Vector2(c + 0.5f, r + 0.5f)));
					}  else if (t.getId() == 40) { // thorns
						t.setBlendMode(TiledMapTile.BlendMode.ALPHA);
						body = world.createBody(staticBodyDef);
						new Thorns(body, new Vector2(c + 0.5f, r + 0.5f));
					}   else if (t.getId() == 36) { // enemy spawn
						t.setBlendMode(TiledMapTile.BlendMode.ALPHA);
						body = world.createBody(dynamicBodyDef);
						new Enemy(body, new Vector2(c + 0.5f, r + 1f));
						cell.setTile(null); // so you don't see the texture
					}   else if (t.getId() == 59) { // thorns
						t.setBlendMode(TiledMapTile.BlendMode.ALPHA);
						body = world.createBody(staticBodyDef);
						new TutorialSign(body, new Vector2(c + 0.5f, r + 0.5f));
					}
				}
			}
		}

		if (map.getLayers().size() == 1) return;

		TiledMapTileLayer foreground = (TiledMapTileLayer) map.getLayers().get(1);
		for (int r = 0; r < foreground.getHeight(); r++) {
			for (int c = 0; c < foreground.getWidth(); c++) {
				TiledMapTileLayer.Cell cell = foreground.getCell(c, r);

				if (cell != null) {
					TiledMapTile t = cell.getTile();

					if (t.getId() == 22 || t.getId() == 42) { // top of water
						t.setBlendMode(TiledMapTile.BlendMode.ALPHA);
						body = world.createBody(staticBodyDef);
						new WaveTile(body, new Vector2(c + 0.5f, r + 0.5f));
					}
				}
			}
		}
	}
}
