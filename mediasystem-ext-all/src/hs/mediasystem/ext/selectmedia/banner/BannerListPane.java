package hs.mediasystem.ext.selectmedia.banner;

import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeCellProvider;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.ServiceMediaNodeCell;
import hs.mediasystem.screens.selectmedia.ListPane;
import hs.mediasystem.util.Events;

import java.util.Set;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import javax.inject.Inject;
import javax.inject.Provider;

public class BannerListPane extends BorderPane implements ListPane {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);
  private static final KeyCombination KEY_I = new KeyCodeCombination(KeyCode.I);

  private final TableColumn<DuoMediaNode, MediaNode> leftColumn = new TableColumn<>("Left");
  private final TableColumn<DuoMediaNode, MediaNode> rightColumn = new TableColumn<>("Right");

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onItemSelected = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected() { return onItemSelected; }

  private final ObjectProperty<EventHandler<MediaNodeEvent>> onItemAlternateSelect = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaNodeEvent>> onNodeAlternateSelect() { return onItemAlternateSelect; }

  private final ReadOnlyObjectWrapper<MediaNode> focusedNode = new ReadOnlyObjectWrapper<>();
  @Override public ReadOnlyObjectProperty<MediaNode> focusedNodeProperty() { return focusedNode.getReadOnlyProperty(); }

  private final TableView<DuoMediaNode> tableView = new TableView<DuoMediaNode>() {{
    getColumns().add(leftColumn);
    getColumns().add(rightColumn);

    getSelectionModel().setCellSelectionEnabled(true);

    widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) {

        // Don't show header
        Pane header = (Pane)lookup("TableHeaderRow");
        if(header.isVisible()) {
          header.setMaxHeight(0);
          header.setMinHeight(0);
          header.setPrefHeight(0);
          header.setVisible(false);
          header.setManaged(false);
        }

        final int cellWidth = (int)((Double)newWidth - 16 - 2 - 4) / 2;
        final int bannerWidth = cellWidth - 2 * 8;

        leftColumn.setPrefWidth(cellWidth);
        rightColumn.setPrefWidth(cellWidth);

        leftColumn.setCellFactory(new Callback<TableColumn<DuoMediaNode, MediaNode>, TableCell<DuoMediaNode, MediaNode>>() {
          @Override
          public TableCell<DuoMediaNode, MediaNode> call(TableColumn<DuoMediaNode, MediaNode> column) {
            return new MediaNodeTableCell(bannerWidth);
          }
        });

        rightColumn.setCellFactory(new Callback<TableColumn<DuoMediaNode, MediaNode>, TableCell<DuoMediaNode, MediaNode>>() {
          @Override
          public TableCell<DuoMediaNode, MediaNode> call(TableColumn<DuoMediaNode, MediaNode> column) {
            return new MediaNodeTableCell(bannerWidth);
          }
        });
      }
    });

    leftColumn.setCellValueFactory(new PropertyValueFactory<DuoMediaNode, MediaNode>("left"));
    rightColumn.setCellValueFactory(new PropertyValueFactory<DuoMediaNode, MediaNode>("right"));
  }};

  private final ObjectBinding<MediaNode> mediaNode = new ObjectBinding<MediaNode>() {
    {
      bind(tableView.getFocusModel().focusedCellProperty());
    }

    @Override
    protected MediaNode computeValue() {
      return getFocusedMediaNode();
    }
  };
  @Override public ObjectBinding<MediaNode> mediaNodeBinding() { return mediaNode; }

  private final Provider<Set<MediaNodeCellProvider>> mediaNodeCellProvidersProvider;

  @Inject
  public BannerListPane(Provider<Set<MediaNodeCellProvider>> mediaNodeCellProvidersProvider) {
    this.mediaNodeCellProvidersProvider = mediaNodeCellProvidersProvider;
    getStylesheets().add("select-media/banner-list-pane.css");

//    mediaNodeCellProviderTracker = MediaNodeCellProvider.Type.HORIZONTAL.createTracker(bundleContext);

    tableView.setEditable(false);

    tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        MediaNode focusedNode = getFocusedMediaNode();

        if(focusedNode != null) {
          if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            itemSelected(event, focusedNode);
          }
          else if(event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 1) {
            Events.dispatchEvent(onItemAlternateSelect, new MediaNodeEvent(focusedNode), event);
          }
        }
      }
    });

    tableView.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        MediaNode focusedNode = getFocusedMediaNode();

        if(focusedNode != null) {
          if(ENTER.match(event)) {
            itemSelected(event, focusedNode);
          }
          else if(KEY_I.match(event)) {
            Events.dispatchEvent(onItemAlternateSelect, new MediaNodeEvent(focusedNode), event);
          }
        }
      }
    });

    tableView.getFocusModel().focusedCellProperty().addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        focusedNode.set(getFocusedMediaNode());
      }
    });

    setCenter(tableView);
  }

  private MediaNode root;

  @Override
  public MediaNode getRoot() {
    return root;
  }

  @Override
  public void setRoot(final MediaNode root) {
    this.root = root;

    tableView.getItems().clear();
    DuoMediaNode duoMediaNode = null;

    for(MediaNode node : root.getChildren()) {
      if(duoMediaNode != null) {
        duoMediaNode.rightProperty().set(node);
        duoMediaNode = null;
      }
      else {
        duoMediaNode = new DuoMediaNode();
        duoMediaNode.leftProperty().set(node);
        tableView.getItems().add(duoMediaNode);
      }
    }
  }

  @Override
  public void requestFocus() {
    tableView.requestFocus();
  }

  private void itemSelected(Event event, MediaNode focusedNode) {
    Events.dispatchEvent(onItemSelected, new MediaNodeEvent(focusedNode), event);
  }

  private MediaNode getFocusedMediaNode() {
    int column = tableView.getFocusModel().getFocusedCell().getColumn();
    DuoMediaNode duoNode = tableView.getFocusModel().getFocusedItem();
    if(duoNode != null) {
      return column == 0 ? duoNode.leftProperty().get() : duoNode.rightProperty().get();
    }

    return null;
  }

  private final class MediaNodeTableCell extends TableCell<DuoMediaNode, MediaNode> {
    private final ServiceMediaNodeCell mediaNodeCell = new ServiceMediaNodeCell(mediaNodeCellProvidersProvider.get());
    private final int bannerWidth;

    public MediaNodeTableCell(int bannerWidth) {
      this.bannerWidth = bannerWidth;

      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(final MediaNode mediaNode, boolean empty) {
      super.updateItem(mediaNode, empty);

      mediaNodeCell.configureGraphic(mediaNode);

      Region node = mediaNodeCell.getGraphic();

      if(node != null) {
        node.setMinWidth(bannerWidth);
        node.setMaxWidth(bannerWidth);
      }

      setGraphic(node);
    }
  }

  public static final class DuoMediaNode {
    private final ObjectProperty<MediaNode> left = new SimpleObjectProperty<>();
    public ObjectProperty<MediaNode> leftProperty() { return left; }
    public MediaNode getLeft() { return left.get(); }

    private final ObjectProperty<MediaNode> right = new SimpleObjectProperty<>();
    public ObjectProperty<MediaNode> rightProperty() { return right; }
    public MediaNode getRight() { return right.get(); }
  }

  @Override
  public MediaNode getSelectedNode() {
    return getFocusedMediaNode();
  }

  @Override
  public void setSelectedNode(MediaNode mediaNode) {
    if(tableView.getItems().size() > 0) {
      int index = mediaNode == null ? 0 : mediaNode.getParent().indexOf(mediaNode);

      if(index == -1) {
        index = 0;
      }

      final int finalIndex = index;

      tableView.getFocusModel().focus(finalIndex / 2, finalIndex % 2 == 0 ? leftColumn : rightColumn);
      tableView.getSelectionModel().select(finalIndex / 2, finalIndex % 2 == 0 ? leftColumn : rightColumn);

      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          tableView.scrollTo(finalIndex / 2);
        }
      });
    }
  }
}
