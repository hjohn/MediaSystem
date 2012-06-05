package hs.mediasystem.db;

public class Person {
  private int id;

  private String name;
  private String photoURL;
  private Source<byte[]> photo;

  public int getId() {
    return id;
  }

  public void setId(int id) {
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
