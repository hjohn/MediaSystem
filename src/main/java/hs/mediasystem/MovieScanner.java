package hs.mediasystem;

import hs.mediasystem.db.CachedItemProvider;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.db.TmdbItemProvider;
import hs.mediasystem.screens.movie.Element;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

// TODO this can probably be used genericly for all types of files
public class MovieScanner implements Scanner {
  private static final MovieElementDecoder DECODER = new MovieElementDecoder();
  private static final ItemProvider ITEM_PROVIDER = new CachedItemProvider(new TmdbItemProvider());
  
  @Override
  public Serie scan(Path scanPath) {
    Serie serie = new Serie();
    
    try {
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);
  
      for(Path path : dirStream) {
        Element element = DECODER.decode(path);
        
        if(element != null) {
          serie.addEpisode(element, ITEM_PROVIDER);
        }
        else {
          System.err.println("MovieScanner: Could not decode as movie: " + path);
        }
      }
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
    
    return serie;
  }
}
