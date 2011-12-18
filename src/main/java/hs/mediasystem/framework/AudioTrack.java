package hs.mediasystem.framework;

public class AudioTrack {
  private final int id;
  private final String description;
  
  public AudioTrack(int id, String description) {
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
