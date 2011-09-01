package hs.mediasystem;

import hs.mediasystem.db.CachedItemProvider;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.db.TmdbItemProvider;
import hs.mediasystem.screens.movie.Element;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// TODO this can probably be used genericly for all types of files
public class MovieScanner implements Scanner<Episode> {
  private static final MovieElementDecoder DECODER = new MovieElementDecoder();
  private static final ItemProvider ITEM_PROVIDER = new CachedItemProvider(new TmdbItemProvider());
  
  @Override
  public List<Episode> scan(Path scanPath) {
    try {
      List<Episode> episodes = new ArrayList<Episode>();
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);
  
      for(Path path : dirStream) {
        Element element = DECODER.decode(path);
        
        if(element != null) {
          episodes.add(new Episode(element, ITEM_PROVIDER));
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
