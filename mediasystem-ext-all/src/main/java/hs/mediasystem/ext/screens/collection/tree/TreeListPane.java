package hs.mediasystem.ext.screens.collection.tree;

import hs.mediasystem.framework.descriptors.Descriptor;
import hs.mediasystem.framework.descriptors.DescriptorSet;
import hs.mediasystem.framework.descriptors.DescriptorSet.Attribute;
import hs.mediasystem.framework.descriptors.EntityDescriptors;
import hs.mediasystem.framework.descriptors.EntityDescriptors.TextType;
import hs.mediasystem.screens.DuoLineCell;
import hs.mediasystem.screens.Filter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
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
import javafx.util.Callback;

import javax.inject.Inject;

public class TreeListPane extends BorderPane {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination LEFT = new KeyCodeCombination(KeyCode.LEFT);
  private static final KeyCombination RIGHT = new KeyCodeCombination(KeyCode.RIGHT);

  public final ObservableList<MediaNode> mediaNodes = FXCollections.observableArrayList();
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();
  public final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected = new SimpleObjectProperty<>();
  public final BooleanProperty expandTopLevel = new SimpleBooleanProperty();

  private final TreeView<MediaNode> treeView = new TreeView<>();

  private final Filter filter = new Filter() {{
    getStyleClass().add("tabs");
  }};

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

  private boolean treeValid = true;

  @Inject
  public TreeListPane() {
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
        label.setText(((MediaNodeTreeItem)value.getUserData()).getValue().media.get().title.get());

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

    applyCss();
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
    private final DuoLineCell duoLineCell = new DuoLineCell();
    private final WeakBinder binder = new WeakBinder();
    private final StringProperty[] bindTargets = {duoLineCell.titleProperty(), duoLineCell.extraInfoProperty()};

    @Override
    protected void updateItem(final MediaNode mediaNode, boolean empty) {
      super.updateItem(mediaNode, empty);

      binder.unbindAll();

      if(empty) {
        setGraphic(null);
        return;
      }

      EntityDescriptors mediaProperties = mediaNode.getMedia().getEntityDescriptors();

      DescriptorSet set = mediaProperties == null ? null : mediaProperties.getDescriptorSets().stream().filter(p -> p.getAttributes().contains(Attribute.PREFERRED)).findFirst().orElse(null);

      if(mediaProperties == null || set == null) {
        binder.bind(duoLineCell.titleProperty(), mediaNode.media.get().title);
        duoLineCell.extraInfoProperty().set("");
        duoLineCell.prefixProperty().set("");
        duoLineCell.subtitleProperty().set("");
        duoLineCell.viewedProperty().set(false);
        duoLineCell.ratingProperty().set(0);
      }
      else {

        /*
         * Figure out which information is redundant:
         */

        Set<Descriptor> redundantProperties = new HashSet<>();
        TreeItem<MediaNode> treeItem = getTreeItem().getParent();

        while(treeItem != null) {
          EntityDescriptors paretMediaProperties = treeItem.getValue().getMedia().getEntityDescriptors();

          if(paretMediaProperties != null) {
            for(Descriptor descriptor : paretMediaProperties.getDescriptors()) {
              redundantProperties.add(descriptor);
              redundantProperties.addAll(descriptor.getElements());
            }
          }

          treeItem = treeItem.getParent();
        }

        /*
         * Set-up the bindings with relevant information:
         */

        int target = 0;

        for(Descriptor property : set.getDescriptors()) {
          if(property.getType() instanceof TextType) {
            binder.bind(bindTargets[target++], MapBindings.select(mediaNode.media, property.getName()).asString());
            redundantProperties.add(property);
          }
        }

        List<Descriptor> properties = mediaProperties.getDescriptors().stream()
          .filter(p -> !redundantProperties.contains(p) && Collections.disjoint(redundantProperties, p.getElements()))
          .filter(p -> p.getType() instanceof TextType)
          .sorted((a, b) -> Double.compare(b.getUniqueness(), a.getUniqueness()))
          .collect(Collectors.toList());

        for(Descriptor property : properties) {
          if(target >= bindTargets.length) {
            break;
          }

          binder.bind(bindTargets[target++], MapBindings.select(mediaNode.media, property.getName()));
        }

        binder.bind(duoLineCell.prefixProperty(), MapBindings.selectString(mediaNode.media, "prefix"));
        binder.bind(duoLineCell.subtitleProperty(), MapBindings.selectString(mediaNode.media, "subtitle"));
        binder.bind(duoLineCell.viewedProperty(), MapBindings.selectBoolean(mediaNode.mediaData, "viewed"));
        binder.bind(duoLineCell.ratingProperty(), MapBindings.selectDouble(mediaNode.media, "rating").divide(10));
      }

      duoLineCell.collectionSizeProperty().set(mediaNode.getChildren().size());

      double maxWidth = treeView.getWidth() - 35;

      duoLineCell.setMaxWidth(maxWidth);  // WORKAROUND for being unable to restrict cells to the width of the view
      duoLineCell.setPrefWidth(maxWidth);

      setGraphic(duoLineCell);
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
