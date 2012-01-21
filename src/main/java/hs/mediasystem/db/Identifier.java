package hs.mediasystem.db;

public class Identifier {
  private final String type;
  private final String provider;
  private final String providerId;

  public Identifier(String type, String provider, String providerId) {
    this.type = type;
    this.provider = provider;
    this.providerId = providerId;
  }

  public String getType() {
    return type;
  }

  public String getProvider() {
    return provider;
  }

  public String getProviderId() {
    return providerId;
  }

  @Override
  public String toString() {
    return "(" + type + ";" + provider + ";" + providerId + ")";
  }
}
