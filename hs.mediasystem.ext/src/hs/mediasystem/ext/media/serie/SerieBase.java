package hs.mediasystem.ext.media.serie;

import hs.mediasystem.enrich.DefaultEnrichable;
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
