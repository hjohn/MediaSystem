package hs.mediasystem.dao;

import hs.mediasystem.enrich.DefaultEnrichable;

import java.util.Date;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MediaData extends DefaultEnrichable<MediaData> {
  private Integer id;

  private Date lastUpdated;

  private String uri;

  private MediaId mediaId;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  private final BooleanProperty viewed = new SimpleBooleanProperty() {
//    @Override
//    public boolean get() {
//      queueForEnrichment();
//      return super.get();
//    }

    @Override
    protected void invalidated() {
      queueAsDirty();
      get();
    }
  };
  public BooleanProperty viewedProperty() { return viewed; }

  public boolean isViewed() {
    return viewed.get();
  }

  public void setViewed(boolean viewed) {
    this.viewed.set(viewed);
  }

  private final IntegerProperty resumePosition = new SimpleIntegerProperty() {
//    @Override
//    public int get() {
//      queueForEnrichment();
//      return super.get();
//    }

    @Override
    protected void invalidated() {
      queueAsDirty();
      get();
    }
  };
  public IntegerProperty resumePositionProperty() { return resumePosition; }

  public int getResumePosition() {
    return resumePosition.get();
  }

  public void setResumePosition(int resumePosition) {
    this.resumePosition.set(resumePosition);
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public MediaId getMediaId() {
    return mediaId;
  }

  public void setMediaId(MediaId mediaId) {
    this.mediaId = mediaId;
  }

  public Date getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(Date lastUpdated) {
    this.lastUpdated = lastUpdated;
  }
}
