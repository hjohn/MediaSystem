package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.LocalInfo.Type;
import hs.mediasystem.framework.Decoder;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpisodeDecoder implements Decoder {
  private static String VALID_EXTENSIONS = "(avi|flv|mkv|mov|mp4|mpg|mpeg)";
  private static String SPLIT_CHARS = " \\.";
  private static String ALL_BUT_SEPARATOR = "(?:[^" + SPLIT_CHARS +"]++| (?!(?:- |\\[)))++";
  private static String SEPARATOR_PLUS_SEQUENCE_NUMBER = "(?: - ([0-9x]++))?";
  private static String SEPARATOR_PLUS_SUBTITLE = "(?: - )?((?:[^" + SPLIT_CHARS + "]++| (?!\\[))*+)";
  private static String RELEASE_YEAR = "[0-9]{4}";
  private static String EXTENSION = "\\." + VALID_EXTENSIONS;
  private static String IMDB = "\\(([0-9]++)\\)";

  private static final Pattern PATTERN = Pattern.compile(
      "(?i)(" + ALL_BUT_SEPARATOR + ")" + SEPARATOR_PLUS_SEQUENCE_NUMBER + SEPARATOR_PLUS_SUBTITLE +
      "(?: \\[(" + RELEASE_YEAR + ")?(?: ?(?:" + IMDB + ")?)?.*\\])?" + EXTENSION
  );

  @Override
  public LocalInfo decode(Path path, Type type) {
    Matcher matcher = PATTERN.matcher(path.getFileName().toString());

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

    Integer year = yearString.isEmpty() ? null : Integer.parseInt(yearString);
    Integer season = null;
    Integer episode = null;

    if(sequence != null && sequence.matches("[0-9]+x[0-9]+")) {
      season = Integer.parseInt(sequence.split("x")[0]);
      episode = Integer.parseInt(sequence.split("x")[1]);
    }

    return new LocalInfo(path, type, title, subtitle.isEmpty() ? null : subtitle, imdbNumber, year, season, episode);
  }
}
