package hs.mediasystem.util;

import java.nio.file.Path;

public class PathToFullPathConverter implements StringConverter<Path> {

  @Override
  public String toString(Path object) {
    return object.toString();
  }
}
