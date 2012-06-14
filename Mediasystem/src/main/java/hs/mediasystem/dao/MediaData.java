package hs.mediasystem.dao;

import java.util.Date;

import hs.mediasystem.framework.DefaultEnrichable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class MediaData extends DefaultEnrichable<MediaData> {
  private int id;
  private Date lastUpdated;

  private String uri;

  private MediaId mediaId;
  private Identifier identifier;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  private final BooleanProperty viewed = new SimpleBooleanProperty() {
    @Override
    public boolean get() {
      queueForEnrichment();
      return super.get();
    }

    @Override
    protected void invalidated() {
      queueAsDirty();
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
    @Override
    public int get() {
      queueForEnrichment();
      return super.get();
    }

    @Override
    protected void invalidated() {
      queueAsDirty();
    }
  };
  public IntegerProperty resumePositionProperty() { return resumePosition; }

  public int getResumePosition() {
    return resumePosition.get();
  }

  public void setResumePosition(int resumePosition) {
    this.resumePosition.set(resumePosition);
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Identifier getIdentifier() {
    return identifier;
  }

  public void setIdentifier(Identifier identifier) {
    this.identifier = identifier;
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
