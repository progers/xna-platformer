package j15r.xna.platformer.core;

import forplay.core.Surface;

// Controls playback of an Animation.
public class AnimationPlayer {
  // Gets the animation which is currently playing.
  public Animation Animation() {
    return animation;
  }

  Animation animation;

  // Gets the index of the current frame in the animation.
  public int FrameIndex() {
    return frameIndex;
  }

  int frameIndex;

  // The amount of time in seconds that the current frame has been shown for.
  private float time;

  // Gets a texture origin at the bottom center of each frame.
  public Vector2 Origin() {
    return new Vector2(Animation().FrameWidth() / 2.0f, Animation().FrameHeight());
  }

  // Begins or continues playback of an animation.
  public void PlayAnimation(Animation animation) {
    // If this animation is already running, do not restart it.
    if (Animation() == animation)
      return;

    // Start the new animation.
    this.animation = animation;
    this.frameIndex = 0;
    this.time = 0.0f;
  }

  // Advances the time position and draws the current frame of the animation.
  public void Draw(float gameTime, Surface surf, Vector2 position, boolean flipH) {
    if (Animation() == null)
      throw new RuntimeException("No animation is currently playing.");

    // Process passing time.
    time += gameTime;
    while (time > Animation().FrameTime()) {
      time -= Animation().FrameTime();

      // Advance the frame index; looping or clamping as appropriate.
      if (Animation().IsLooping()) {
        frameIndex = (frameIndex + 1) % Animation().FrameCount();
      } else {
        frameIndex = Math.min(frameIndex + 1, Animation().FrameCount() - 1);
      }
    }

    // Calculate the source rectangle of the current frame.
    Rectangle source =
        new Rectangle(FrameIndex() * Animation().Texture().height(), 0,
            Animation().Texture().height(), Animation().Texture().height());

    // Draw the current frame.
    float startX = flipH ? (position.X + Origin().X) : (position.X - Origin().X);
    float width = flipH ? -source.Width : source.Width;
    surf.drawImage(Animation().Texture(), startX, position.Y - Origin().Y,
        width, source.Height, source.Left, source.Top, source.Width, source.Height);
  }
}
