package hs.mediasystem.dao;

import hs.mediasystem.db.AnnotatedRecordMapper;
import hs.mediasystem.db.Column;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;
import hs.mediasystem.entity.DefaultPersistable;

import java.util.Date;
import java.util.List;

@Table(name = "mediadata")
public class MediaData extends DefaultPersistable<MediaData> {

  @Id
  private Integer id;

  @Column
  private Date lastUpdated;

  @Column
  private String uri;

  @Column(name = {"filelength", "filetime", "filecreatetime", "hash", "oshash"})
  private MediaId mediaId;

  @Column
  private int resumePosition;

  @Column
  private boolean viewed;

  private List<Identifier> identifiers;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public boolean isViewed() {
    return viewed;
  }

  public void setViewed(boolean viewed) {
    this.viewed = viewed;
  }

  public int getResumePosition() {
    return resumePosition;
  }

  public void setResumePosition(int resumePosition) {
    this.resumePosition = resumePosition;
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

  public boolean areIdentifiersLoaded() {
    return identifiers != null;
  }

  public List<Identifier> getIdentifiers() {
    if(identifiers == null) {
      identifiers = AnnotatedRecordMapper.fetch(Identifier.class, this);
    }

    return identifiers;
  }
}
