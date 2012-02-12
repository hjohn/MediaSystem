package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.Decoder;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MovieDecoder implements Decoder {
  public static final Pattern EXTENSION_PATTERN = Pattern.compile("(?i).+\\.(avi|flv|mkv|mov|mp4|mpg|mpeg)");

  private static final String SPLIT_CHARS = " ";
  private static final String ALL_BUT_SEPARATOR = "(?:[^" + SPLIT_CHARS + "]++| (?!(?:- |\\[)))++";      // matches the title
  private static final String SEPARATOR_PLUS_SEQUENCE_NUMBER = "(?: - ([0-9]++))?";   // matches the sequence number
  private static final String SEPARATOR_PLUS_SUBTITLE = "(?: - )?((?:[^" + SPLIT_CHARS + "]++| (?!\\[))*+)";  // matches the subtitle
  private static final String RELEASE_YEAR = "[0-9]{4}";
  private static final String IMDB = "\\(([0-9]++)\\)";

  private static final Pattern PATTERN = Pattern.compile(
      "(?i)(" + ALL_BUT_SEPARATOR + ")" + SEPARATOR_PLUS_SEQUENCE_NUMBER + SEPARATOR_PLUS_SUBTITLE +
      "(?: \\[(" + RELEASE_YEAR + ")?(?: ?(?:" + IMDB + ")?)?.*\\])?"
  );

  private static String getValidExtension(String fileName) {
    Matcher matcher = EXTENSION_PATTERN.matcher(fileName);

    if(matcher.matches()) {
      return matcher.group(1);
    }

    return null;
  }

  @Override
  public LocalInfo decode(Path path, MediaType mediaType) {
    String fileName = path.getFileName().toString();
    String extension = getValidExtension(fileName);

    if(extension == null) {
      return null;
    }

    fileName = fileName.substring(0, fileName.length() - extension.length() - 1);

    Matcher matcher = PATTERN.matcher(fileName);

    if(!matcher.matches()) {
      return null;
    }

    String title = matcher.group(1);
    String sequence = matcher.group(2);
    String subtitle = matcher.group(3);
    String yearString = matcher.group(4);

    String imdb = matcher.group(5);
    String imdbNumber;

    if(imdb != null && !imdb.isEmpty()) {
      imdbNumber = String.format("tt%07d", Integer.parseInt(imdb));
    }
    else {
      imdbNumber = null;
    }

    Integer year = yearString == null || yearString.isEmpty() ? null : Integer.parseInt(yearString);
    Integer episode = sequence == null ? 1 : Integer.parseInt(sequence);

    return new LocalInfo(path, mediaType, null, title, subtitle == null ? "" : subtitle, imdbNumber, year, null, episode);
  }
}
