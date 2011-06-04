package j15r.xna.platformer.core;

public class Vector3 {

  public float X, Y, Z;

  public Vector3() {
  }

  public Vector3(float x, float y, float z) {
    X = x;
    Y = y;
    Z = z;
  }

  public void Normalize() {
    float len = (float) Math.sqrt(X * X + Y * Y + Z * Z);
    X /= len;
    Y /= len;
    Z /= len;
  }
}
