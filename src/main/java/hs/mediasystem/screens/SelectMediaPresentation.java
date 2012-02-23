package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.SelectMediaView;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.StringConverter;

import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

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

import javax.inject.Inject;

public class SelectMediaPresentation {
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);
  private final SelectMediaView view;
  private final Navigator navigator;
  private final StandardLayout layout = new StandardLayout();

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaView view, final MediaItemEnrichmentEventHandler enrichmentHandler) {
    this.navigator = new Navigator(controller.getNavigator());
    this.view = view;

    view.onBack().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        navigator.back();
        event.consume();
      }
    });

    view.onItemSelected().set(new EventHandler<MediaNodeEvent>() {
      @Override
      public void handle(MediaNodeEvent event) {
        if(event.getMediaNode().getMediaItem().isLeaf()) {
          controller.play(event.getMediaNode().getMediaItem());
        }
        else {
          setTreeRoot(event.getMediaNode().getMediaItem()); // TODO Could trigger a new pane altogether
        }
        event.consume();
      }
    });

    view.onItemAlternateSelect().set(new EventHandler<MediaNodeEvent>() {
      @Override
      public void handle(MediaNodeEvent event) {
        final MediaItem mediaItem = event.getMediaNode().getMediaItem();

        List<? extends Option> options = FXCollections.observableArrayList(
          new ActionOption("Reload meta data", new Callable<Boolean>() {
            @Override
            public Boolean call() {
              enrichmentHandler.enrich(mediaItem, true);

              ImageCache.expunge(mediaItem.getBanner());
              ImageCache.expunge(mediaItem.getPoster());
              ImageCache.expunge(mediaItem.getBackground());
              return true;
            }
          })
        );

        controller.showOptionScreen("Options: " + mediaItem.getTitle(), options);
        event.consume();
      }
    });

    getView().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(KEY_O.match(event)) {
          @SuppressWarnings("unchecked")
          List<? extends Option> options = FXCollections.observableArrayList(
            new ListOption<>("Group by", groupSetProperty(), availableGroupSetsProperty(), new StringConverter<GroupSet>() {
              @Override
              public String toString(GroupSet set) {
                return set.getTitle();
              }
            }),
            new ListOption<>("Order by", sortOrderProperty(), availableSortOrdersProperty(), new StringConverter<SortOrder>() {
              @Override
              public String toString(SortOrder order) {
                return order.getTitle();
              }
            })
          );

          controller.showOptionScreen("Media - Options", options);
          event.consume();
        }
      }
    });
  }

  public Node getView() {
    return (Node)view;
  }

  private void setTreeRoot(final MediaItem root) {
    navigator.navigateTo(new Destination(root.getTitle()) {
      @Override
      public void execute() {
        view.setRoot(layout.wrap(root));
      }
    });
  }

  public static void main(String[] args) {
    Date date = new Date();


    GregorianCalendar gc = new GregorianCalendar();
    gc.setTime(date);
    System.out.println(gc.getTimeZone());
    gc.setTimeZone(TimeZone.getTimeZone("EST"));

    System.out.println(date.getTime());
    System.out.println(gc);
    DateFormat dateTimeInstance = DateFormat.getDateTimeInstance();
    dateTimeInstance.setTimeZone(TimeZone.getTimeZone("EST"));
    System.out.println(dateTimeInstance.format(gc.getTime()));
  }

  public void setMediaTree(final MediaTree mediaTree) {
    setTreeRoot(mediaTree.getRoot());
  }

  private final ObservableList<GroupSet> groupSets = FXCollections.observableArrayList(new GroupSet("(ungrouped)"), new GroupSet("Decade"), new GroupSet("Genre"));
  private final ObservableList<SortOrder> sortOrders = FXCollections.observableArrayList(new SortOrder("Alphabetically"), new SortOrder("Chronologically"));

  private final ObjectProperty<GroupSet> groupSet = new SimpleObjectProperty<>(groupSets.get(0));
  private final ObjectProperty<SortOrder> sortOrder = new SimpleObjectProperty<>(sortOrders.get(0));

  public ObservableList<GroupSet> availableGroupSetsProperty() {
    return groupSets;
  }

  public ObjectProperty<GroupSet> groupSetProperty() {
    return groupSet;
  }

  public ObservableList<SortOrder> availableSortOrdersProperty() {
    return sortOrders;
  }

  public ObjectProperty<SortOrder> sortOrderProperty() {
    return sortOrder;
  }
}
