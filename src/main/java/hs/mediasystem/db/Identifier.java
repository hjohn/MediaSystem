package hs.mediasystem.db;

public class Identifier {
  private final String mediaType;
  private final String provider;
  private final String providerId;

  public Identifier(String mediaType, String provider, String providerId) {
    this.mediaType = mediaType;
    this.provider = provider;
    this.providerId = providerId;
  }

  public String getType() {
    return mediaType;
  }

  public String getProvider() {
    return provider;
  }

  public String getProviderId() {
    return providerId;
  }

  @Override
  public String toString() {
    return "(" + mediaType + ";" + provider + ";" + providerId + ")";
  }
}
