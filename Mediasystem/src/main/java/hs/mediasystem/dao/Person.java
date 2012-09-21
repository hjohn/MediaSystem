package hs.mediasystem.dao;

import hs.mediasystem.db.AnnotatedRecordMapper;
import hs.mediasystem.db.Column;
import hs.mediasystem.db.Database;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.db.Id;
import hs.mediasystem.db.Table;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Table(name = "persons")
public class Person extends DatabaseObject {

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

  private List<Casting> castings;

  public void afterLoadStore(Database database) throws SQLException {
    setPhoto(DatabaseUrlSource.create(database, getPhotoURL()));
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

  public String getBiography() {
    return biography;
  }

  public void setBiography(String biography) {
    this.biography = biography;
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

  public boolean isCastingsLoaded() {
    return castings != null;
  }

  public List<Casting> getCastings() {
    if(castings == null) {
      castings = AnnotatedRecordMapper.fetch(Casting.class, this);
    }
    return castings;
  }
}
