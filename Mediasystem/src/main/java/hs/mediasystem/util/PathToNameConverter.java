package hs.mediasystem.util;

import java.nio.file.Files;
import java.nio.file.Path;

public class PathToNameConverter implements StringConverter<Path> {

  @Override
  public String toString(Path object) {
    String name = object.getFileName() == null ? object.toString() : object.getFileName().toString();

    return Files.isDirectory(object) ? "[" + name + "]" : name;
  }
}
