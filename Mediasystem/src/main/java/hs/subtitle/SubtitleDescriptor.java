package hs.subtitle;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface SubtitleDescriptor {
  String getName();
  String getLanguageName();
  String getType();
  ByteBuffer fetch() throws IOException;
}
