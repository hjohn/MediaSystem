package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.Filter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.Events;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

public class TreeListPane extends BorderPane implements ListPane {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination KEY_C = new KeyCodeCombination(KeyCode.C);
  private static final KeyCombination LEFT = new KeyCodeCombination(KeyCode.LEFT);
  private static final KeyCombination RIGHT = new KeyCodeCombination(KeyCode.RIGHT);

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onItemSelected = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onItemSelected() { return onItemSelected; }

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onItemAlternateSelect = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onItemAlternateSelect() { return onItemAlternateSelect; }

  private final TreeView<MediaNode> treeView = new TreeView<>();

  private final ObjectBinding<MediaItem> mediaItem = new ObjectBinding<MediaItem>() {
    {
      bind(treeView.getFocusModel().focusedItemProperty());
    }

    @Override
    protected MediaItem computeValue() {
      TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

      return focusedItem != null ? focusedItem.getValue().getMediaItem() : null;
    }
  };
  @Override public ObjectBinding<MediaItem> mediaItemBinding() { return mediaItem; }

  private final Filter filter = new Filter() {{
    getStyleClass().add("seasons");
  }};

  public TreeListPane() {
    getStylesheets().add("select-media/tree-list-pane.css");

    treeView.setEditable(false);
    treeView.setShowRoot(false);

    treeView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

        if(focusedItem != null) {
          if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            itemSelected(event, focusedItem);
          }
          else if(event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 1) {
            Events.dispatchEvent(onItemAlternateSelect, new MediaNodeEvent(focusedItem.getValue()), event);
          }
        }
      }
    });

    treeView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

        if(focusedItem != null) {
          if(ENTER.match(event)) {
            itemSelected(event, focusedItem);
          }
          else if(KEY_C.match(event)) {
            Events.dispatchEvent(onItemAlternateSelect, new MediaNodeEvent(focusedItem.getValue()), event);
          }
        }

        if(LEFT.match(event)) {
          filter.activatePrevious();
          event.consume();
        }
        else if(RIGHT.match(event)) {
          filter.activateNext();
          event.consume();
        }
      }
    });

    filter.activeProperty().addListener(new ChangeListener<Node>() {
      @Override
      public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node value) {
        Label oldLabel = (Label)oldValue;
        Label label = (Label)value;

        if(oldLabel != null) {
          oldLabel.setText(((MediaNode)oldValue.getUserData()).getMediaItem().getShortTitle());
        }
        label.setText(((MediaNode)value.getUserData()).getMediaItem().getTitle());

        refilter();
      }
    });

    setTop(filter);
    setCenter(treeView);
  }

  @Override
  public void setRoot(final MediaNode root) {
    TreeItem<MediaNode> treeRoot = new TreeItem<>(root);

    treeView.setCellFactory(new Callback<TreeView<MediaNode>, TreeCell<MediaNode>>() {
      @Override
      public TreeCell<MediaNode> call(TreeView<MediaNode> param) {
        return new MediaItemTreeCell(root.getCellProvider());
      }
    });

    treeView.setRoot(treeRoot);

    filter.getChildren().clear();

    boolean expandTopLevel = root.expandTopLevel();

    if(expandTopLevel) {
      for(MediaNode node : root.getChildren()) {
        Label label = new Label(node.getMediaItem().getShortTitle());

        filter.getChildren().add(label);
        label.setUserData(node);
      }

      filter.activeProperty().set(filter.getChildren().get(0));
    }
    else {
      treeRoot.getChildren().clear();

      for(MediaNode node : root.getChildren()) {
        treeRoot.getChildren().add(new MediaNodeTreeItem(node));
      }
    }
  }

  @Override
  public void requestFocus() {
    treeView.requestFocus();
    treeView.getFocusModel().focus(0);
  }

  private void itemSelected(Event event, TreeItem<MediaNode> focusedItem) {
    if(focusedItem.isLeaf()) {
      Events.dispatchEvent(onItemSelected, new MediaNodeEvent(focusedItem.getValue()), event);
    }
    else {
      focusedItem.setExpanded(!focusedItem.isExpanded());
      event.consume();
    }
  }

  private void refilter() {
    TreeItem<MediaNode> treeRoot = treeView.getRoot();

    treeRoot.getChildren().clear();

    MediaNode group = (MediaNode)filter.activeProperty().get().getUserData();

    for(MediaNode item : group.getChildren()) {
      treeRoot.getChildren().add(new MediaNodeTreeItem(item));
    }
  }

  private final class MediaNodeTreeItem extends TreeItem<MediaNode> {
    private boolean childrenPopulated;

    private MediaNodeTreeItem(MediaNode value) {
      super(value);
    }

    @Override
    public boolean isLeaf() {
      return getValue().isLeaf();
    }

    @Override
    public ObservableList<TreeItem<MediaNode>> getChildren() {
      ObservableList<TreeItem<MediaNode>> treeChildren = super.getChildren();

      if(!childrenPopulated) {
        childrenPopulated = true;

        if(getValue().hasChildren()) {
          for(MediaNode child : getValue().getChildren()) {
            treeChildren.add(new MediaNodeTreeItem(child));
          }
        }
      }

      return treeChildren;
    }
  }

  private final class MediaItemTreeCell extends TreeCell<MediaNode> {
    private final CellProvider<MediaNode> provider;

    private MediaItemTreeCell(CellProvider<MediaNode> provider) {
      this.provider = provider;

      setDisclosureNode(new Group());
    }

    @Override
    protected void updateItem(final MediaNode item, boolean empty) {
      super.updateItem(item, empty);

      if(!empty) {
        setGraphic(provider.configureCell(item));
      }
    }
  }
}
