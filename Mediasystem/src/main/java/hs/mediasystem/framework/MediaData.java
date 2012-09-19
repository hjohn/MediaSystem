package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class MediaData extends Entity<MediaData> {
  public final ObjectProperty<hs.mediasystem.dao.MediaData> dbMediaData = object("dbMediaData");

  public final StringProperty uri = stringProperty();
  public final LongProperty fileLength = longProperty();
  public final ObjectProperty<Long> osHash = object("osHash");
  public final IntegerProperty resumePosition = integerProperty();
  public final BooleanProperty viewed = booleanProperty();

  public MediaData(hs.mediasystem.dao.MediaData dbMediaData) {
    this.dbMediaData.addListener(new ChangeListener<hs.mediasystem.dao.MediaData>() {
      @Override
      public void changed(ObservableValue<? extends hs.mediasystem.dao.MediaData> observableValue, hs.mediasystem.dao.MediaData old, hs.mediasystem.dao.MediaData current) {
        uri.set(current.getUri());
        fileLength.set(current.getMediaId().getFileLength());
        osHash.set(current.getMediaId().getOsHash());
        resumePosition.set(current.getResumePosition());
        viewed.set(current.isViewed());
      }
    });

    this.dbMediaData.set(dbMediaData);
  }
}
