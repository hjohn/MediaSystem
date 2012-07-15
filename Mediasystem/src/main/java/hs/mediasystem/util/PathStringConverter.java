package hs.mediasystem.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.util.StringConverter;

public class PathStringConverter extends StringConverter<Path> {

  @Override
  public Path fromString(String s) {
    return Paths.get(s);
  }

  @Override
  public String toString(Path path) {
    return path.toString();
  }
}
