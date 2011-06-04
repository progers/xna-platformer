package j15r.xna.platformer.core;

import static forplay.core.ForPlay.assetManager;
import static forplay.core.ForPlay.json;
import forplay.core.Image;
import forplay.core.Json;
import forplay.core.Sound;
import forplay.core.Surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// A uniform grid of tiles with collections of gems and enemies.
// The level owns the player and controls the game's win and lose
// conditions as well as scoring.
class Level {
  // Physical structure of the level.
  private Tile[][] tiles;
  private Image[] layers;
  // The layer which entities are drawn on top of.
  private static final int EntityLayer = 2;

  // Entities in the level.
  public Player Player() {
    return player;
  }

  Player player;

  private List<Gem> gems = new ArrayList<Gem>();
  private List<Enemy> enemies = new ArrayList<Enemy>();

  // Key locations in the level.
  private Vector2 start;
  private Vector2 exit = InvalidPosition;
  private static final Vector2 InvalidPosition = new Vector2(-1, -1);

  // Level game state.
  private Random random = new Random(354668); // Arbitrary, but constant seed

  public int Score() {
    return score;
  }

  int score;

  public boolean ReachedExit() {
    return reachedExit;
  }

  boolean reachedExit;

  public float TimeRemaining() {
    return timeRemaining;
  }

  float timeRemaining;

  private static final int PointsPerSecond = 5;

  private Sound exitReachedSound;

  // Constructs a new level.
  // <param name="serviceProvider">
  // The service provider that will be used to construct a ContentManager.
  // </param>
  // <param name="fileStream">
  // A stream containing the tile data.
  // </param>
  public Level(String levelJson, int levelIndex) {
    timeRemaining = 2 * 60;

    LoadTiles(levelJson);

    // Load background layer textures. For now, all levels must
    // use the same backgrounds and only use the left-most part of them.
    layers = new Image[3];
    for (int i = 0; i < layers.length; ++i) {
      // Choose a random segment if each background layer for level variety.
      int segmentIndex = levelIndex;
      layers[i] = assetManager().getImage("Backgrounds/Layer" + i + "_" + segmentIndex + ".png");
    }

    // Load sounds.
    exitReachedSound = assetManager().getSound("Sounds/ExitReached");
  }

  // Iterates over every tile in the structure file and loads its
  // appearance and behavior. This method also validates that the
  // file is well-formed with a player start point, exit, etc.
  // <param name="fileStream">
  // A stream containing the tile data.
  // </param>
  private void LoadTiles(String levelJson) {
    Json.Object jso = json().parse(levelJson);
    Json.Array lines = jso.getArray("lines");
    int width = lines.getString(0).length();

    // Allocate the tile grid.
    tiles = new Tile[width][lines.length()];

    // Loop over every tile position,
    for (int y = 0; y < Height(); ++y) {
      for (int x = 0; x < Width(); ++x) {
        // to load each tile.
        char tileType = lines.getString(y).charAt(x);
        tiles[x][y] = LoadTile(tileType, x, y);
      }
    }

    // Verify that the level has a beginning and an end.
    if (Player() == null)
      throw new RuntimeException("A level must have a starting point.");
    if (exit == InvalidPosition)
      throw new RuntimeException("A level must have an exit.");
  }

  // Loads an individual tile's appearance and behavior.
  // <param name="tileType">
  // The character loaded from the structure file which
  // indicates what should be loaded.
  // </param>
  // <param name="x">
  // The X location of this tile in tile space.
  // </param>
  // <param name="y">
  // The Y location of this tile in tile space.
  // </param>
  // <returns>The loaded tile.</returns>
  private Tile LoadTile(char tileType, int x, int y) {
    switch (tileType) {
      // Blank space
      case '.':
        return new Tile(null, TileCollision.Passable);

        // Exit
      case 'X':
        return LoadExitTile(x, y);

        // Gem
      case 'G':
        return LoadGemTile(x, y);

        // Floating platform
      case '-':
        return LoadTile("Platform", TileCollision.Platform);

        // Various enemies
      case 'A':
        return LoadEnemyTile(x, y, "MonsterA");
      case 'B':
        return LoadEnemyTile(x, y, "MonsterB");
      case 'C':
        return LoadEnemyTile(x, y, "MonsterC");
      case 'D':
        return LoadEnemyTile(x, y, "MonsterD");

        // Platform block
      case '~':
        return LoadVarietyTile("BlockB", 2, TileCollision.Platform);

        // Passable block
      case ':':
        return LoadVarietyTile("BlockB", 2, TileCollision.Passable);

        // Player 1 start point
      case '1':
        return LoadStartTile(x, y);

        // Impassable block
      case '#':
        return LoadVarietyTile("BlockA", 7, TileCollision.Impassable);

        // Unknown tile type character
      default:
        throw new RuntimeException(
            "Unsupported tile type character '" + tileType + "' at position " + x + ", " + y);
    }
  }

