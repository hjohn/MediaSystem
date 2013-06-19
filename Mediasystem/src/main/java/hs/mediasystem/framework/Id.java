package hs.mediasystem.framework;

/**
 * A generally useable Id which can be stored as a String in maps and databases.  Id's
 * are not allowed to contain colons or any type of slashes as these are reserved for
 * concatenating multiple Id's.<p>
 *
 * Id's can consist of other Id's that can be returned seperately or as a whole.  This
 * is useful when a value needs to be assocatiated with all Id's of a certain type.<p>
 *
 * Id's are immutable.
 */
public class Id {
  private final String id;

  public Id(String id) {
    if(id == null || id.length() == 0) {
      throw new IllegalArgumentException("parameter 'id' cannot be null or zero length");
    }
    if(id.contains("/") || id.contains("\\") || id.contains(":")) {
      throw new IllegalArgumentException("parameter 'id' cannot contain colons, slashes or backslashes");
    }

    this.id = id;
  }

  public String toString(String prefix, String subGroup) {
    return prefix + ":" + id + "[" + subGroup + "]";
  }

  public String toString(String prefix) {
    return prefix + ":" + id;
  }

  @Override
  public String toString() {
    return id;
  }
}
