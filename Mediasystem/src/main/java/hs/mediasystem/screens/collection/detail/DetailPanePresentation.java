package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.screens.AreaLayout;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Presentation class for DetailPane views.  The presentation provides the content to be
 * displayed, the basic layout and whether or not the view should allow interaction with
 * the user.  If interaction is allowed, the content property can be updated to navigate
 * to other content.<p>
 *
 * Views using this presentation should make sure the content is something they can
 * handle as it can be anything.  Directly binding to the content property therefore
 * should be done with great care.
 */
public class DetailPanePresentation {
  public final ObjectProperty<Object> content = new SimpleObjectProperty<>();

  private AreaLayout areaLayout;
  private boolean interactive;

  public boolean isInteractive() {
    return interactive;
  }

  public void setInteractive(boolean interactive) {
    this.interactive = interactive;
  }

  public AreaLayout getAreaLayout() {
    return areaLayout;
  }

  public void setAreaLayout(AreaLayout areaLayout) {
    this.areaLayout = areaLayout;
  }
}
