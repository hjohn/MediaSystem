package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.ListMediaRoot;
import hs.mediasystem.framework.MediaRoot;

public class Season extends ListMediaRoot {
  private static final Id ID = new Id("season");

  public Season(MediaRoot parent, String rootName) {
    super(parent, ID, rootName);
  }
}
