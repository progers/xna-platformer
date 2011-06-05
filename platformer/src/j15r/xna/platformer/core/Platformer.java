package j15r.xna.platformer.core;

import static forplay.core.ForPlay.*;

import forplay.core.AssetWatcher;
import forplay.core.Game;
import forplay.core.Image;
import forplay.core.ResourceCallback;
import forplay.core.Surface;
import forplay.core.SurfaceLayer;

public class Platformer implements Game {

  // Global content.
  // private SpriteFont hudFont;

  private Image winOverlay;
  private Image loseOverlay;
  private Image diedOverlay;

  // Meta-level game state.
  private int levelIndex = -1;
  private Level level;
  private boolean wasContinuePressed;

  // When the time remaining is less than the warning time, it blinks on the hud
  private static float WarningTime = 30 * 1000;

  // We store our input states so that we only poll once per frame,
  // then we use the same input state wherever needed
  private GamePadState gamePadState;
  private KeyboardState keyboardState;
  private TouchCollection touchState;
  private AccelerometerState accelerometerState;

  // The number of levels in the Levels directory of our content. We assume that
  // levels in our content are 0-based and that all numbers under this constant
  // have a level file present. This allows us to not need to check for the file
  // or handle exceptions, both of which can add unnecessary time to level
  // loading.
  private static final int numberOfLevels = 3;
  private SurfaceLayer layer;

  public Platformer() {
    Accelerometer.Initialize();
  }

  private AssetWatcher watcher;

  // LoadContent will be called once per game and is the place to load
  // all of your content.
  @Override
  public void init() {
    keyboardState = KeyboardState.GetState();
    keyboard().setListener(keyboardState);

    graphics().setSize(800, 480);

    // Load fonts
    // hudFont = Content.Load<SpriteFont>("Fonts/Hud");
    layer = graphics().createSurfaceLayer(graphics().width(), graphics().height());
    graphics().rootLayer().add(layer);

    // Load overlay textures
    winOverlay = assetManager().getImage("Overlays/you_win.png");
    loseOverlay = assetManager().getImage("Overlays/you_lose.png");
    diedOverlay = assetManager().getImage("Overlays/you_died.png");

    assetManager().getSound("Sounds/Music").play();

    watcher = new AssetWatcher(new AssetWatcher.Listener() {
      @Override
      public void error(Throwable e) {
        log().error(e.getMessage());
      }

      @Override
      public void done() {
        LoadNextLevel();
      }
    });
    Player.loadAssets(watcher);
    Enemy.loadAssets(watcher);
    Gem.loadAssets(watcher);
    watcher.start();
  }

  // Allows the game to run logic such as updating the world,
  // checking for collisions, gathering input, and playing audio.
  // <param name="gameTime">Provides a snapshot of timing values.</param>
  @Override
  public void update(float delta) {
    if (level == null) {
      return;
    }

    // Handle polling for our input and handling high-level input
    HandleInput();

    // update our level, passing down the GameTime along with all of our input
    // states
    level.Update(delta / 1000, keyboardState, gamePadState, touchState, accelerometerState,
        DisplayOrientation.LandscapeRight);
  }

  private void HandleInput() {
    // get all of our input states
    keyboardState = KeyboardState.GetState();
    gamePadState = GamePadState.GetState();
    touchState = TouchPanel.GetState();
    accelerometerState = Accelerometer.GetState();

    // Exit the game when back is pressed.
    // if (gamePadState.Buttons.Back == ButtonState.Pressed)
    // Exit();

    boolean continuePressed =
        keyboardState.IsKeyDown(' ') || gamePadState.IsButtonDown(Buttons.A)
            || touchState.AnyTouch();

    // Perform the appropriate action to advance the game and
    // to get the player back to playing.
    if (!wasContinuePressed && continuePressed) {
      if (!level.Player().IsAlive()) {
        level.StartNewLife();
      } else if (level.TimeRemaining() == 0) {
        if (level.ReachedExit())
          LoadNextLevel();
        else
          ReloadCurrentLevel();
      }
    }

    wasContinuePressed = continuePressed;
  }

  private void LoadNextLevel() {
    // move to the next level
    levelIndex = (levelIndex + 1) % numberOfLevels;

    // Load the level.
    String levelPath = "Levels/" + levelIndex + ".txt";
    assetManager().getText(levelPath, new ResourceCallback<String>() {
      @Override
      public void error(Throwable err) {
        log().error(err.getMessage());
      }

      @Override
      public void done(String resource) {
        level = new Level(resource, levelIndex);
      }
    });
  }

  private void ReloadCurrentLevel() {
    --levelIndex;
    LoadNextLevel();
  }

  // Draws the game from background to foreground.
  // <param name="gameTime">Provides a snapshot of timing values.</param>
  @Override
  public void paint(float alpha) {
    if (level == null) {
      return;
    }

    Surface surf = layer.surface();
    surf.clear();
    // TODO: Change this to alpha
    level.Draw(17.0f / 1000, surf);

    DrawHud();
  }

  private void DrawHud() {
    Rectangle titleSafeArea = new Rectangle(0, 0, graphics().width(), graphics().height());
    Vector2 hudLocation = new Vector2(titleSafeArea.Left, titleSafeArea.Top);
    Vector2 center =
        new Vector2(titleSafeArea.Left + titleSafeArea.Width / 2.0f, titleSafeArea.Top
            + titleSafeArea.Height / 2.0f);

    // Draw time remaining. Uses modulo division to cause blinking when the
    // player is running out of time.
    String timeString =
        "TIME: " + Integer.toString((int) (level.TimeRemaining() / 60)) + ":"
            + Integer.toString((int) level.TimeRemaining());
    int timeColor;
    if (level.TimeRemaining() > WarningTime || level.ReachedExit()
        || (int) (level.TimeRemaining()) % 2 == 0) {
      timeColor = 0xff00ffff;
    } else {
      timeColor = 0xffff0000;
    }
    // TODO DrawShadowedString(hudFont, timeString, hudLocation, timeColor);

    // Draw score
    // TODO float timeHeight = hudFont.MeasureString(timeString).Y;
    // TODO DrawShadowedString(hudFont, "SCORE: " + level.Score.ToString(),
    // hudLocation + new Vector2(0.0f, timeHeight * 1.2f), Color.Yellow);

    // Determine the status overlay message to show.
    Image status = null;
    if (level.TimeRemaining() == 0) {
      if (level.ReachedExit()) {
        status = winOverlay;
      } else {
        status = loseOverlay;
      }
    } else if (!level.Player().IsAlive()) {
      status = diedOverlay;
    }

    if (status != null) {
      // Draw status message.
      Vector2 statusSize = new Vector2(status.width(), status.height());
      layer.surface().drawImage(status, center.X - status.width() / 2, center.Y - status.height() / 2);
    }
  }

  @Override
  public int updateRate() {
    return 17;
  }

  // TODO: font
  // private void DrawShadowedString(SpriteFont font, String value, Vector2
  // position, Color color)
  // {
  // spriteBatch.DrawString(font, value, position + new Vector2(1.0f, 1.0f),
  // Color.Black);
  // spriteBatch.DrawString(font, value, position, color);
  // }
}
