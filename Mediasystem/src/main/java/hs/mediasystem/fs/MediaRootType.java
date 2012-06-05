package hs.mediasystem.fs;

public enum MediaRootType {

  /**
   * Media with a parent of SERIES with a horizontally orientated poster (usually 4:3 or 16:9), no background, no banner.  The background
   * and banner can be used from the parent.
   */
  SERIE_EPISODES,

  /**
   * Media with a vertically orientated poster (about 2:3), a horizontally orientated background (usually 16:9) and banner (about 6:1).
   */
  SERIES,

  /**
   * Media with a optionally vertically orientated poster (about 2:3), a horizontally orientated background (usually 16:9), no banner.
   */
  MOVIES
}
