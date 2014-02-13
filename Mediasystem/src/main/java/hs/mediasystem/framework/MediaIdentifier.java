package hs.mediasystem.framework;

public abstract class MediaIdentifier<T> {
  private final String source;
  private final String mediaType;

  public MediaIdentifier(String source, String mediaType) {
    this.source = source;
    this.mediaType = mediaType;
  }

  public final String getSource() {
    return source;
  }

  public final String getMediaType() {
    return mediaType;
  }

  public abstract Identifier identify(T media);
}
