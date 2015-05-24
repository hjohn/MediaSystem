package hs.mediasystem.dao;

import hs.mediasystem.db.Column;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.db.Table;

import java.time.LocalDateTime;

@Table(name = "images")
public class Image extends DatabaseObject {

  @Column
  private String url;

  @Column
  private LocalDateTime creationTime;

  @Column
  private byte[] image;

  public Image(String url, byte[] data) {
    this.url = url;
    this.creationTime = LocalDateTime.now();
    this.image = data;
  }

  public Image() {
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public LocalDateTime getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(LocalDateTime creationTime) {
    this.creationTime = creationTime;
  }

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }
}
