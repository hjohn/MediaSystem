package hs.mediasystem.framework;

public class Subtitle {
  private final int id;
  private final String description;
  
  public Subtitle(int id, String description) {
    this.id = id;
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public int getId() {
    return id;
  }
}
