package hs.mediasystem.dao;

import hs.mediasystem.db.Column;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.db.Table;

@Table(name = "images")
public class Image extends DatabaseObject {

  @Column
  private String url;

  @Column
  private byte[] image;

  public Image(String url, byte[] data) {
    this.url = url;
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

  public byte[] getImage() {
    return image;
  }

  public void setImage(byte[] image) {
    this.image = image;
  }
}
