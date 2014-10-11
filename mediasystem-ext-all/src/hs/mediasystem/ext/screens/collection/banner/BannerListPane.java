package hs.mediasystem.ext.screens.collection.banner;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.WeakBinder;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import javax.inject.Inject;

public class BannerListPane extends BorderPane {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);

  public final ObservableList<MediaNode> mediaNodes = FXCollections.observableArrayList();
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();
  public final ObjectProperty<EventHandler<MediaNodeEvent>> onNodeSelected = new SimpleObjectProperty<>();

  private final TableColumn<DuoMediaNode, MediaNode> leftColumn = new TableColumn<>("Left");
  private final TableColumn<DuoMediaNode, MediaNode> rightColumn = new TableColumn<>("Right");

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

  @Inject
  public BannerListPane() {
    getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

    focusedMediaNode.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        setSelectedNode(current);
      }
    });

    tableView.getFocusModel().focusedCellProperty().addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        focusedMediaNode.set(getFocusedMediaNode());
      }
    });

    tableView.setEditable(false);

    tableView.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent event) {
        MediaNode focusedNode = getFocusedMediaNode();

        if(focusedNode != null) {
          if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            itemSelected(event, focusedNode);
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
        }
      }
    });

    mediaNodes.addListener(new ListChangeListener<MediaNode>() {
      @Override
      public void onChanged(ListChangeListener.Change<? extends MediaNode> change) {
        tableView.getItems().clear();
        DuoMediaNode duoMediaNode = null;

        for(MediaNode node : mediaNodes) {
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
    });

    setCenter(tableView);
  }

  @Override
  public void requestFocus() {
    tableView.requestFocus();
  }

  private void itemSelected(Event event, MediaNode focusedNode) {
    Events.dispatchEvent(onNodeSelected, new MediaNodeEvent(event.getTarget(), focusedNode), event);
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
    private final WeakBinder binder = new WeakBinder();
    private final int bannerWidth;
    private final VBox banner;

    private final Label title = new Label() {{
      getStyleClass().add("title");
    }};

    public MediaNodeTableCell(int bannerWidth) {
      this.bannerWidth = bannerWidth;

      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

      banner = new VBox() {{
        getChildren().add(title);
        HBox.setHgrow(this, Priority.ALWAYS);
      }};
    }

    @Override
    protected void updateItem(final MediaNode mediaNode, boolean empty) {
      super.updateItem(mediaNode, empty);

      binder.unbindAll();

      if(empty || mediaNode == null) {
        setGraphic(null);
        return;
      }

      final AsyncImageProperty asyncImageProperty = new AsyncImageProperty();
      final StringProperty titleProperty = new SimpleStringProperty();

      ObjectBinding<ImageHandle> bannerImageHandle = MapBindings.select(mediaNode.media, "banner");

      binder.bind(titleProperty, MapBindings.selectString(mediaNode.media, "title"));
      binder.bind(asyncImageProperty.imageHandleProperty(), bannerImageHandle);

      title.minHeightProperty().bind(minWidthProperty().multiply(140).divide(758));

      binder.bind(title.textProperty(), Bindings.when(asyncImageProperty.isNull()).then(titleProperty).otherwise(""));
      binder.bind(title.graphicProperty(), Bindings.when(asyncImageProperty.isNull()).then((ImageView)null).otherwise(new ImageView() {{
        imageProperty().bind(asyncImageProperty);
        setPreserveRatio(true);
        fitWidthProperty().bind(banner.minWidthProperty());
      }}));

      banner.setMinWidth(bannerWidth);
      banner.setMaxWidth(bannerWidth);

      setGraphic(banner);
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

  private void setSelectedNode(MediaNode mediaNode) {
    MediaNode focusedMediaNode = getFocusedMediaNode();

    if(mediaNode != null && mediaNode.equals(focusedMediaNode)) {
      return;
    }

    if(tableView.getItems().size() > 0) {
      ObservableList<DuoMediaNode> items = tableView.getItems();
      TableColumn<DuoMediaNode, MediaNode> column = null;

      for(int row = 0; row < items.size(); row++) {
        DuoMediaNode duoMediaNode = items.get(row);

        if(duoMediaNode.getLeft().equals(mediaNode)) {
          column = leftColumn;
        }
        else if(duoMediaNode.getRight() != null && duoMediaNode.getRight().equals(mediaNode)) {
          column = rightColumn;
        }

        if(column != null) {
          tableView.getFocusModel().focus(row, column);
          tableView.getSelectionModel().select(row, column);

          final int finalRow = row;

          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              tableView.scrollTo(finalRow);
            }
          });

          break;
        }
      }
    }
  }
}
