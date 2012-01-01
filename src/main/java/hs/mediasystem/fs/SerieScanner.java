package hs.mediasystem.fs;

import hs.mediasystem.db.LocalItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerieScanner implements Scanner<Serie> {
  private final MediaTree mediaTree;

  public SerieScanner(MediaTree mediaTree) {
    this.mediaTree = mediaTree;
  }

  @Override
  public List<Serie> scan(Path scanPath) {
    List<Serie> series = new ArrayList<Serie>();

    try {
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);

      for(Path path : dirStream) {
        if(Files.isDirectory(path) && !path.getFileName().toString().startsWith(".")) {
          LocalItem item = new LocalItem(path);

          item.setLocalName(path.getFileName().toString());
          item.setLocalTitle(item.getLocalName());
          item.setTitle(item.getLocalName());
          item.setType("serie");
          item.setProvider("LOCAL");
          item.setProviderId("");

          series.add(new Serie(mediaTree, item));
        }
      }
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }

    return series;
  }
}
