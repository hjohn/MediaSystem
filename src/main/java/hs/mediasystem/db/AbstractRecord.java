package hs.mediasystem.db;

public class AbstractRecord {
  private int id;
  private String title;
  private String provider;
  private String providerId;
  private String type;
  private int version;

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
  
  public String getProvider() {
    return provider;
  }
  
  public void setProvider(String provider) {
    this.provider = provider;
  }
  
  public String getProviderId() {
    return providerId;
  }
  
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public int getVersion() {
    return version;
  }
  
  public void setVersion(int version) {
    this.version = version;
  }
}
