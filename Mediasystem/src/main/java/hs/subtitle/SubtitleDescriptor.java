package hs.subtitle;

import java.io.IOException;

public interface SubtitleDescriptor {
  public enum MatchType {

    /**
     * Matched directly by an id.
     */
    ID,

    /**
     * Matched by a hash calculated over the content of a file.
     */
    HASH,

    /**
     * Matched on name.
     */
    NAME,

    /**
     * Unknown.
     */
    UNKNOWN
  }

  String getName();
  String getLanguageName();
  String getType();
  MatchType getMatchType();

  byte[] getSubtitleRawData() throws IOException;
}
