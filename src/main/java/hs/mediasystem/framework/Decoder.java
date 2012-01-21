package hs.mediasystem.framework;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;

import java.nio.file.Path;

public interface Decoder {
  LocalInfo decode(Path path, MediaType mediaType);
}
