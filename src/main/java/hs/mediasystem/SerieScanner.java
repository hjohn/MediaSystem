package hs.mediasystem;

import hs.mediasystem.db.TvdbSerieProvider;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SerieScanner implements Scanner<Serie> {
  private final MovieScanner episodeScanner = new MovieScanner();
  
  @Override
  public List<Serie> scan(Path scanPath) {
    List<Serie> series = new ArrayList<Serie>();
    
    try {
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);
  
      for(Path path : dirStream) {
        if(Files.isDirectory(path)) {
          Serie serie = new Serie(path, path.getFileName().toString(), new TvdbSerieProvider());
          
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
