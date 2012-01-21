package hs.mediasystem.db;

public class Identifier {
  private final MediaType mediaType;
  private final String provider;
  private final String providerId;

  public Identifier(MediaType mediaType, String provider, String providerId) {
    this.mediaType = mediaType;
    this.provider = provider;
    this.providerId = providerId;
  }

  public MediaType getType() {
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
    return "(" + mediaType.name() + ";" + provider + ";" + providerId + ")";
  }
}
