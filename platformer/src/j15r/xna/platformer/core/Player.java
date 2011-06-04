package j15r.xna.platformer.core;

import static forplay.core.ForPlay.assetManager;

import forplay.core.Keyboard;
import forplay.core.Sound;
import forplay.core.Surface;

// Our fearless adventurer!
class Player {
  // Animations
  private Animation idleAnimation;
  private Animation runAnimation;
  private Animation jumpAnimation;
  private Animation celebrateAnimation;
  private Animation dieAnimation;
  private AnimationPlayer sprite = new AnimationPlayer();

  // Sounds
  private Sound killedSound;
  private Sound jumpSound;
  private Sound fallSound;

  public Level Level() {
    return level;
  }

  Level level;

  public boolean IsAlive() {
    return isAlive;
  }

  boolean isAlive;

  // Physics state
  public Vector2 Position() {
    return position;
  }

  public void setPosition(Vector2 value) {
    position = value;
  }

  Vector2 position;

  private float previousBottom;

  public Vector2 Velocity() {
    return velocity;
  }

  public void setVelocity(Vector2 value) {
    velocity = value;
  }

  Vector2 velocity;

  // Constants for controling horizontal movement
  private static final float MoveAcceleration = 13000.0f;
  private static final float MaxMoveSpeed = 1750.0f;
  private static final float GroundDragFactor = 0.48f;
  private static final float AirDragFactor = 0.58f;

  // Constants for controlling vertical movement
  private static final float MaxJumpTime = 0.35f;
  private static final float JumpLaunchVelocity = -3500.0f;
  private static final float GravityAcceleration = 3400.0f;
  private static final float MaxFallSpeed = 550.0f;
  private static final float JumpControlPower = 0.14f;

  // Input configuration
  private static final float MoveStickScale = 1.0f;
  private static final float AccelerometerScale = 1.5f;
  private static final Buttons JumpButton = Buttons.A;

  // Gets whether or not the player's feet are on the ground.
  public boolean IsOnGround() {
    return isOnGround;
  }

  boolean isOnGround;

  // Current user movement input.
  private float movement;

  // Jumping state
  private boolean isJumping;
  private boolean wasJumping;
  private float jumpTime;

  private Rectangle localBounds;

  // Gets a rectangle which bounds this player in world space.
  public Rectangle BoundingRectangle() {
    int left = (int) (Math.round(Position().X - sprite.Origin().X) + localBounds.Left);
    int top = (int) (Math.round(Position().Y - sprite.Origin().Y) + localBounds.Top);

    return new Rectangle(left, top, localBounds.Width, localBounds.Height);
  }

  // Constructors a new player.
  public Player(Level level, Vector2 position) {
    this.level = level;
    LoadContent();
    Reset(position);
  }

  // Loads the player sprite sheet and sounds.
  public void LoadContent() {
    // Load animated textures.
    idleAnimation = new Animation(assetManager().getImage("Sprites/Player/Idle.png"), 0.1f, true);
    runAnimation = new Animation(assetManager().getImage("Sprites/Player/Run.png"), 0.1f, true);
    jumpAnimation = new Animation(assetManager().getImage("Sprites/Player/Jump.png"), 0.1f, false);
    celebrateAnimation =
        new Animation(assetManager().getImage("Sprites/Player/Celebrate.png"), 0.1f, false);
    dieAnimation = new Animation(assetManager().getImage("Sprites/Player/Die.png"), 0.1f, false);

    // Calculate bounds within texture size.
    int width = (int) (idleAnimation.FrameWidth() * 0.4);
    int left = (idleAnimation.FrameWidth() - width) / 2;
    int height = (int) (idleAnimation.FrameWidth() * 0.8);
    int top = idleAnimation.FrameHeight() - height;
    localBounds = new Rectangle(left, top, width, height);

    // Load sounds.
    killedSound = assetManager().getSound("Sounds/PlayerKilled");
    jumpSound = assetManager().getSound("Sounds/PlayerJump");
    fallSound = assetManager().getSound("Sounds/PlayerFall");
  }

  // Resets the player to life.
  // <param name="position">The position to come to life at.</param>
  public void Reset(Vector2 position) {
    this.position = position;
    this.velocity = Vector2.Zero;
    isAlive = true;
    sprite.PlayAnimation(idleAnimation);
  }

