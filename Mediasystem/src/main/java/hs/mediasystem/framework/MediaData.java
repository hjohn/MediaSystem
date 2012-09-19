package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class MediaData extends Entity<MediaData> {
  public final StringProperty uri = stringProperty();
  public final LongProperty fileLength = longProperty();
  public final ObjectProperty<Long> osHash = object("osHash");
  public final IntegerProperty resumePosition = integerProperty();
  public final BooleanProperty viewed = booleanProperty();

  public MediaData(String uri, long fileLength, Long osHash, int resumePosition, boolean viewed) {
    this.uri.set(uri);
    this.fileLength.set(fileLength);
    this.osHash.set(osHash);
    this.resumePosition.set(resumePosition);
    this.viewed.set(viewed);

    this.setPersister(PersisterProvider.getPersister(MediaData.class));
  }
}
