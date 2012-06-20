package hs.mediasystem.dao;

import hs.mediasystem.db.Column;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;

@Table(name = "persons")
public class Person {
  @Id
  private Integer id;

  @Column
  private String name;

  @Column
  private String photoURL;

  @Column
  private Source<byte[]> photo;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhotoURL() {
    return photoURL;
  }

  public void setPhotoURL(String photoURL) {
    this.photoURL = photoURL;
  }

  public Source<byte[]> getPhoto() {
    return photo;
  }

  public void setPhoto(Source<byte[]> photo) {
    this.photo = photo;
  }
}
