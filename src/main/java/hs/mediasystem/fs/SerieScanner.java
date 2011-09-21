package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
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
        if(Files.isDirectory(path)) {
          Item item = new Item(path);
          
          item.setTitle(path.getFileName().toString());
          
          Serie serie = new Serie(mediaTree, item);
          
          series.add(serie);
        }
      }
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
    
    return series;
  }
}
