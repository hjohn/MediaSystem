package hs.mediasystem.ext.screens.collection.tree;

import hs.mediasystem.screens.Filter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeCellProvider;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.ServiceMediaNodeCell;
import hs.mediasystem.util.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
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

import javax.inject.Inject;
import javax.inject.Provider;

public class TreeListPane extends BorderPane {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination KEY_I = new KeyCodeCombination(KeyCode.I);
  private static final KeyCombination LEFT = new KeyCodeCombination(KeyCode.LEFT);
  private static final KeyCombination RIGHT = new KeyCodeCombination(KeyCode.RIGHT);

  public final ObservableList<MediaNode> mediaNodes = FXCollections.observableArrayList();
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();
  public final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected = new SimpleObjectProperty<>();
  public final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect = new SimpleObjectProperty<>();
  public final BooleanProperty expandTopLevel = new SimpleBooleanProperty();

  private final TreeView<MediaNode> treeView = new TreeView<>();

  private final Filter filter = new Filter() {{
    getStyleClass().add("seasons");
  }};

  private final Provider<Set<MediaNodeCellProvider>> mediaNodeCellsProvider;

  private final InvalidationListener invalidateTreeListener = new InvalidationListener() {
    @Override
    public void invalidated(Observable observable) {
      if(treeValid) {
        treeValid = false;
        requestLayout();
      }
    }
  };

  private final ChangeListener<TreeItem<MediaNode>> updateFocusedMediaNode = new ChangeListener<TreeItem<MediaNode>>() {
    @Override
    public void changed(ObservableValue<? extends TreeItem<MediaNode>> observable, TreeItem<MediaNode> old, TreeItem<MediaNode> current) {
      focusedMediaNode.set(current == null ? null : current.getValue());
    }
  };

  private boolean treeValid;

