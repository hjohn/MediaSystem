package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class EpisodeScanner implements Scanner<LocalInfo> {
  private static final Pattern EXTENSION_PATTERN = Pattern.compile("(?i).+\\.(avi|flv|mkv|mov|mp4|mpg|mpeg)");

  private final Decoder decoder;

  public EpisodeScanner(Decoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public List<LocalInfo> scan(Path scanPath) {
    try {
      List<LocalInfo> results = new ArrayList<>();

      try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath)) {
        for(Path path : dirStream) {
          if(path.getFileName().toString().matches(EXTENSION_PATTERN.pattern())) {
            LocalInfo localInfo = decoder.decode(path);

            if(localInfo != null) {
              results.add(localInfo);
            }
            else {
              System.err.println("EpisodeScanner: Could not decode as movie: " + path);
            }
          }
        }
      }

      return results;
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