  // Creates a new tile. The other tile loading methods typically chain to
  // this
  // method after performing their special logic.
  // <param name="name">
  // Path to a tile texture relative to the Content/Tiles directory.
  // </param>
  // <param name="collision">
  // The tile collision type for the new tile.
  // </param>
  // <returns>The new tile.</returns>
  private Tile LoadTile(String name, TileCollision collision) {
    return new Tile(assetManager().getImage("Tiles/" + name + ".png"), collision);
  }

  // Loads a tile with a random appearance.
  // <param name="baseName">
  // The content name prefix for this group of tile variations. Tile groups
  // are
  // name LikeThis0.png and LikeThis1.png and LikeThis2.png.
  // </param>
  // <param name="variationCount">
  // The number of variations in this group.
  // </param>
  private Tile LoadVarietyTile(String baseName, int variationCount, TileCollision collision) {
    int index = random.nextInt(variationCount);
    return LoadTile(baseName + index, collision);
  }

  // Instantiates a player, puts him in the level, and remembers where to put
  // him when he is resurrected.
  private Tile LoadStartTile(int x, int y) {
    if (Player() != null)
      throw new RuntimeException("A level may only have one starting point.");

    start = Rectangle.GetBottomCenter(GetBounds(x, y));
    player = new Player(this, start);

    return new Tile(null, TileCollision.Passable);
  }

  // Remembers the location of the level's exit.
  private Tile LoadExitTile(int x, int y) {
    if (exit != InvalidPosition)
      throw new RuntimeException("A level may only have one exit.");

    exit = GetBounds(x, y).Center();

    return LoadTile("Exit", TileCollision.Passable);
  }

  // Instantiates an enemy and puts him in the level.
  private Tile LoadEnemyTile(int x, int y, String spriteSet) {
    Vector2 position = Rectangle.GetBottomCenter(GetBounds(x, y));
    enemies.add(new Enemy(this, position, spriteSet));

    return new Tile(null, TileCollision.Passable);
  }

  // Instantiates a gem and puts it in the level.
  private Tile LoadGemTile(int x, int y) {
    Vector2 position = GetBounds(x, y).Center();
    gems.add(new Gem(this, new Vector2(position.X, position.Y)));

    return new Tile(null, TileCollision.Passable);
  }

  // Gets the collision mode of the tile at a particular location.
  // This method handles tiles outside of the levels boundries by making it
  // impossible to escape past the left or right edges, but allowing things
  // to jump beyond the top of the level and fall off the bottom.
  public TileCollision GetCollision(int x, int y) {
    // Prevent escaping past the level ends.
    if (x < 0 || x >= Width())
      return TileCollision.Impassable;
    // Allow jumping past the level top and falling through the bottom.
    if (y < 0 || y >= Height())
      return TileCollision.Passable;

    return tiles[x][y].Collision;
  }

  // Gets the bounding rectangle of a tile in world space.
  public Rectangle GetBounds(int x, int y) {
    return new Rectangle(x * Tile.Width, y * Tile.Height, Tile.Width, Tile.Height);
  }

  // Width of level measured in tiles.
  public int Width() {
    return tiles.length;
  }

  // Height of the level measured in tiles.
  public int Height() {
    return tiles[0].length;
  }

