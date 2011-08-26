package hs.mediasystem;

import hs.mediasystem.screens.movie.Element;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class MovieScanner implements Scanner {
  private static final MovieElementDecoder decoder = new MovieElementDecoder();
  
  @Override
  public Serie scan(Path scanPath) {
    Serie serie = new Serie();
    
    try {
      DirectoryStream<Path> dirStream = Files.newDirectoryStream(scanPath);
  
      for(Path path : dirStream) {
        Element element = decoder.decode(path);
        
        if(element != null) {
          serie.addEpisode(element);
        }
        else {
          System.err.println("Could not decode as movie: " + path);
        }
      }
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
    
    return serie;
  }
}
