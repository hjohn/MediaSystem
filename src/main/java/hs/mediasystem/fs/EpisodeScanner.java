package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class EpisodeScanner implements Scanner<MediaItem> {
  private final Decoder decoder;
  private final MediaType mediaType;
  private final MediaTree mediaTree;

  public EpisodeScanner(MediaTree mediaTree, Decoder decoder, MediaType mediaType) {
    this.mediaTree = mediaTree;
    this.decoder = decoder;
    this.mediaType = mediaType;
  }

  @Override
  public List<MediaItem> scan(Path scanPath) {
    try {
      List<MediaItem> episodes = new ArrayList<>();

      try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath)) {
        for(Path path : dirStream) {
          if(path.getFileName().toString().matches(MovieDecoder.EXTENSION_PATTERN.pattern())) {
            LocalInfo localInfo = decoder.decode(path, mediaType);

            if(localInfo != null) {
              episodes.add(new MediaItem(mediaTree, localInfo));
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
