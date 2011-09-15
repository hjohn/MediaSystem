package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.framework.Decoder;

import java.nio.file.Path;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpisodeDecoder implements Decoder {
  private static String ALL_BUT_SEPARATOR = "(?:[^ ]+| (?!(?:- |\\[)))+";
  private static String SEPARATOR_PLUS_SEQUENCE_NUMBER = "(?: - ([0-9x]+))?";
  private static String SEPARATOR_PLUS_SUBTITLE = "(?: - )?((?:[^ ]*| (?!\\[))*)";
  private static String RELEASE_YEAR = "[0-9]{4}";
  private static String EXTENSION = "\\.[^ ]+";
  private static String IMDB = "\\(([0-9]+)\\)";
  
  private static final Pattern PATTERN = Pattern.compile(
      "(" + ALL_BUT_SEPARATOR + ")" + SEPARATOR_PLUS_SEQUENCE_NUMBER + SEPARATOR_PLUS_SUBTITLE +
      "(?: \\[(" + RELEASE_YEAR + ")?(?: ?(?:" + IMDB + ")?)?.*\\])?" + EXTENSION
  );
  
  @Override
  public Item decode(Path path) {
    Matcher matcher = PATTERN.matcher(path.getFileName().toString());
    
    if(!matcher.matches()) {
      return null;
    }
    
    String title = matcher.group(1);
    String sequence = matcher.group(2);
    String subtitle = matcher.group(3);
    String year = matcher.group(4);
    
    String imdb = matcher.group(5);
    String imdbNumber;
    
    if(imdb != null && !imdb.isEmpty()) {
      imdbNumber = String.format("tt%07d", Integer.parseInt(imdb));
    }
    else {
      imdbNumber = null;
    }
    
    Item item = new Item(path);
    
    item.setTitle(title);
    item.setSubtitle(subtitle);
    item.setImdbId(imdbNumber);
    item.setSeason(Integer.parseInt(sequence.split("x")[0]));
    item.setEpisode(Integer.parseInt(sequence.split("x")[1]));
    
    if(year != null) {
      GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(year), 0, 1);
      item.setReleaseDate(gc.getTime());
    }
    
    return item; 
  }
}
