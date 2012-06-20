package hs.mediasystem.dao;

import hs.mediasystem.db.Column;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;

import java.sql.SQLException;
import java.util.Date;

@Table(name = "persons")
public class Person {
  @Id
  private Integer id;

  @Column
  private String name;

  @Column
  private String biography;

  @Column
  private String birthPlace;

  @Column
  private Date birthDate;

  @Column
  private String photoURL;

  private Source<byte[]> photo;

  public void afterLoad(Transaction transaction) throws SQLException {
    if(getPhotoURL() != null) {
      Object[] result = transaction.selectUnique("url", "images", "url = ?", getPhotoURL());

      setPhoto(new DatabaseImageSource(transaction.getConnectionProvider(), getPhotoURL(), result != null ? null : new URLImageSource(getPhotoURL())));
    }
  }

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

  public String getBiography() {
    return biography;
  }

  public void setBiography(String biography) {
    this.biography = biography;
  }

  public String getBirthPlace() {
    return birthPlace;
  }

  public void setBirthPlace(String birthPlace) {
    this.birthPlace = birthPlace;
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    this.birthDate = birthDate;
  }
}
