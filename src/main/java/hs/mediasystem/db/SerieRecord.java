package hs.mediasystem.db;

public class SerieRecord {
  private int id;
  private String title;
  private String overview;
  private byte[] banner;
  private byte[] background;
  
  public int getId() {
    return id;
  }
  
  public void setId(int id) {
    this.id = id;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public byte[] getBanner() {
    return banner;
  }
  
  public void setBanner(byte[] banner) {
    this.banner = banner;
  }
  
  public byte[] getBackground() {
    return background;
  }
  
  public void setBackground(byte[] background) {
    this.background = background;
  }
  
  public String getOverview() {
    return overview;
  }
  
  public void setOverview(String overview) {
    this.overview = overview;
  }
}
