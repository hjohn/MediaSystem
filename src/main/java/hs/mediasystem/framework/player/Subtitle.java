package hs.mediasystem.framework.player;

public class Subtitle {
  public static final Subtitle DISABLED = new Subtitle(-1, "Disabled");
  
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
