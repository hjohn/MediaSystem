package hs.mediasystem.framework;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.LocalInfo.Type;

import java.nio.file.Path;

public interface Decoder {
  LocalInfo decode(Path path, Type type);
}
