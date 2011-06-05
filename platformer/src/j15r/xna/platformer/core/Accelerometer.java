package j15r.xna.platformer.core;

import forplay.core.Keyboard;

// A static encapsulation of accelerometer input to provide games with a
// polling-based
// accelerometer system.
public class Accelerometer {
  // the accelerometer sensor on the device
  private static AccelerometerDevice accelerometer = new AccelerometerDevice();

  // we use this to keep the last known value from the accelerometer callback
  private static Vector3 nextValue = new Vector3();

  // we want to prevent the Accelerometer from being initialized twice.
  private static boolean isInitialized = false;

  // whether or not the accelerometer is active
  private static boolean isActive = false;

  // Initializes the Accelerometer for the current game. This method can only
  // be called once per game.
  public static void Initialize() {
    // make sure we don't initialize the Accelerometer twice
    if (isInitialized) {
      throw new RuntimeException("Initialize can only be called once");
    }

    // try to start the sensor only on devices, catching the exception if it
    // fails
    // TODO: get a real device somehow
    {
      // we always return isActive on emulator because we use the arrow
      // keys for simulation which is always available.
      isActive = true;
    }

    // remember that we are initialized
    isInitialized = true;
  }

  private static void sensor_ReadingChanged(Object sender, Vector3 e) {
    // store the accelerometer value in our variable to be used on the next
    // Update
    nextValue = new Vector3((float) e.X, (float) e.Y, (float) e.Z);
  }

  // Gets the current state of the accelerometer.
  // <returns>A new AccelerometerState with the current state of the
  // accelerometer.</returns>
  public static AccelerometerState GetState() {
    // make sure we've initialized the Accelerometer before we try to get the
    // state
    if (!isInitialized) {
      throw new RuntimeException("You must Initialize before you can call GetState");
    }

    // create a new value for our state
    Vector3 stateValue = new Vector3();

    // if the accelerometer is active
    if (isActive) {
      // if (DeviceType == DeviceType.Device)
      // {
      // // if we're on device, we'll just grab our latest reading from the
      // accelerometer
      // stateValue = nextValue;
      // }
      // else
      {
        // if we're in the emulator, we'll generate a fake acceleration value
        // using the arrow keys
        // press the pause/break key to toggle keyboard input for the emulator
        // TODO: get this from somewhere.
        KeyboardState keyboardState = KeyboardState.GetState();

        stateValue.Z = -1;

        if (keyboardState.IsKeyDown(Keyboard.KEY_LEFT))
          stateValue.X--;
        if (keyboardState.IsKeyDown(Keyboard.KEY_RIGHT))
          stateValue.X++;
        if (keyboardState.IsKeyDown(Keyboard.KEY_UP))
          stateValue.Y++;
        if (keyboardState.IsKeyDown(Keyboard.KEY_DOWN))
          stateValue.Y--;

        stateValue.Normalize();
      }
    }

    return new AccelerometerState(stateValue, isActive);
  }
}
