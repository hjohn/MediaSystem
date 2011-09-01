package hs.mediasystem;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerieScanner {
  private final MovieScanner episodeScanner = new MovieScanner();
  
  public List<Serie> scan(Path scanPath) {
    List<Serie> series = new ArrayList<Serie>();
    
    try {
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);
  
      for(Path path : dirStream) {
        if(Files.isDirectory(path)) {
          Serie serie = new Serie(path, path.getFileName().toString());
          
          serie.addAll(episodeScanner.scan(path));
          
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
