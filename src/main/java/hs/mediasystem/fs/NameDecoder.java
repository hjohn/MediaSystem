package hs.mediasystem.fs;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameDecoder {
  private static final String RELEASE_YEAR = "[0-9]{4}";
  private static final String IMDB = "\\(([0-9]++)\\)";

  private static final Pattern MAIN = Pattern.compile("(?i)([^\\[]+)(?: \\[(.+)\\])?");
  private static final Pattern INFO = Pattern.compile("(" + RELEASE_YEAR + ")?(?: ?(?:" + IMDB + ")?)?.*");
  private static final Pattern TITLES = Pattern.compile("([^\\(]+)(?: \\((.*)\\))?");

  private static final Set<String> KNOWN_DOUBLE_EXTENSIONS = new HashSet<String>() {{
    add("tar");
  }};

  private static final Set<String> SPACE_REPLACERS = new HashSet<String>() {{
    add("_");
    add(".");
  }};

  private static final String SEASON = "([0-9]{1,2})";
  private static final String EPISODE = "([0-9]{1,2}(?:-[0-9]{1,2})?)";

  private static final Set<Pattern> SEQUENCE_PATTERNS = new HashSet<Pattern>() {{
    add(Pattern.compile("" + SEASON + "x" + EPISODE));
    add(Pattern.compile("- " + SEASON + "()"));  // Only allows stand-alone sequence number if prefixed by a dash and space
    add(Pattern.compile("(?i)-S" + SEASON + " ?E" + EPISODE + "-"));
    add(Pattern.compile("-" + SEASON + "x" + EPISODE + "-"));
    add(Pattern.compile("(?i)\\(S" + SEASON + " ?E" + EPISODE + "\\)"));
    add(Pattern.compile("\\(" + SEASON + "x" + EPISODE + "\\)"));
    add(Pattern.compile("(?i)\\[S" + SEASON + " ?E" + EPISODE + "\\]"));
    add(Pattern.compile("\\[" + SEASON + "x" + EPISODE + "\\]"));
    add(Pattern.compile("(?i)S" + SEASON + " ?E" + EPISODE));
  }};

  public static DecodeResult decode(String input) {
    String title = null;
    String alternativeTitle = null;
    String subtitle = null;
    String sequence = null;
    Integer releaseYear = null;
    String code = null;
    String extension = null;

    String[] nameAndExtension = splitExtension(input);

    String cleanedInput = cleanInput(nameAndExtension[0]);
    extension = nameAndExtension[1];

    String[] main = match(MAIN, cleanedInput);
    //System.out.println("MAIN result: " + Arrays.toString(main));

    if(main != null) {
      String[] nameParts = splitNameParts(main[0]);
      // System.out.println("Split result: " + Arrays.toString(nameParts));

      String[] titles = match(TITLES, nameParts[0]);

      if(titles != null) {
        title = titles[0];
        alternativeTitle = titles[1];
      }

      if(nameParts[1] != null) {
        if(nameParts[2] != null && !nameParts[2].isEmpty()) {
          sequence = nameParts[1] + "," + nameParts[2];
        }
        else {
          sequence = nameParts[1];
        }
      }
      if(!nameParts[3].isEmpty()) {
        subtitle = nameParts[3];
      }

      if(main[1] != null) {
        String[] info = match(INFO, main[1]);

        if(info != null) {
          if(info[0] != null) {
            releaseYear = Integer.parseInt(info[0]);
          }
          if(info[1] != null) {
            code = info[1];
          }
        }
      }
    }

    // System.out.println("title=" + title + "; alternativeTitle=" + alternativeTitle + "; subtitle=" + subtitle + "; sequence=" + sequence + "; code=" + code + "; releaseYear=" + releaseYear + "; extension=" + extension);
    return new DecodeResult(title, alternativeTitle, subtitle, sequence, code, releaseYear, extension);
  }

  private static String[] splitNameParts(String input) {
    String[] parts = input.split(" ");
    String title = "";
    String season = null;
    String episode = null;
    String subtitle = "";
    int titleEnd = parts.length;
    int subtitleStart = parts.length;

    for(int i = 0; i < parts.length; i++) {
      String part = parts[i];
      String[] sequenceParts = decodeAsSequence(part);

      if(sequenceParts != null) {
        titleEnd = i;
        season = sequenceParts[0];
        episode = sequenceParts[1];
        subtitleStart = i + 1;
        break;
      }
      if(i < parts.length - 1) {
        sequenceParts = decodeAsSequence(part + " " + parts[i + 1]);

        if(sequenceParts != null) {
          titleEnd = i;
          season = sequenceParts[0];
          episode = sequenceParts[1];
          subtitleStart = i + 2;
          break;
        }
      }
    }

    if(season != null) {
      if(parts[titleEnd - 1].matches("[-]")) {
        titleEnd--;
      }
      if(subtitleStart < parts.length && parts[subtitleStart].matches("[-]")) {
        subtitleStart++;
      }
    }

    for(int i = 0; i < titleEnd; i++) {
      if(parts[i].matches("[-]")) {
        subtitleStart = i + 1;
        break;
      }
      if(!title.isEmpty()) {
        title += ' ';
      }
      title += parts[i];
    }

    for(int i = subtitleStart; i < parts.length; i++) {
      if(!subtitle.isEmpty()) {
        subtitle += ' ';
      }
      subtitle += parts[i];
    }

    return new String[] {title, season, episode, subtitle};
  }

  private static String[] splitExtension(String input) {
    String[] parts = input.split("\\.");
    int extensionStart = parts.length - 1;

    while(KNOWN_DOUBLE_EXTENSIONS.contains(parts[extensionStart - 1].toLowerCase())) {
      extensionStart--;
    }

    String[] result = new String[] {"", ""};

    for(int i = 0; i < extensionStart; i++) {
      if(!result[0].isEmpty()) {
        result[0] += '.';
      }
      result[0] += parts[i];
    }

    for(int i = extensionStart; i < parts.length; i++) {
      if(!result[1].isEmpty()) {
        result[1] += '.';
      }
      result[1] += parts[i];
    }

    return result;
  }

  private static String[] decodeAsSequence(String text) {
    for(Pattern pattern : SEQUENCE_PATTERNS) {
      String[] groups = match(pattern, text);

      if(groups != null) {
        return groups;
      }
    }

    return null;
  }

  private static String cleanInput(String input) {
    if(!input.contains(" ")) {
      String bestReplacer = null;
      int bestCount = 0;

      for(String spaceReplacer : SPACE_REPLACERS) {
        String[] split = input.split(Pattern.quote(spaceReplacer));

        if(split.length > bestCount) {
          bestReplacer = spaceReplacer;
          bestCount = split.length;
        }
      }

      if(bestReplacer != null) {
        return input.replaceAll(Pattern.quote(bestReplacer), " ");
      }
    }

    return input;
  }

  public static String[] match(Pattern pattern, String input) {
    Matcher matcher = pattern.matcher(input);

    if(matcher.matches()) {
      String[] groups = new String[matcher.groupCount()];

      for(int i = 0; i < groups.length; i++) {
        groups[i] = matcher.group(i + 1);
      }

      return groups;
    }

    return null;
  }

  public static class DecodeResult {
    private final String title;
    private final String alternativeTitle;
    private final String subtitle;
    private final String sequence;
    private final String code;
    private final Integer releaseYear;
    private final String extension;

    public DecodeResult(String title, String alternativeTitle, String subtitle, String sequence, String code, Integer releaseYear, String extension) {
      this.title = title;
      this.alternativeTitle = alternativeTitle;
      this.subtitle = subtitle;
      this.sequence = sequence;
      this.code = code;
      this.releaseYear = releaseYear;
      this.extension = extension;
    }

    public String getTitle() {
      return title;
    }

    public String getAlternativeTitle() {
      return alternativeTitle;
    }

    public String getSubtitle() {
      return subtitle;
    }

    public String getSequence() {
      return sequence;
    }

    public String getCode() {
      return code;
    }

    public Integer getReleaseYear() {
      return releaseYear;
    }

    public String getExtension() {
      return extension;
    }
  }
}
