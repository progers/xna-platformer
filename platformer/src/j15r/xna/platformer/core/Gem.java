package j15r.xna.platformer.core;

import static forplay.core.ForPlay.assetManager;
import forplay.core.AssetWatcher;
import forplay.core.Image;
import forplay.core.Sound;
import forplay.core.Surface;

// A valuable item the player can collect.
class Gem {

  static void loadAssets(AssetWatcher watcher) {
    // Load animated textures.
    watcher.add(assetManager().getImage("Sprites/Gem.png"));
  }

  private Image texture;
  private Sound collectedSound;

  public static final int PointValue = 30;
  public int Color = 0xff00ffff;

  // The gem is animated from a base position along the Y axis.
  private Vector2 basePosition;
  private float bounce;

  public Level Level() {
    return level;
  }

  Level level;

  // Gets the current position of this gem in world space.
  public Vector2 Position() {
    return basePosition.add(new Vector2(0.0f, bounce));
  }

  // Gets a circle which bounds this gem in world space.
  public Circle BoundingCircle() {
    return new Circle(Position(), Tile.Width / 3.0f);
  }

  // Constructs a new gem.
  public Gem(Level level, Vector2 position) {
    this.level = level;
    this.basePosition = position;

    LoadContent();
  }

  // Loads the gem texture and collected sound.
  public void LoadContent() {
    texture = assetManager().getImage("Sprites/Gem.png");
    collectedSound = assetManager().getSound("Sounds/GemCollected");
  }

  // Bounces up and down in the air to entice players to collect them.
  public void Update(float gameTime) {
    // Bounce control constants
    float BounceHeight = 0.18f;
    float BounceRate = 3.0f;
    float BounceSync = -0.75f;

    // Bounce along a sine curve over time.
    // Include the X coordinate so that neighboring gems bounce in a nice wave
    // pattern.
    double t = gameTime * BounceRate + Position().X * BounceSync;
    bounce = (float) Math.sin(t) * BounceHeight * texture.height();
  }

  // Called when this gem has been collected by a player and removed from the
  // level.
  // <param name="collectedBy">
  // The player who collected this gem. Although currently not used, this
  // parameter would be
  // useful for creating special powerup gems. For example, a gem could make
  // the player invincible.
  // </param>
  public void OnCollected(Player collectedBy) {
    collectedSound.play();
  }

  // Draws a gem in the appropriate color.
  public void Draw(float gameTime, Surface surf) {
    surf.drawImage(texture, Position().X - texture.width() / 2.0f, Position().Y - texture.height() / 2.0f);
  }
}