  // Handles input, performs physics, and animates the player sprite.
  // <remarks>
  // We pass in all of the input states so that our game is only polling the
  // hardware
  // once per frame. We also pass the game's orientation because when using
  // the accelerometer,
  // we need to reverse our motion when the orientation is in the
  // LandscapeRight orientation.
  // </remarks>
  public void Update(float gameTime, KeyboardState keyboardState, GamePadState gamePadState,
      TouchCollection touchState, AccelerometerState accelState, DisplayOrientation orientation) {
    GetInput(keyboardState, gamePadState, touchState, accelState, orientation);

    ApplyPhysics(gameTime);

    if (IsAlive() && IsOnGround()) {
      if (Math.abs(Velocity().X) - 0.02f > 0) {
        sprite.PlayAnimation(runAnimation);
      } else {
        sprite.PlayAnimation(idleAnimation);
      }
    }

    // Clear input.
    movement = 0.0f;
    isJumping = false;
  }

  // Gets player horizontal movement and jump commands from input.
  private void GetInput(KeyboardState keyboardState, GamePadState gamePadState,
      TouchCollection touchState, AccelerometerState accelState, DisplayOrientation orientation) {
    // Get analog horizontal movement.
    movement = gamePadState.ThumbSticks().Left().X * MoveStickScale;

    // Ignore small movements to prevent running in place.
    if (Math.abs(movement) < 0.5f)
      movement = 0.0f;

    // Move the player with accelerometer
    if (Math.abs(accelState.Acceleration().Y) > 0.10f) {
      // set our movement speed
      movement = MathHelper.Clamp(-accelState.Acceleration().Y * AccelerometerScale, -1f, 1f);

      // if we're in the LandscapeLeft orientation, we must reverse our movement
      if (orientation == DisplayOrientation.LandscapeRight)
        movement = -movement;
    }

    // If any digital horizontal movement input is found, override the analog
    // movement.
    if (gamePadState.IsButtonDown(Buttons.DPadLeft) || keyboardState.IsKeyDown(Keyboard.KEY_LEFT)
        || keyboardState.IsKeyDown('A')) {
      movement = -1.0f;
    } else if (gamePadState.IsButtonDown(Buttons.DPadRight)
        || keyboardState.IsKeyDown(Keyboard.KEY_RIGHT) || keyboardState.IsKeyDown('D')) {
      movement = 1.0f;
    }

    // Check if the player wants to jump.
    isJumping =
        gamePadState.IsButtonDown(JumpButton) || keyboardState.IsKeyDown(' ')
            || keyboardState.IsKeyDown(Keyboard.KEY_UP) || keyboardState.IsKeyDown('W')
            || touchState.AnyTouch();
  }

  // Updates the player's velocity and position based on input, gravity, etc.
  public void ApplyPhysics(float gameTime) {
    float elapsed = gameTime;

    Vector2 previousPosition = Position();

    // Base velocity is a combination of horizontal movement control and
    // acceleration downward due to gravity.
    velocity.X += movement * MoveAcceleration * elapsed;
    velocity.Y =
        MathHelper.Clamp(velocity.Y + GravityAcceleration * elapsed, -MaxFallSpeed, MaxFallSpeed);

    velocity.Y = DoJump(velocity.Y, gameTime);

    // Apply pseudo-drag horizontally.
    if (IsOnGround())
      velocity.X *= GroundDragFactor;
    else
      velocity.X *= AirDragFactor;

    // Prevent the player from running faster than his top speed.
    velocity.X = MathHelper.Clamp(velocity.X, -MaxMoveSpeed, MaxMoveSpeed);

    // Apply velocity.
    setPosition(Position().add(velocity.mul(elapsed)));
    setPosition(new Vector2((float) Math.round(Position().X), (float) Math.round(Position().Y)));

    // If the player is now colliding with the level, separate them.
    HandleCollisions();

    // If the collision stopped us from moving, reset the velocity to zero.
    if (Position().X == previousPosition.X)
      velocity.X = 0;

    if (Position().Y == previousPosition.Y)
      velocity.Y = 0;
  }

