package j15r.xna.platformer.core;

public class Vector2 {

  public static final Vector2 Zero = new Vector2(0, 0);

  public float X;
  public float Y;

  public Vector2(float X, float Y) {
    this.X = X;
    this.Y = Y;
  }

  public Vector2() {
  }

  public Vector2 sub(Vector2 v) {
    return new Vector2(X - v.X, Y - v.Y);
  }

  public float LengthSquared() {
    return X * X + Y * Y;
  }

  public Vector2 mul(float a) {
    return new Vector2(X * a, Y * a);
  }

  public Vector2 add(Vector2 v) {
    return new Vector2(X + v.X, Y + v.Y);
  }

  public Vector2 mul(Vector2 v) {
    return new Vector2(X * v.X, Y * v.Y);
  }
}
