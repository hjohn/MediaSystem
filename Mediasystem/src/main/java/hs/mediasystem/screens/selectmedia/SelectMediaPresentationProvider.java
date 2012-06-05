package hs.mediasystem.screens.selectmedia;

import javax.inject.Inject;
import javax.inject.Provider;

public class SelectMediaPresentationProvider implements Provider<SelectMediaPresentation> {
  @Inject
  private Provider<SelectMediaPresentation> provider;

  @Override
  public SelectMediaPresentation get() {
    return provider.get();
  }
}
