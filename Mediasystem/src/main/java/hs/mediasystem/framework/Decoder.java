package hs.mediasystem.framework;

import hs.mediasystem.db.LocalInfo;

import java.nio.file.Path;

public interface Decoder {
  LocalInfo decode(Path path);
}
