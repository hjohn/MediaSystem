package hs.mediasystem.ext.serie;

import hs.mediasystem.framework.DefaultEnrichable;
import hs.mediasystem.framework.Media;

public class SerieBase extends Serie {

  public SerieBase(String title) {
    super(title);
  }

  @Override
  protected Class<? extends DefaultEnrichable<Media>> getEnrichClass() {
    return Serie.class;
  }
}
