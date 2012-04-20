package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.SelectMediaView;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.GridPaneUtil;

import java.util.List;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import javax.inject.Inject;

public class StandardView extends StackPane implements SelectMediaView {
  private static final KeyCombination BACK_SPACE = new KeyCodeCombination(KeyCode.BACK_SPACE);

  private final ObjectProperty<EventHandler<ActionEvent>> onBack = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<ActionEvent>> onBack() { return onBack; }

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected() { return onNodeSelected; }

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect() { return onNodeAlternateSelect; }

  private final Set<StandardLayoutExtension> selectMediaExtensions;
  private final BackgroundPane backgroundPane = new BackgroundPane();

  private StandardLayoutExtension layoutExtension;
  private StandardLayout layout;

  @Inject
  public StandardView(Set<StandardLayoutExtension> selectMediaExtensions) {
    this.selectMediaExtensions = selectMediaExtensions;

    getStylesheets().add("select-media/duo-pane-select-media-view.css");

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(BACK_SPACE.match(event)) {
          Events.dispatchEvent(onBack, new ActionEvent(StandardView.this, null), event);
        }
      }
    });

    GridPane stage = GridPaneUtil.create(new double[] {100}, new double[] {90, 10});

    stage.add(new StackPane() {{
      getStyleClass().add("stage");
    }}, 0, 1);

    getChildren().add(backgroundPane);
    getChildren().add(stage);
  }

  @Override
  public void setRoot(MediaNode root) {
    StandardLayoutExtension layoutExtension = findLayout(root.getMediaItem());

    if(!layoutExtension.equals(this.layoutExtension)) {
      this.layoutExtension = layoutExtension;

      if(layout != null) {
        layout.onNodeSelected().set(null);
        layout.onNodeAlternateSelect().set(null);

        getChildren().remove(layout);
      }

      layout = layoutExtension.createLayout();
      getChildren().add((Node)layout);

      backgroundPane.mediaItemProperty().bind(layout.mediaItemBinding());

      layout.onNodeSelected().set(onNodeSelected.get());
      layout.onNodeAlternateSelect().set(onNodeAlternateSelect.get());
    }

    layout.setRoot(root);
  }

  @Override
  public void requestFocus() {
    if(layout != null) {
      ((Node)layout).requestFocus();
    }
  }

  @Override
  public MediaNode getSelectedNode() {
    return layout == null ? null : layout.getSelectedNode();
  }

  @Override
  public void setSelectedNode(MediaNode mediaNode) {
    if(layout != null) {
      layout.setSelectedNode(mediaNode);
    }
  }

  private final ObservableList<StandardLayoutExtension> availableLayoutExtensions = FXCollections.observableArrayList();

  private StandardLayoutExtension findLayout(MediaItem root) {
    availableLayoutExtensions.clear();

    MediaRootType rootType;

    if(root.getMediaType().equals("SERIE")) {
      rootType = MediaRootType.SERIE_EPISODES;
    }
    else if(root.getMediaType().equals("SERIE_ROOT")) {
      rootType = MediaRootType.SERIES;
    }
    else {
      rootType = MediaRootType.MOVIES;
    }

    for(StandardLayoutExtension extension : selectMediaExtensions) {
      if(extension.getSupportedMediaRootTypes().contains(rootType)) {
        availableLayoutExtensions.add(extension);
      }
    }

    return availableLayoutExtensions.get(0);
  }

  @Override
  public List<String> getAvailableLayouts() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getLayout() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setLayout(String layout) {
    // TODO Auto-generated method stub

  }
}