  // Calculates the Y velocity accounting for jumping and
  // animates accordingly.
  // <remarks>
  // During the accent of a jump, the Y velocity is completely
  // overridden by a power curve. During the decent, gravity takes
  // over. The jump velocity is controlled by the jumpTime field
  // which measures time into the accent of the current jump.
  // </remarks>
  // <param name="velocityY">
  // The player's current velocity along the Y axis.
  // </param>
  // <returns>
  // A new Y velocity if beginning or continuing a jump.
  // Otherwise, the existing Y velocity.
  // </returns>
  private float DoJump(float velocityY, float gameTime) {
    // If the player wants to jump
    if (isJumping) {
      // Begin or continue a jump
      if ((!wasJumping && IsOnGround()) || jumpTime > 0.0f) {
        if (jumpTime == 0.0f)
          jumpSound.play();

        jumpTime += (float) gameTime;
        sprite.PlayAnimation(jumpAnimation);
      }

      // If we are in the ascent of the jump
      if (0.0f < jumpTime && jumpTime <= MaxJumpTime) {
        // Fully override the vertical velocity with a power curve that gives
        // players more control over the top of the jump
        velocityY = JumpLaunchVelocity
          * (1.0f - (float) Math.pow(jumpTime / MaxJumpTime, JumpControlPower));
      } else {
        // Reached the apex of the jump
        jumpTime = 0.0f;
      }
    } else {
      // Continues not jumping or cancels a jump in progress
      jumpTime = 0.0f;
    }
    wasJumping = isJumping;

    return velocityY;
  }

  // Detects and resolves all collisions between the player and his
  // neighboring
  // tiles. When a collision is detected, the player is pushed away along one
  // axis to prevent overlapping. There is some special logic for the Y axis
  // to
  // handle platforms which behave differently depending on direction of
  // movement.
  private void HandleCollisions() {
    // Get the player's bounding rectangle and find neighboring tiles.
    Rectangle bounds = BoundingRectangle();
    int leftTile = (int) Math.floor((float) bounds.Left / Tile.Width);
    int rightTile = (int) Math.ceil(((float) bounds.Right() / Tile.Width)) - 1;
    int topTile = (int) Math.floor((float) bounds.Top / Tile.Height);
    int bottomTile = (int) Math.ceil(((float) bounds.Bottom() / Tile.Height)) - 1;

    // Reset flag to search for ground collision.
    isOnGround = false;

    // For each potentially colliding tile,
    for (int y = topTile; y <= bottomTile; ++y) {
      for (int x = leftTile; x <= rightTile; ++x) {
        // If this tile is collidable,
        TileCollision collision = Level().GetCollision(x, y);
        if (collision != TileCollision.Passable) {
          // Determine collision depth (with direction) and magnitude.
          Rectangle tileBounds = Level().GetBounds(x, y);
          Vector2 depth = Rectangle.GetIntersectionDepth(bounds, tileBounds);
          if (depth != Vector2.Zero) {
            float absDepthX = Math.abs(depth.X);
            float absDepthY = Math.abs(depth.Y);

            // Resolve the collision along the shallow axis.
            if (absDepthY < absDepthX || collision == TileCollision.Platform) {
              // If we crossed the top of a tile, we are on the ground.
              if (previousBottom <= tileBounds.Top)
                isOnGround = true;

              // Ignore platforms, unless we are on the ground.
              if (collision == TileCollision.Impassable || IsOnGround()) {
                // Resolve the collision along the Y axis.
                setPosition(new Vector2(Position().X, Position().Y + depth.Y));

                // Perform further collisions with the new bounds.
                bounds = BoundingRectangle();
              }
            } else if (collision == TileCollision.Impassable) // Ignore
                                                              // platforms.
            {
              // Resolve the collision along the X axis.
              setPosition(new Vector2(Position().X + depth.X, Position().Y));

              // Perform further collisions with the new bounds.
              bounds = BoundingRectangle();
            }
          }
        }
      }
    }

    // Save the new bounds bottom.
    previousBottom = bounds.Bottom();
  }

  // Called when the player has been killed.
  // <param name="killedBy">
  // The enemy who killed the player. This parameter is null if the player was
  // not killed by an enemy (fell into a hole).
  // </param>
  public void OnKilled(Enemy killedBy) {
    isAlive = false;

    if (killedBy != null)
      killedSound.play();
    else
      fallSound.play();

    sprite.PlayAnimation(dieAnimation);
  }

  // Called when this player reaches the level's exit.
  public void OnReachedExit() {
    sprite.PlayAnimation(celebrateAnimation);
  }

  // Draws the animated player.
  public void Draw(float gameTime, Surface surf) {
    // Flip the sprite to face the way we are moving.
    boolean flip = (Velocity().X > 0);

    // Draw that sprite.
    sprite.Draw(gameTime, surf, Position(), flip);
  }
}
