package hs.mediasystem.fs;

import hs.mediasystem.db.LocalItem;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class EpisodeScanner implements Scanner<Episode> {
  private final MediaTree mediaTree;
  private final Decoder decoder;
  private final String type;

  public EpisodeScanner(MediaTree mediaTree, Decoder decoder, String type) {
    this.mediaTree = mediaTree;
    this.decoder = decoder;
    this.type = type;
  }

  @Override
  public List<Episode> scan(Path scanPath) {
    try {
      List<Episode> episodes = new ArrayList<>();

      try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath)) {
        for(Path path : dirStream) {
          LocalItem item = decoder.decode(path);

          if(item != null) {
            item.setTitle(item.getLocalTitle());
            item.setSubtitle(item.getLocalSubtitle());

            if(item.getLocalReleaseYear() != null) {
              GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(item.getLocalReleaseYear()), 0, 1);
              item.setReleaseDate(gc.getTime());
            }

            episodes.add(new Episode(mediaTree, item, type));
          }
          else {
            System.err.println("MovieScanner: Could not decode as movie: " + path);
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
