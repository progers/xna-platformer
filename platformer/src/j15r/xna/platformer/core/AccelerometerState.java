package j15r.xna.platformer.core;

class AccelerometerState {
  // Gets the accelerometer's current value in G-force.
  public Vector3 Acceleration() {
    return acceleration;
  }

  Vector3 acceleration;

  // Gets whether or not the accelerometer is active and running.
  public boolean IsActive() {
    return isActive;
  }

  boolean isActive;

  // Initializes a new AccelerometerState.
  // <param name="acceleration">The current acceleration (in G-force) of the
  // accelerometer.</param>
  // <param name="isActive">Whether or not the accelerometer is
  // active.</param>
  public AccelerometerState(Vector3 acceleration, boolean isActive) {
    this.acceleration = acceleration;
    this.isActive = isActive;
  }

  // Returns a string containing the values of the Acceleration and IsActive
  // properties.
  // <returns>A new string describing the state.</returns>
  public String ToString() {
    return "Acceleration: " + acceleration + ", IsActive: " + isActive;
  }
}