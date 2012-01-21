package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EpisodeScanner implements Scanner<Episode> {
  private final Decoder decoder;
  private final MediaType mediaType;

  public EpisodeScanner(Decoder decoder, MediaType mediaType) {
    this.decoder = decoder;
    this.mediaType = mediaType;
  }

  @Override
  public List<Episode> scan(Path scanPath) {
    try {
      List<Episode> episodes = new ArrayList<>();

      try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath)) {
        for(Path path : dirStream) {
          if(path.getFileName().toString().matches(MovieDecoder.EXTENSION_PATTERN.pattern())) {
            LocalInfo localInfo = decoder.decode(path, mediaType);

            if(localInfo != null) {
              episodes.add(new Episode(localInfo));
            }
            else {
              System.err.println("EpisodeScanner: Could not decode as movie: " + path);
            }
          }
        }
      }

      return episodes;
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
