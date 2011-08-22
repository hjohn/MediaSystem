package hs.mediasystem.screens.movie;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemProvider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

public class MovieElement {
  private static String ALL_BUT_SEPARATOR = "(?:[^ ]+| (?!(?:- |\\[)))+";
  private static String SEPARATOR_PLUS_SEQUENCE_NUMBER = "(?: - ([0-9]+))?";
  private static String SEPARATOR_PLUS_SUBTITLE = "(?: - )?((?:[^ ]*| (?!\\[))*)";
  private static String RELEASE_YEAR = "[0-9]{4}";
  private static String EXTENSION = "\\.[^ ]+";
  private static String IMDB = "\\(([0-9]+)\\)";
  
  private static final Pattern PATTERN = Pattern.compile(
      "(" + ALL_BUT_SEPARATOR + ")" + SEPARATOR_PLUS_SEQUENCE_NUMBER + SEPARATOR_PLUS_SUBTITLE +
      "(?: \\[(" + RELEASE_YEAR + ")?(?: ?(?:" + IMDB + ")?)?.*\\])?" + EXTENSION
  );
  
  private final String fileName;
  
  private final String title;
  private final int sequence;
  private final String subtitle;
  private final String year;
  private final String imdbNumber;
  
  private SerieElement parentElement;
  private Item item;
  private final ItemProvider itemProvider;
  private final Path path;

  private MovieElement(Path path, String name, ItemProvider itemProvider) {
    this.path = path;
    this.fileName = name;
    this.itemProvider = itemProvider;
    
    Matcher matcher = PATTERN.matcher(fileName);
    
    if(matcher.matches()) {
      title = matcher.group(1);
      sequence = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
      subtitle = matcher.group(3) != null ? matcher.group(3) : "";
      year = matcher.group(4);
      
      String imdb = matcher.group(5);
      
      if(imdb != null && !imdb.isEmpty()) {
        imdbNumber = String.format("tt%07d", Integer.parseInt(imdb));
      }
      else {
        imdbNumber = null;
      }
      
  //    System.out.printf("%30s %30s %2s %4s %10s\n", "title", "subtitle", "seq", "year", "imdb");
//      System.out.printf("%-30s %-30s %4s %6s %-10s\n", ">"+matcher.group(1)+"<", 
//          ">"+matcher.group(3)+"<", 
//          ">"+matcher.group(2)+"<", 
//          ">"+matcher.group(4)+"<", 
//          ">"+matcher.group(5)+"<");
      
      
    }
    else {
      throw new RuntimeException(fileName);
    }
  }

  public MovieElement(Path path, ItemProvider itemProvider) {
    this(path, path.getFileName().toString(), itemProvider);
  }
  
  public MovieElement(String name, ItemProvider itemProvider) {
    this(null, name, itemProvider);
  }
  
  public String getFileName() {
    return fileName;
  }
  
  public String getTitle() {
    return title;
  }
  
  public String getSubtitle() {
    return subtitle;
  }
  
  public String getYear() {
    return year;
  }
  
  public int getSequence() {
    return sequence;
  }
  
  public BufferedImage getImage() {
    ensureMovieDataLoaded();
    
    try {
      return ImageIO.read(new ByteArrayInputStream(item.getCover()));
    }
    catch(IOException e) {
      return new BufferedImage(0, 0, BufferedImage.TYPE_4BYTE_ABGR);
    }
  }

  public String getPlot() {
    ensureMovieDataLoaded();
    
    return item.getPlot();
  }
  
  public int getRuntime() {
    ensureMovieDataLoaded();
    
    return item.getRuntime();
  }
 
  private void ensureMovieDataLoaded() {
    if(item == null) {
      try {
        item = itemProvider.getItem(fileName, title + (sequence != 0 ? " " + sequence : ""), year, imdbNumber);
      }
      catch(ItemNotFoundException e) {
        item = new Item();
      }
    }
  }

  public SerieElement getParent() {
    return parentElement;
  }
  
  public void setParent(SerieElement element) {
    this.parentElement = element;
  }
  
  public Path getPath() {
    return path;
  }
}

