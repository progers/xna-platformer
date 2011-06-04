package j15r.xna.platformer.core;

import forplay.core.Image;

// Represents an animated texture.
// <remarks>
// Currently, this class assumes that each frame of animation is
// as wide as each animation is tall. The number of frames in the
// animation are inferred from this.
// </remarks>
class Animation {
  // All frames in the animation arranged horizontally.
  public Image Texture() {
    return texture;
  }
  Image texture;

  // Duration of time to show each frame.
  public float FrameTime() {
    return frameTime;
  }
  float frameTime;

  // When the end of the animation is reached, should it
  // continue playing from the beginning?
  public boolean IsLooping() {
    return isLooping;
  }
  boolean isLooping;

  // Gets the number of frames in the animation.
  public int FrameCount() {
    return Texture().width() / FrameWidth();
  }

  // Gets the width of a frame in the animation.
  public int FrameWidth() {
    // Assume square frames.
    return Texture().width();
  }

  // Gets the height of a frame in the animation.
  public int FrameHeight() {
    return Texture().height();
  }

  // Constructors a new animation.
  public Animation(Image texture, float frameTime, boolean isLooping) {
    this.texture = texture;
    this.frameTime = frameTime;
    this.isLooping = isLooping;
  }
}
