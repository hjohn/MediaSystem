package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.screens.AreaLayout;
import hs.mediasystem.util.AreaPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;

/**
 * The base AreaPane class for showing details for the selected content.  Subclasses should provide
 * an AreaLayout to set up the pane before adding any controls.
 */
public class DetailPane<C> extends AreaPane {

  /**
   * The content to show in the detail pane.
   */
  public final ObjectProperty<C> content = new SimpleObjectProperty<>();

  /**
   * The EventHandler for when new content should be displayed.
   */
  public final ObjectProperty<EventHandler<DetailNavigationEvent>> onNavigate = new SimpleObjectProperty<>();

  public DetailPane(AreaLayout areaLayout) {
    getStylesheets().add("collection/detail-pane.css");
    getStylesheets().add("controls.css");
    getStyleClass().add("detail-pane");

    areaLayout.layout(this);
  }
}
