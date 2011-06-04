package j15r.xna.platformer.java;

import forplay.core.ForPlay;
import forplay.java.JavaAssetManager;
import forplay.java.JavaPlatform;
import j15r.xna.platformer.core.Platformer;

public class PlatformerJava {

  public static void main(String[] args) {
    JavaAssetManager assets = JavaPlatform.register().assetManager();
    assets.setPathPrefix("src/j15r/xna/platformer/resources");
    ForPlay.run(new Platformer());
  }
}
