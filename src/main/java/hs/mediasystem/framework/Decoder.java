package hs.mediasystem.framework;

import hs.mediasystem.db.LocalItem;

import java.nio.file.Path;

public interface Decoder {
  LocalItem decode(Path path);
}
