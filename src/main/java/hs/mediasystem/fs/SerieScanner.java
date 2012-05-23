package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerieScanner implements Scanner<LocalInfo> {

  @Override
  public List<LocalInfo> scan(Path scanPath) {
    List<LocalInfo> results = new ArrayList<>();

    try {
      try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath)) {
        for(Path path : dirStream) {
          if(Files.isDirectory(path) && !path.getFileName().toString().startsWith(".")) {
            results.add(new LocalInfo(path.toString(), null, path.getFileName().toString(), null, null, null, null, null, null));
          }
        }
      }
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }

    return results;
  }
}
