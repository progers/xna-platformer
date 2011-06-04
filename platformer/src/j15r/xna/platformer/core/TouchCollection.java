package j15r.xna.platformer.core;

import java.util.ArrayList;
import java.util.List;

// Provides extension methods for the TouchCollection type.
public class TouchCollection {

  // Determines if there are any touches on the screen.
  // <param name="touchState">The current TouchCollection.</param>
  // <returns>True if there are any touches in the Pressed or Moved state,
  // false otherwise</returns>
  public boolean AnyTouch() {
    for (TouchLocation location : Touches()) {
      if (location.State() == TouchLocationState.Pressed
          || location.State() == TouchLocationState.Moved) {
        return true;
      }
    }
    return false;
  }

  private List<TouchLocation> Touches() {
    // TODO: something
    return new ArrayList<TouchLocation>();
  }
}
