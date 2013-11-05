package hs.mediasystem.screens.collection;

import hs.mediasystem.screens.AreaLayout;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.collection.detail.DetailPanePresentation;
import hs.mediasystem.screens.collection.detail.DetailView;
import hs.mediasystem.util.AreaPane;
import hs.mediasystem.util.DialogPane;
import hs.mediasystem.util.GridPaneUtil;

import java.util.Set;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.inject.Inject;

public class CollectionSelectorPresentation {
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();
  public final ObservableList<MediaNode> mediaNodes = FXCollections.observableArrayList();
  public final ObjectProperty<Node> defaultInputFocus = new SimpleObjectProperty<>();
  public final BooleanProperty expandTopLevel = new SimpleBooleanProperty();

  public final ObjectProperty<EventHandler<MediaNodeEvent>> onSelect = new SimpleObjectProperty<>();
  public final EventHandler<MediaNodeEvent> onInfoSelect = new InfoEventHandler();  // TODO ugly

  private final ProgramController controller;
  private final Set<Layout<? extends Object, ? extends DetailPanePresentation>> layouts;

  @Inject
  public CollectionSelectorPresentation(ProgramController controller, Set<Layout<? extends Object, ? extends DetailPanePresentation>> layouts) {
    this.controller = controller;
    this.layouts = layouts;
  }

  private DialogPane<?> createInformationDialog(final MediaNode mediaNode, Set<Layout<? extends Object, ? extends DetailPanePresentation>> layouts) {
    DetailView detailPane = new DetailView(layouts, true, new AreaLayout() {
      @Override
      public void layout(AreaPane areaPane) {
        BorderPane borderPane = new BorderPane();
        GridPane gridPane = GridPaneUtil.create(new double[] {33, 34, 33}, new double[] {100});
        gridPane.setHgap(20);

        HBox hbox = new HBox();
        hbox.setId("link-area");

        areaPane.getChildren().add(borderPane);

        borderPane.setCenter(gridPane);
        borderPane.setBottom(hbox);

        gridPane.add(new VBox() {{
          setId("title-image-area");
        }}, 0, 0);

        gridPane.add(new VBox() {{
          getChildren().add(new VBox() {{
            setId("title-area");
          }});

          getChildren().add(new VBox() {{
            setId("description-area");
          }});
        }}, 1, 0);

        gridPane.add(new VBox() {{
          setId("action-area");
          setSpacing(2);
        }}, 2, 0);
      }
    });

    detailPane.content.set(mediaNode);

    DialogPane<?> dialogPane = new DialogPane<Void>() {
      @Override
      public void close() {
        super.close();

        detailPane.content.set(null);
      }
    };

    dialogPane.getChildren().add(detailPane);

    return dialogPane;
  }

  class InfoEventHandler implements EventHandler<MediaNodeEvent> {
    @Override
    public void handle(MediaNodeEvent event) {
      controller.showDialog(createInformationDialog(event.getMediaNode(), layouts));

      event.consume();
    }
  }
}
