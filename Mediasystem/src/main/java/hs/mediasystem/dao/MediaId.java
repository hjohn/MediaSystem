package hs.mediasystem.dao;

public class MediaId {
  private byte[] hash;
  private long fileLength;
  private long fileTime;
  private long fileCreateTime;
  private Long osHash;

  public MediaId(long fileLength, long fileTime, long fileCreateTime, byte[] hash, Long osHash) {
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
