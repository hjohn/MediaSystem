package hs.mediasystem.screens;

/**
 * Provides methods that allows a Layout to be selectable by the user, by giving it an id (for
 * remembering the user selection) and a title (to show in a UI).
 */
public interface UserLayout<C, P> extends Layout<C, P> {
  String getId();
  String getTitle();
}