  // Updates all objects in the world, performs collision between them,
  // and handles the time limit with scoring.
  public void Update(float gameTime, KeyboardState keyboardState, GamePadState gamePadState,
      TouchCollection touchState, AccelerometerState accelState, DisplayOrientation orientation) {
    // Pause while the player is dead or time is expired.
    if (!Player().IsAlive() || TimeRemaining() == 0) {
      // Still want to perform physics on the player.
      Player().ApplyPhysics(gameTime);
    } else if (ReachedExit()) {
      // Animate the time being converted into points.
      int seconds = (int) Math.round(gameTime * 100.0f);
      seconds = Math.min(seconds, (int) Math.ceil(TimeRemaining()));
      timeRemaining -= seconds;
      score += seconds * PointsPerSecond;
    } else {
      timeRemaining -= gameTime;
      Player().Update(gameTime, keyboardState, gamePadState, touchState, accelState, orientation);
      UpdateGems(gameTime);

      // Falling off the bottom of the level kills the player.
      if (Player().BoundingRectangle().Top >= Height() * Tile.Height)
        OnPlayerKilled(null);

      UpdateEnemies(gameTime);

      // The player has reached the exit if they are standing on the ground and
      // his bounding rectangle contains the center of the exit tile. They can
      // only
      // exit when they have collected all of the gems.
      if (Player().IsAlive() && Player().IsOnGround()
          && Player().BoundingRectangle().Contains(exit)) {
        OnExitReached();
      }
    }

    // Clamp the time remaining at zero.
    if (timeRemaining < 0)
      timeRemaining = 0;
  }

  // Animates each gem and checks to allows the player to collect them.
  private void UpdateGems(float gameTime) {
    for (int i = 0; i < gems.size(); ++i) {
      Gem gem = gems.get(i);

      gem.Update(gameTime);

      if (gem.BoundingCircle().Intersects(Player().BoundingRectangle())) {
        gems.remove(i--);
        OnGemCollected(gem, Player());
      }
    }
  }

  // Animates each enemy and allow them to kill the player.
  private void UpdateEnemies(float gameTime) {
    for (Enemy enemy : enemies) {
      enemy.Update(gameTime);

      // Touching an enemy instantly kills the player
      if (enemy.BoundingRectangle().Intersects(Player().BoundingRectangle())) {
        OnPlayerKilled(enemy);
      }
    }
  }

  // Called when a gem is collected.
  // <param name="gem">The gem that was collected.</param>
  // <param name="collectedBy">The player who collected this gem.</param>
  private void OnGemCollected(Gem gem, Player collectedBy) {
    score += Gem.PointValue;

    gem.OnCollected(collectedBy);
  }

  // Called when the player is killed.
  // <param name="killedBy">
  // The enemy who killed the player. This is null if the player was not
  // killed by an
  // enemy, such as when a player falls into a hole.
  // </param>
  private void OnPlayerKilled(Enemy killedBy) {
    Player().OnKilled(killedBy);
  }

  // Called when the player reaches the level's exit.
  private void OnExitReached() {
    Player().OnReachedExit();
    exitReachedSound.play();
    reachedExit = true;
  }

  // Restores the player to the starting point to try the level again.
  public void StartNewLife() {
    Player().Reset(start);
  }

  // Draw everything in the level from background to foreground.
  public void Draw(float gameTime, Surface surf) {
    for (int i = 0; i <= EntityLayer; ++i)
      surf.drawImage(layers[i], 0, 0);

    DrawTiles(surf);

    for (Gem gem : gems)
      gem.Draw(gameTime, surf);

    Player().Draw(gameTime, surf);

    for (Enemy enemy : enemies)
      enemy.Draw(gameTime, surf);

    for (int i = EntityLayer + 1; i < layers.length; ++i)
      surf.drawImage(layers[i], 0, 0);
  }

  // Draws each tile in the level.
  private void DrawTiles(Surface surf) {
    // For each tile position
    for (int y = 0; y < Height(); ++y) {
      for (int x = 0; x < Width(); ++x) {
        // If there is a visible tile in that position
        Image texture = tiles[x][y].Texture;
        if (texture != null) {
          // Draw it in screen space.
          Vector2 position = new Vector2((float) x, (float) y).mul(Tile.Size);
          surf.drawImage(texture, position.X, position.Y);
        }
      }
    }
  }
}
