package hs.mediasystem.dao;

import hs.mediasystem.db.Column;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;
import hs.mediasystem.enrich.DefaultPersistable;

import java.util.Date;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;

@Table(name = "mediadata")
public class MediaData extends DefaultPersistable<MediaData> {

  @Id
  private Integer id;

  @Column
  private Date lastUpdated;

  @Column
  private String uri;

  @Column
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

  @Column
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

  @Column
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
