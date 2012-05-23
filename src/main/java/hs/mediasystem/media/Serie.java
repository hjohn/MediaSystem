package hs.mediasystem.media;

import hs.mediasystem.util.ImageHandle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Serie extends Media {

  private final ObjectProperty<ImageHandle> banner = new SimpleObjectProperty<ImageHandle>() {
    @Override
    public ImageHandle get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public ImageHandle getBanner() { return banner.get(); }
  public ObjectProperty<ImageHandle> bannerProperty() { return banner; }

  public Serie(String title) {
    super(createKey(title), title);
  }
}
