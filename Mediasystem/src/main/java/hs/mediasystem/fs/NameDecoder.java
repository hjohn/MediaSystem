package hs.mediasystem.fs;

import hs.mediasystem.fs.NameDecoder.Parts.Group;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameDecoder {
  private static final String RELEASE_YEAR = "[0-9]{4}(?=(?:[ ,]|$))";  // match exactly 4 digits, but only if followed by space, comma or EOL
  private static final String IMDB = "\\(([0-9]++)\\)";

  private static final String DELIMITER = "((?<=%1$s)|(?=%1$s))";

  private static final Pattern INFO = Pattern.compile("(" + RELEASE_YEAR + ")?(?: ?(?:" + IMDB + ")?)?.*");

  private static final Set<String> KNOWN_DOUBLE_EXTENSIONS = new LinkedHashSet<String>() {{
    add("tar");
  }};

  private static final Set<String> SPACE_REPLACERS = new LinkedHashSet<String>() {{
    add("_");
    add(".");
  }};

  private static final String SEASON = "([0-9]{1,2})";
  private static final String EPISODE = "([0-9]{1,2})(?:-[Ee]?([0-9]{1,2}))?";
  private static final String NOT_PRECEDED_BY_DIGIT = "(?<!\\d)";
  private static final String NOT_SUCCEEDED_BY_DIGIT_OR_LETTER = "(?!(?:\\d|\\p{L}))";

  private static final Set<Pattern> EPISODE_PATTERNS = new LinkedHashSet<Pattern>() {{
    add(Pattern.compile("(.*?)" + "-" + SEASON + "x" + EPISODE + "-" + "(.*?)"));
    add(Pattern.compile("(.*?)" + "\\(" + SEASON + "x" + EPISODE + "\\)" + "(.*?)"));
    add(Pattern.compile("(.*?)" + "\\[" + SEASON + "x" + EPISODE + "\\]" + "(.*?)"));
    add(Pattern.compile("(.*?)" + NOT_PRECEDED_BY_DIGIT + "[Ss]" + SEASON + " ?[Ee]" + EPISODE + NOT_SUCCEEDED_BY_DIGIT_OR_LETTER + "(.*?)"));
    add(Pattern.compile("(.*?)" + "-[Ss]" + SEASON + " ?[Ee]" + EPISODE + "-" + "(.*?)"));
    add(Pattern.compile("(.*?)" + "\\([Ss]" + SEASON + " ?[Ee]" + EPISODE + "\\)" + "(.*?)"));
    add(Pattern.compile("(.*?)" + "\\[[Ss]" + SEASON + " ?[Ee]" + EPISODE + "\\]" + "(.*?)"));
    add(Pattern.compile("(.*?)" + NOT_PRECEDED_BY_DIGIT + SEASON + "x" + EPISODE + NOT_SUCCEEDED_BY_DIGIT_OR_LETTER + "(.*?)"));

    add(Pattern.compile("(.*?)" + NOT_PRECEDED_BY_DIGIT + "([1-3][0-9]|0?[1-9])([1-3][0-9]|0[1-9])()" + "\\b" + "(.*?)"));    // matches seasons upto 39, episodes upto 39 when written without space

    add(Pattern.compile("(.*?)" + "()#" + EPISODE + "(.*?)"));  // No Season
    add(Pattern.compile("(.*?)" + NOT_PRECEDED_BY_DIGIT + "[Ss]" + SEASON + "()()" + NOT_SUCCEEDED_BY_DIGIT_OR_LETTER + "(.*?)")); // Season Only
  }};

  private static final Set<Pattern> MOVIE_PATTERNS = new LinkedHashSet<Pattern>() {{
    add(Pattern.compile("(.*?)" + "- " + SEASON + "()()(( [-\\[]|$).*?)"));
  }};

  private final Set<Pattern> sequencePatternsToCheck = new LinkedHashSet<>();

  public enum Hint {EPISODE, MOVIE}

  public NameDecoder(Hint... hints) {
    Set<Hint> hintSet = new HashSet<>(Arrays.asList(hints));

    if(hintSet.contains(Hint.EPISODE)) {
      sequencePatternsToCheck.addAll(EPISODE_PATTERNS);
    }
    if(hintSet.contains(Hint.MOVIE)) {
      sequencePatternsToCheck.addAll(MOVIE_PATTERNS);
    }
    if(sequencePatternsToCheck.isEmpty()) {
      sequencePatternsToCheck.addAll(EPISODE_PATTERNS);
      sequencePatternsToCheck.addAll(MOVIE_PATTERNS);
    }
  }

  public DecodeResult decode(String input) {
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

    String[] nameParts = splitNameParts(cleanedInput);
//    System.out.println("Split result: " + Arrays.toString(nameParts));

    title = nameParts[0];
    if(!nameParts[5].isEmpty()) {
      alternativeTitle = nameParts[5];
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

    if(nameParts[4] != null) {
      String[] info = match(INFO, nameParts[4]);

      if(info != null) {
        if(info[0] != null) {
          releaseYear = Integer.parseInt(info[0]);
        }
        if(info[1] != null) {
          code = info[1];
        }
      }
    }

    // System.out.println("title=" + title + "; alternativeTitle=" + alternativeTitle + "; subtitle=" + subtitle + "; sequence=" + sequence + "; code=" + code + "; releaseYear=" + releaseYear + "; extension=" + extension);
    return new DecodeResult(title, alternativeTitle, subtitle, sequence, code, releaseYear, extension);
  }

  private String[] splitNameParts(String input) {
    String[] sequenceParts = decodeAsSequence(input);
    String season = null;
    String episode = null;
    String episodeEnd = null;
    String leftoverInput = input;
    int subtitleIndex = input.length();

    if(sequenceParts != null) {
      season = sequenceParts[1];
      episode = sequenceParts[2];
      episodeEnd = sequenceParts[3];
      leftoverInput = sequenceParts[0] + " " + sequenceParts[4];
      subtitleIndex = sequenceParts[0].length() + 1;
    }

    Parts parts = new Parts(leftoverInput, "[- \\(\\)\\[\\]]");

    Group splitter = parts.groupAt(subtitleIndex);
    Group title = parts.before(splitter);
    Group subtitle = parts.after(splitter);

    Group special = parts.all().between("[", "]");
    String specialText = special.toString();

    if(!special.isEmpty()) {
      special.expand(1).delete();
    }

    Group alternativeTitle = title.copy().between("(", ")");
    String alternativeTitleText = alternativeTitle.toString();

    if(!alternativeTitle.isEmpty()) {
      alternativeTitle.expand(1).delete();
    }

    if(subtitle.isEmpty()) {
      int index = title.indexOf(" ", "-", " ");

      if(index >= 0) {
        title.setEndIndex(index);
        subtitle.setStartIndex(index + 1);
      }
    }

    title.trim(Pattern.compile("[- ]"));
    subtitle.trim(Pattern.compile("[- ]"));

//    System.out.println("FULL: [" + title.getStartIndex() + "-" + (title.getEndIndex() - 1) + "] [" + subtitle.getStartIndex() + "-" + subtitle.getEndIndex() + "]: " + parts);

    return new String[] {title.toString(), season, episode + (episodeEnd == null || episodeEnd.isEmpty() ? "" : "-" + episodeEnd), subtitle.toString(), specialText, alternativeTitleText};
  }

  private String[] splitExtension(String input) {
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

  private String[] decodeAsSequence(String text) {
    for(Pattern pattern : sequencePatternsToCheck) {
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

  public static class Parts {
    private final List<String> parts;
    private final List<Group> managedGroups = new ArrayList<>();

    public Parts(String input, String delimiterPattern) {
      parts = new ArrayList<>(Arrays.asList(input.split(String.format(DELIMITER, delimiterPattern))));
    }

    public Group groupAt(int charIndex) {
      int group = 0;
      int offset = 0;

      for(String part : parts) {
        offset += part.length();

        if(charIndex < offset) {
          return group(group, group + 1);
        }

        group++;
      }

      return end();
    }

    public Group before(int groupIndex) {
      return group(0, groupIndex);
    }

    public Group before(Group otherGroup) {
      return before(otherGroup.start);
    }

    public Group after(int groupIndex) {
      return group(groupIndex, parts.size());
    }

    public Group after(Group otherGroup) {
      return after(otherGroup.end);
    }

    public Group end() {
      return new Group(parts.size(), parts.size());
    }

    public Group all() {
      return group(0, parts.size());
    }

    public Group group(int start, int end) {
      Group group = new Group(start, end);

      managedGroups.add(group);

      return group;
    }

    public Iterable<Group> asGroupsOf(final int size) {
      if(size < 1) {
        throw new IllegalArgumentException("size must be positive");
      }

      return new Iterable<Group>() {
        @Override
        public Iterator<Group> iterator() {
          return new Iterator<Group>() {
            private Group lastGroup = null;
            private int position = 0;

            @Override
            public boolean hasNext() {
              return position + size <= parts.size();
            }

            @Override
            public Group next() {
              lastGroup = new Group(position, position + size);

              position++;

              return lastGroup;
            }

            @Override
            public void remove() {
              if(lastGroup == null) {
                throw new IllegalStateException("must call next() first");
              }

              lastGroup.delete();
              lastGroup = null;
            }
          };
        }
      };
    }

    public String get(int index) {
      return parts.get(index);
    }

    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();

      for(String s : parts) {
        if(builder.length() > 0) {
          builder.append(", ");
        }
        builder.append("'" + s + "'");
      }

      return builder.toString();
    }

    public class Group {
      private int start;
      private int end;

      public Group(int start, int end) {
        this.start = start;
        this.end = end;
      }

      public Group() {
        this(0, parts.size());
      }

      public Group between(String startText, String endText) {
        for(int i = start; i < end; i++) {
          if(parts.get(i).equals(startText)) {
            for(int j = i + 1; j < end; j++) {
              if(parts.get(j).equals(endText)) {
                start = i + 1;
                end = j;

                return this;
              }
            }
          }
        }

        start = end;

        return this;
      }

      public Group trim(String text) {
        while(start < end && get(start).equals(text)) {
          start++;
        }
        while(end > start && get(end - 1).equals(text)) {
          end--;
        }

        return this;
      }

      public Group trim(Pattern pattern) {
        while(start < end && pattern.matcher(get(start)).matches()) {
          start++;
        }
        while(end > start && pattern.matcher(get(end - 1)).matches()) {
          end--;
        }

        return this;
      }

      public int getStartIndex() {
        return start;
      }

      public void setStartIndex(int start) {
        if(start < 0 || start > end) {
          throw new IllegalArgumentException("parameter 'start' must be between 0 and end (" + end + "): " + start);
        }
        this.start = start;
      }

      public int getEndIndex() {
        return end;
      }

      public void setEndIndex(int end) {
        if(end < start || end > parts.size()) {
          throw new IllegalArgumentException("end must be between start and parts.size()");
        }
        this.end = end;
      }

      public int indexOf(String... text) {
        outer:
        for(int i = start; i < end - text.length + 1; i++) {
          for(int j = i; j < i + text.length; j++) {
            if(!text[j - i].equals(parts.get(j))) {
              continue outer;
            }
          }

          return i;
        }

        return -1;
      }

      public Group delete() {
        if(start != end) {
          parts.subList(start, end).clear();

          for(Group group : managedGroups) {
            group.remove(start, end);
          }

          end = start;
        }

        return this;
      }

      public Group expandStart(int size) {
        start -= size;

        if(start < 0) {
          start = 0;
        }
        if(start > end) {
          start = end;
        }

        return this;
      }

      public Group expandEnd(int size) {
        end += size;

        if(end > parts.size()) {
          end = parts.size();
        }
        if(end < start) {
          end = start;
        }

        return this;
      }

      public Group expand(int size) {
        expandStart(size);
        return expandEnd(size);
      }

      public boolean isEmpty() {
        return start == end;
      }

      public Group copy() {
        return Parts.this.group(start, end);
      }

      private void remove(int start, int end) {
        int length = this.end - this.start;
        int overlap = (end - start) - (start < this.start ? this.start - start : 0) - (end > this.end ? end - this.end : 0);

        if(overlap < 0) {
          overlap = 0;
        }

        if(start <= this.start) {
          this.start = overlap > 0 ? start : this.start - (end - start);
        }
        this.end = this.start + length - overlap;
      }

      @Override
      public String toString() {
        String result = "";

        for(int i = start; i < end; i++) {
          result += parts.get(i);
        }

        return result;
      }
    }
  }
}
