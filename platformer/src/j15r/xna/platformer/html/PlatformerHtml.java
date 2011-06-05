package j15r.xna.platformer.html;

import forplay.core.ForPlay;
import forplay.html.HtmlAssetManager;
import forplay.html.HtmlGame;
import forplay.html.HtmlPlatform;
import j15r.xna.platformer.core.Platformer;

public class PlatformerHtml extends HtmlGame {

  @Override
  public void start() {
    HtmlAssetManager assets = HtmlPlatform.register().assetManager();
    assets.setPathPrefix("platformer/");
    ForPlay.run(new Platformer());
  }
}
