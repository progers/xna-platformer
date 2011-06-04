package j15r.xna.platformer.core;

public class Circle {
  // Center position of the circle.
  public Vector2 Center;

  // Radius of the circle.
  public float Radius;

  // Constructs a new circle.
  public Circle(Vector2 position, float radius) {
    Center = position;
    Radius = radius;
  }

  // Determines if a circle intersects a rectangle.
  // <returns>True if the circle and rectangle overlap. False
  // otherwise.</returns>
  public boolean Intersects(Rectangle rectangle) {
    Vector2 v = new Vector2(
      MathHelper.Clamp(Center.X, rectangle.Left, rectangle.Right()),
      MathHelper.Clamp(Center.Y, rectangle.Top, rectangle.Bottom())
    );

    Vector2 direction = Center.sub(v);
    float distanceSquared = direction.LengthSquared();

    return ((distanceSquared > 0) && (distanceSquared < Radius * Radius));
  }
}
