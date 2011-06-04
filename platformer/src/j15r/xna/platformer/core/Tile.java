package j15r.xna.platformer.core;

import forplay.core.Image;

// Controls the collision detection and response behavior of a tile.
enum TileCollision {
  // A passable tile is one which does not hinder player motion at all.
  Passable,

  // An impassable tile is one which does not allow the player to move through
  // it at all. It is completely solid.
  Impassable,

  // A platform tile is one which behaves like a passable tile except when the
  // player is above it. A player can jump up through a platform as well as
  // move past it to the left and right, but can not fall down through the top
  // of it.
  Platform
}

// Stores the appearance and collision behavior of a tile.
class Tile {
  public Image Texture;
  public TileCollision Collision;

  public static final int Width = 40;
  public static final int Height = 32;

  public static Vector2 Size = new Vector2(Width, Height);

  // Constructs a new tile.
  public Tile(Image texture, TileCollision collision) {
    Texture = texture;
    Collision = collision;
  }
}
