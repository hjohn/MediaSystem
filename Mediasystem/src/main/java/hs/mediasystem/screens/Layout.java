package hs.mediasystem.screens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.Node;

/**
 * A layout for a presentation.  Provides a way to create a view that will be attached
 * to the given presentation.  This allows for presentations to remain independent of
 * views suitable for working with the given presentation.  Views can be added later and
 * multiple views can be attached to the same presentation.<p>
 *
 * Layouts also allows for the views to remain independent of the presentation, but this
 * depends on the design of the views.
 *
 * @param <C> base content type the provided views can display
 * @param <P> presentation type for which this layout can provide views
 */
public interface Layout<C, P> {

  /**
   * Returns the type of content this layout is useful for.  This may be a subclass of
   * C if this layout should only be used to provide more specific information when a
   * more generalized layout (which can deal with any C) is also available.
   *
   * @return content class this layout can handle
   */
  Class<?> getContentClass();

  /**
   * Creates a view attached to the given presentation.  The view returned will interact
   * with the presentation and react to changes in it.
   *
   * @param presentation a presentation to attach to
   * @return a view attached to the given presentation
   */
  Node create(P presentation);

  /**
   * Finds all suitable layouts given a collection of layouts and the class of the content
   * to be displayed.
   *
   * @param layouts a collection of layouts
   * @param contentClass a class of content
   * @return all suitable layouts
   */
  public static <L extends Layout<?, ?>> Set<L> findAllSuitableLayouts(Collection<L> layouts, Class<?> contentClass) {
    Set<L> suitableLayouts = new HashSet<>();

    for(L layout : layouts) {
      if(layout.getContentClass().isAssignableFrom(contentClass)) {
        suitableLayouts.add(layout);
      }
    }

    return suitableLayouts;
  }

  /**
   * Finds the most suitable layout given a collection of layouts and the class of the content
   * to be displayed.
   *
   * @param layouts a collection of layouts
   * @param contentClass a class of content
   * @return the most suitable layout
   */
  public static <C, P> Layout<? extends C, P> findMostSuitableLayout(Collection<Layout<? extends C, P>> layouts, Class<?> contentClass) {
    int bestInheritanceDepth = -1;
    Layout<? extends C, P> bestLayout = null;

    for(Layout<? extends C, P> layout : layouts) {
      int inheritanceDepth = Layout.getInheritanceDepth(layout.getContentClass());

      if(layout.getContentClass().isAssignableFrom(contentClass) && inheritanceDepth > bestInheritanceDepth) {
        bestLayout = layout;
        bestInheritanceDepth = inheritanceDepth;
      }
    }

    return bestLayout;
  }

  public static int getInheritanceDepth(Class<?> cls) {
    Class<?> parent = cls;
    int depth = 0;

    while((parent = parent.getSuperclass()) != null) {
      depth++;
    }

    return depth;
  }
}
