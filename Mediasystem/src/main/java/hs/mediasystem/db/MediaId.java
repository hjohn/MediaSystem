package hs.mediasystem.db;

public class MediaId {
  private byte[] hash;
  private long fileLength;
  private long fileTime;
  private Long osHash;

  public MediaId(long fileLength, long fileTime, byte[] hash, Long osHash) {
    this.fileLength = fileLength;
    this.fileTime = fileTime;
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

  public Long getOsHash() {
    return osHash;
  }

  public void setOsHash(Long osHash) {
    this.osHash = osHash;
  }
}
