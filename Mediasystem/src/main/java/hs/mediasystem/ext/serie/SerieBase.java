package hs.mediasystem.ext.serie;

import hs.mediasystem.framework.DefaultEnrichable;

public class SerieBase extends Serie {

  public SerieBase(String title) {
    super(title);
  }

  @Override
  protected Class<? extends DefaultEnrichable> getEnrichClass() {
    return Serie.class;
  }
}