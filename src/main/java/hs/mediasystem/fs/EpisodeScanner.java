package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
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
import java.util.regex.Pattern;

public class EpisodeScanner implements Scanner<MediaItem> {
  private static final Pattern EXTENSION_PATTERN = Pattern.compile("(?i).+\\.(avi|flv|mkv|mov|mp4|mpg|mpeg)");

  private final Decoder decoder;
  private final String mediaType;
  private final MediaTree mediaTree;

  public EpisodeScanner(MediaTree mediaTree, Decoder decoder, String mediaType) {
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
          if(path.getFileName().toString().matches(EXTENSION_PATTERN.pattern())) {
            @SuppressWarnings("unchecked")
            LocalInfo<Object> localInfo = (LocalInfo<Object>)decoder.decode(path, mediaType);

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
