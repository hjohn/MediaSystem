package hs.mediasystem.screens.collection;

import javafx.scene.Node;

/**
 * A layout for a presentation.  Provides a way to create a view that will be attached
 * to the given presentation.  This allows for presentations to remain independent of
 * views suitable for working with the given presentation.  Views can be added later and
 * multiple views can be attached to the same presentation.<p>
 *
 * A Layout also allows for the views to remain independent of the presentation, but this
 * depends on the design of the views.
 *
 * @param <C> content type the provided views can display
 * @param <P> presentation type for which this layout can provide views
 */
public interface Layout<C, P> {
  String getId();
  String getTitle();
  boolean isSuitableFor(C content);

  /**
   * Creates a view attached to the given presentation.  The view returned will interact
   * with the presentation and react to changes in it.
   *
   * @param presentation a presentation to attach to
   * @return a view attached to the given presentation
   */
  Node create(P presentation);
}