  @Inject
  public TreeListPane(Provider<Set<MediaNodeCellProvider>> mediaNodeCellsProvider) {
    this.mediaNodeCellsProvider = mediaNodeCellsProvider;

    focusedMediaNode.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        setSelectedNode(current);
      }
    });

    getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

    treeView.getStyleClass().add("main-list");
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
            Events.dispatchEvent(onNodeAlternateSelect, new MediaNodeEvent(treeView, focusedItem.getValue()), event);
          }
        }
      }
    });

    treeView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(LEFT.match(event)) {
          filter.activatePrevious();
          treeView.getFocusModel().focus(0);
          event.consume();
        }
        else if(RIGHT.match(event)) {
          filter.activateNext();
          treeView.getFocusModel().focus(0);
          event.consume();
        }
        else {
          TreeItem<MediaNode> focusedItem = treeView.getFocusModel().getFocusedItem();

          if(focusedItem != null) {
            if(ENTER.match(event)) {
              itemSelected(event, focusedItem);
            }
            else if(KEY_I.match(event)) {
              Events.dispatchEvent(onNodeAlternateSelect, new MediaNodeEvent(treeView, focusedItem.getValue()), event);
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
          oldLabel.setText(((MediaNodeTreeItem)oldValue.getUserData()).getValue().getShortTitle());
        }
        label.setText(((MediaNodeTreeItem)value.getUserData()).getValue().getMedia().title.get());

        refilter();
      }
    });

    expandTopLevel.addListener(invalidateTreeListener);
    mediaNodes.addListener(invalidateTreeListener);

    setTop(filter);
    setCenter(treeView);
  }

  @Override
  protected double computePrefWidth(double height) {
    if(!treeValid) {
      buildTree();
      treeValid = true;
    }

    return super.computePrefWidth(height);
  }

  private void buildTree() {
    treeView.getFocusModel().focusedItemProperty().removeListener(updateFocusedMediaNode);  // prevent focus updates from changing tree root

    treeView.setCellFactory(new Callback<TreeView<MediaNode>, TreeCell<MediaNode>>() {
      @Override
      public TreeCell<MediaNode> call(TreeView<MediaNode> param) {
        return new MediaItemTreeCell();
      }
    });

    filter.getChildren().clear();

    if(expandTopLevel.get()) {
      for(MediaNode node : mediaNodes) {
        Label label = new Label(node.getShortTitle());

        filter.getChildren().add(label);
        label.setUserData(new MediaNodeTreeItem(node));
      }
    }
    else {
      treeView.setRoot(new MediaNodeTreeItem(new MediaNode("root", "root", "root", false, mediaNodes), false));
    }

    treeView.getFocusModel().focusedItemProperty().addListener(updateFocusedMediaNode);

    setSelectedNode(focusedMediaNode.get());
  }

  @Override
  public void requestFocus() {
    treeView.requestFocus();
  }

  private void itemSelected(Event event, TreeItem<MediaNode> focusedItem) {
    if(focusedItem.isLeaf()) {
      Events.dispatchEvent(onNodeSelected, new MediaNodeEvent(event.getTarget(), focusedItem.getValue()), event);
    }
    else {
      focusedItem.setExpanded(!focusedItem.isExpanded());
      event.consume();
    }
  }

  private void refilter() {
    MediaNodeTreeItem group = (MediaNodeTreeItem)filter.activeProperty().get().getUserData();
    treeView.setRoot(group);
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
    private final ServiceMediaNodeCell mediaNodeCell = new ServiceMediaNodeCell(mediaNodeCellsProvider.get());

    @Override
    protected void updateItem(final MediaNode mediaNode, boolean empty) {
      super.updateItem(mediaNode, empty);

      mediaNodeCell.configureGraphic(mediaNode);

      Region node = mediaNodeCell.getGraphic();

      if(node != null) {
        double maxWidth = treeView.getWidth() - 35;
        node.setMaxWidth(maxWidth);  // WORKAROUND for being unable to restrict cells to the width of the view
        node.setPrefWidth(maxWidth);
      }

      setGraphic(node);
    }
  }

  private void setSelectedNode(MediaNode selectedMediaNode) {
    TreeItem<MediaNode> focusedTreeItem = treeView.getFocusModel().getFocusedItem();

    if(selectedMediaNode == null || (focusedTreeItem != null && selectedMediaNode.equals(focusedTreeItem.getValue()))) {
      return;
    }

    List<MediaNode> stack = new ArrayList<>();

    stack.add(selectedMediaNode);

    while(stack.get(0).getParent() != null) {
      stack.add(0, stack.get(0).getParent());
    }

    /*
     * First level of the stack matches the filter (if used).  This needs to be set
     * correctly first to create a new root in the TreeView.
     */

    if(!filter.getChildren().isEmpty()) {
      for(Node n : filter.getChildren()) {
        if(((MediaNodeTreeItem)n.getUserData()).getValue().equals(stack.get(0))) {
          filter.activeProperty().set(n);
          stack.remove(0);
          break;
        }
      }
    }

    /*
     * Get the TreeView's root, which either existed already or was just setup by
     * adjusting the filter.
     */

    TreeItem<MediaNode> treeItemForSelection = treeView.getRoot();

    if(treeItemForSelection != null) {

      /*
       * Expand all TreeItem's towards the MediaNode to be selected.
       */

      for(MediaNode node : stack) {
        treeItemForSelection.setExpanded(true);

        treeItemForSelection = findTreeItem(treeItemForSelection, node);

        if(treeItemForSelection == null) {
          break;
        }
      }

      /*
       * If after the search for the MediaNode to be selected we found a matching TreeItem
       * that can be selected, it will be focused and scrolled to.
       */

      if(treeItemForSelection != null) {
        final int index = treeView.getRow(treeItemForSelection);
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

  private static TreeItem<MediaNode> findTreeItem(TreeItem<MediaNode> root, MediaNode mediaNode) {
    for(TreeItem<MediaNode> child : root.getChildren()) {
      if(child.getValue().equals(mediaNode)) {
        return child;
      }
    }

    return null;
  }
}
