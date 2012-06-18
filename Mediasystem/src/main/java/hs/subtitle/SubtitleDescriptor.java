package hs.subtitle;

import java.io.IOException;

public interface SubtitleDescriptor {
  String getName();
  String getLanguageName();
  String getType();
  byte[] fetch() throws IOException;
}
