package hs.mediasystem.dao;

import hs.mediasystem.db.Embeddable;
import hs.mediasystem.db.EmbeddableColumn;

@Embeddable
public class MediaId {

  @EmbeddableColumn(1)
  private long fileLength;

  @EmbeddableColumn(2)
  private long fileTime;

  @EmbeddableColumn(3)
  private long fileCreateTime;

  @EmbeddableColumn(4)
  private byte[] hash;

  @EmbeddableColumn(5)
  private Long osHash;

  public MediaId(Long fileLength, Long fileTime, Long fileCreateTime, byte[] hash, Long osHash) {
    this.fileLength = fileLength;
    this.fileTime = fileTime;
    this.fileCreateTime = fileCreateTime;
    this.hash = hash;
    this.osHash = osHash;
  }

  public byte[] getHash() {
    return hash;
  }

  public void setHash(byte[] hash) {
    this.hash = hash;
  }

  public long getFileLength() {
    return fileLength;
  }

  public void setFileLength(long fileLength) {
    this.fileLength = fileLength;
  }

  public long getFileTime() {
    return fileTime;
  }

  public void setFileTime(long fileTime) {
    this.fileTime = fileTime;
  }

  public long getFileCreateTime() {
    return fileCreateTime;
  }

  public void setFileCreateTime(long fileCreateTime) {
    this.fileCreateTime = fileCreateTime;
  }

  public Long getOsHash() {
    return osHash;
  }

  public void setOsHash(Long osHash) {
    this.osHash = osHash;
  }
}
