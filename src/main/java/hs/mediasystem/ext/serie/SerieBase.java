package hs.mediasystem.ext.serie;

import hs.mediasystem.media.EnrichableDataObject;

public class SerieBase extends Serie {

  public SerieBase(String title) {
    super(title);
  }

  @Override
  protected Class<? extends EnrichableDataObject> getEnrichClass() {
    return Serie.class;
  }
}
