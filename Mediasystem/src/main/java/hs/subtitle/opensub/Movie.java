package hs.subtitle.opensub;

public class Movie {
  private String name;
  private int year;
  private int imdbId;

  public Movie(Movie obj) {
    this(obj.name, obj.year, obj.imdbId);
  }

  public Movie(String name, int year, int imdbId) {
    this.name = name;
    this.year = year;
    this.imdbId = imdbId;
  }

  public int getYear() {
    return year;
  }

  public int getImdbId() {
    return imdbId;
  }

  @Override
  public boolean equals(Object object) {
    if(object instanceof Movie) {
      Movie other = (Movie) object;
      if(imdbId > 0 && other.imdbId > 0) {
        return imdbId == other.imdbId;
      }

      return year == other.year && name.equals(other.name);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return imdbId;
  }

  @Override
  public String toString() {
    return String.format("%s (%d)", name, year);
  }

}