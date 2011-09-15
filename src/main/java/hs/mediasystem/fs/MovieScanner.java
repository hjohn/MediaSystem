package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.framework.Scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MovieScanner implements Scanner<Episode> {
  private final Decoder decoder;
  private final ItemProvider itemProvider;
  
  public MovieScanner(Decoder decoder, ItemProvider itemProvider) {
    this.decoder = decoder;
    this.itemProvider = itemProvider;
  }
  
  @Override
  public List<Episode> scan(Path scanPath) {
    try {
      List<Episode> episodes = new ArrayList<Episode>();
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);
  
      for(Path path : dirStream) {
        Item item = decoder.decode(path);
        
        if(item != null) {
          episodes.add(new Episode(item, itemProvider));
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
