package j15r.xna.platformer.core;

// TODO: lots
public class GamePadState {

  public static class ThumbStick {

    public Vector2 Left() {
      // TODO Auto-generated method stub
      return Vector2.Zero;
    }
  }

  private static GamePadState gamepad = new GamePadState();

  public static GamePadState GetState() {
    return gamepad;
  }

  private ThumbStick stick = new ThumbStick();

  public ThumbStick ThumbSticks() {
    return stick;
  }

  public boolean IsButtonDown(Buttons dpadleft) {
    // TODO Auto-generated method stub
    return false;
  }
}
