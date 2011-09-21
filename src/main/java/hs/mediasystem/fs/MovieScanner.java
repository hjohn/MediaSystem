package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MovieScanner implements Scanner<Episode> {
  private final MediaTree mediaTree;
  private final Decoder decoder;
  
  public MovieScanner(MediaTree mediaTree, Decoder decoder) {
    this.mediaTree = mediaTree;
    this.decoder = decoder;
  }
  
  @Override
  public List<Episode> scan(Path scanPath) {
    try {
      List<Episode> episodes = new ArrayList<Episode>();
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);
  
      for(Path path : dirStream) {
        Item item = decoder.decode(path);
        
        if(item != null) {
          episodes.add(new Episode(mediaTree, item));
        }
        else {
          System.err.println("MovieScanner: Could not decode as movie: " + path);
        }
      }
      
      return episodes;
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
