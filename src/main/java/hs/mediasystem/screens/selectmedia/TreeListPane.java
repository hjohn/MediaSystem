package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.Filter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.Events;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class TreeListPane extends BorderPane implements ListPane {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination KEY_C = new KeyCodeCombination(KeyCode.C);
  private static final KeyCombination LEFT = new KeyCodeCombination(KeyCode.LEFT);
  private static final KeyCombination RIGHT = new KeyCodeCombination(KeyCode.RIGHT);

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected() { return onNodeSelected; }

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect() { return onNodeAlternateSelect; }

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
            Events.dispatchEvent(onNodeAlternateSelect, new MediaNodeEvent(focusedItem.getValue()), event);
          }
        }
      }
    });

    treeView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(LEFT.match(event)) {
          filter.activatePrevious();
          event.consume();
        }
        else if(RIGHT.match(event)) {
          filter.activateNext();
          event.consume();
        }
        else {
          TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

          if(focusedItem != null) {
            if(ENTER.match(event)) {
              itemSelected(event, focusedItem);
            }
            else if(KEY_C.match(event)) {
              Events.dispatchEvent(onNodeAlternateSelect, new MediaNodeEvent(focusedItem.getValue()), event);
            }
          }
        }
      }
    });

    filter.activeProperty().addListener(new ChangeListener<Node>() {
      @Override
      public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node value) {
        Label oldLabel = (Label)oldValue;
        Label label = (Label)value;

        if(oldLabel != null) {
          oldLabel.setText(((MediaNodeTreeItem)oldValue.getUserData()).getValue().getMediaItem().getShortTitle());
        }
        label.setText(((MediaNodeTreeItem)value.getUserData()).getValue().getMediaItem().getTitle());

        refilter();
      }
    });

    setTop(filter);
    setCenter(treeView);
  }

  @Override
  public void setRoot(final MediaNode root) {
    treeView.setCellFactory(new Callback<TreeView<MediaNode>, TreeCell<MediaNode>>() {
      @Override
      public TreeCell<MediaNode> call(TreeView<MediaNode> param) {
        return new MediaItemTreeCell(root.getCellProvider());
      }
    });

    filter.getChildren().clear();

    if(root.expandTopLevel()) {
      for(MediaNode node : root.getChildren()) {
        Label label = new Label(node.getMediaItem().getShortTitle());

        filter.getChildren().add(label);
        label.setUserData(new MediaNodeTreeItem(node));
      }

      filter.activeProperty().set(filter.getChildren().get(0));
    }
    else {
      treeView.setRoot(new MediaNodeTreeItem(root, false));
    }
  }

  @Override
  public void requestFocus() {
    treeView.requestFocus();
  }

  private void itemSelected(Event event, TreeItem<MediaNode> focusedItem) {
    if(focusedItem.isLeaf()) {
      Events.dispatchEvent(onNodeSelected, new MediaNodeEvent(focusedItem.getValue()), event);
    }
    else {
      focusedItem.setExpanded(!focusedItem.isExpanded());
      event.consume();
    }
  }

  private void refilter() {
    MediaNodeTreeItem group = (MediaNodeTreeItem)filter.activeProperty().get().getUserData();
    treeView.getFocusModel().focus(-1);  // WORKAROUND: If focus index was same as before, even if root was changed, focused item is NOT updated
    treeView.setRoot(group);
    treeView.getFocusModel().focus(0);
  }

  private final class MediaNodeTreeItem extends TreeItem<MediaNode> {
    private final boolean isLeaf;

    private boolean childrenPopulated;

    MediaNodeTreeItem(MediaNode value, boolean isLeaf) {
      super(value);

      this.isLeaf = isLeaf;
    }

    MediaNodeTreeItem(MediaNode value) {
      this(value, value.isLeaf());
    }

    @Override
    public boolean isLeaf() {
      return isLeaf;
    }

    @Override
    public ObservableList<TreeItem<MediaNode>> getChildren() {
      ObservableList<TreeItem<MediaNode>> treeChildren = super.getChildren();

      if(!childrenPopulated) {
        childrenPopulated = true;

        for(MediaNode child : getValue().getChildren()) {
          treeChildren.add(new MediaNodeTreeItem(child));
        }
      }

      return treeChildren;
    }
  }

  private final class MediaItemTreeCell extends TreeCell<MediaNode> {
    private final CellProvider<MediaNode> provider;

    MediaItemTreeCell(CellProvider<MediaNode> provider) {
      this.provider = provider;
    }

    @Override
    protected void updateItem(final MediaNode item, boolean empty) {
      super.updateItem(item, empty);

      if(!empty) {
        Node node = provider.configureCell(item);

        double maxWidth = treeView.getWidth() - 35;
        ((Region)node).setMaxWidth(maxWidth);  // WORKAROUND for being unable to restrict cells to the width of the view
        ((Region)node).setPrefWidth(maxWidth);

        setGraphic(node);
      }
    }
  }

  @Override
  public MediaNode getSelectedNode() {
    TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

    return focusedItem == null ? null : focusedItem.getValue();
  }

  @Override
  public void setSelectedNode(MediaNode mediaNode) {
    treeView.getFocusModel().focus(-1);  // WORKAROUND: If focus index was same as before, even if root was changed, focused item is NOT updated

    if(mediaNode == null) {
      treeView.getFocusModel().focus(0);
    }
    else {
      List<MediaNode> stack = new ArrayList<>();

      stack.add(mediaNode);

      while(stack.get(0).getParent() != null) {
        stack.add(0, stack.get(0).getParent());
      }

      stack.remove(0);
      TreeItem<MediaNode> root = treeView.getRoot();

      stack:
      for(MediaNode node : stack) {
        for(Node n : filter.getChildren()) {
          if(((MediaNodeTreeItem)n.getUserData()).getValue().equals(node)) {
            filter.activeProperty().set(n);
            root = treeView.getRoot();
            continue stack;
          }
        }

        root.setExpanded(true);

        root = findTreeItem(root, node);

        if(root == null) {
          break;
        }
      }

      if(root != null) {
        final int index = treeView.getRow(root);

        treeView.getFocusModel().focus(index);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            treeView.scrollTo(index);
          }
        });
      }
    }
  }

  private TreeItem<MediaNode> findTreeItem(TreeItem<MediaNode> root, MediaNode mediaNode) {
    for(TreeItem<MediaNode> child : root.getChildren()) {
      if(child.getValue().equals(mediaNode)) {
        return child;
      }
    }

    return null;
  }
}
