package j15r.xna.platformer.core;

import static forplay.core.ForPlay.assetManager;
import forplay.core.AssetWatcher;
import forplay.core.Surface;

// A monster who is impeding the progress of our fearless adventurer.
class Enemy {

  static void loadAssets(AssetWatcher watcher) {
    watcher.add(assetManager().getImage("Sprites/MonsterA/Run.png"));
    watcher.add(assetManager().getImage("Sprites/MonsterA/Idle.png"));
    watcher.add(assetManager().getImage("Sprites/MonsterB/Run.png"));
    watcher.add(assetManager().getImage("Sprites/MonsterB/Idle.png"));
    watcher.add(assetManager().getImage("Sprites/MonsterC/Run.png"));
    watcher.add(assetManager().getImage("Sprites/MonsterC/Idle.png"));
    watcher.add(assetManager().getImage("Sprites/MonsterD/Run.png"));
    watcher.add(assetManager().getImage("Sprites/MonsterD/Idle.png"));
  }

  public Level Level() {
    return level;
  }

  Level level;

  // Position in world space of the bottom center of this enemy.
  public Vector2 Position() {
    return position;
  }

  Vector2 position;

  private Rectangle localBounds;

  // Gets a rectangle which bounds this enemy in world space.
  public Rectangle BoundingRectangle() {
    int left = (int) (Math.round(Position().X - sprite.Origin().X) + localBounds.Left);
    int top = (int) (Math.round(Position().Y - sprite.Origin().Y) + localBounds.Top);

    return new Rectangle(left, top, localBounds.Width, localBounds.Height);
  }

  private Animation runAnimation;
  private Animation idleAnimation;

  // Animations
  private AnimationPlayer sprite = new AnimationPlayer();

  // The direction this enemy is facing and moving along the X axis.
  private FaceDirection direction = FaceDirection.Left;

  // How long this enemy has been waiting before turning around.
  private float waitTime;

  // How long to wait before turning around.
  private static final float MaxWaitTime = 0.5f;

  // The speed at which this enemy moves along the X axis.
  private static final float MoveSpeed = 64.0f;

  // Constructs a new Enemy.
  public Enemy(Level level, Vector2 position, String spriteSet) {
    this.level = level;
    this.position = position;

    LoadContent(spriteSet);
  }

  // Loads a particular enemy sprite sheet and sounds.
  public void LoadContent(String spriteSet) {
    // Load animations.
    spriteSet = "Sprites/" + spriteSet + "/";
    runAnimation = new Animation(assetManager().getImage(spriteSet + "Run.png"), 0.1f, true);
    idleAnimation = new Animation(assetManager().getImage(spriteSet + "Idle.png"), 0.15f, true);
    sprite.PlayAnimation(idleAnimation);

    // Calculate bounds within texture size.
    int width = (int) (idleAnimation.FrameWidth() * 0.35);
    int left = (idleAnimation.FrameWidth() - width) / 2;
    int height = (int) (idleAnimation.FrameWidth() * 0.7);
    int top = idleAnimation.FrameHeight() - height;
    localBounds = new Rectangle(left, top, width, height);
  }

  // Paces back and forth along a platform, waiting at either end.
  public void Update(float gameTime) {
    float elapsed = gameTime;

    // Calculate tile position based on the side we are walking towards.
    float posX = Position().X + localBounds.Width / 2 * (int) direction.value;
    int tileX = (int) Math.floor(posX / Tile.Width) - (int) direction.value;
    int tileY = (int) Math.floor(Position().Y / Tile.Height);

    if (waitTime > 0) {
      // Wait for some amount of time.
      waitTime = Math.max(0.0f, waitTime - gameTime);
      if (waitTime <= 0.0f) {
        // Then turn around.
        direction = direction == FaceDirection.Left ? FaceDirection.Right : FaceDirection.Left;
      }
    } else {
      // If we are about to run into a wall or off a cliff, start waiting.
      if (Level().GetCollision((int) (tileX + direction.value), tileY - 1) == TileCollision.Impassable
          || Level().GetCollision((int) (tileX + direction.value), tileY) == TileCollision.Passable) {
        waitTime = MaxWaitTime;
      } else {
        // Move in the current direction.
        Vector2 velocity = new Vector2(direction.value * MoveSpeed * elapsed, 0.0f);
        position = position.add(velocity);
      }
    }
  }

  // Draws the animated enemy.
  public void Draw(float gameTime, Surface surf) {
    // Stop running when the game is paused or before turning around.
    if (!Level().Player().IsAlive() || Level().ReachedExit() || Level().TimeRemaining() == 0
        || waitTime > 0) {
      sprite.PlayAnimation(idleAnimation);
    } else {
      sprite.PlayAnimation(runAnimation);
    }

    // Draw facing the way the enemy is moving.
    boolean flip = direction.value > 0;
    sprite.Draw(gameTime, surf, Position(), flip);
  }
}

// Facing direction along the X axis.
enum FaceDirection {
  Left(-1), Right(1);

  float value;

  FaceDirection(float value) {
    this.value = value;
  }
}
