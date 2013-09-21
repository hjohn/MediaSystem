package hs.mediasystem.screens.collection;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SettingUpdater;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.GridPaneUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provides the View part of the Collection screen.
 *
 * The CollectionView provides a standard area with a background and a small bottom border in which
 * information about a collection can be shown.  How the collection is shown is determined by the
 * current layout.  The CollectionView manages these layouts and provides controls for choosing one.<p>
 *
 * Provided properties and events:
 * - the collection root under which the media items to be displayed are located
 * - the currently focused media item
 * - an event handler triggered when the options action is selected
 */
public class CollectionView extends StackPane {
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);

  /**
   * The collection root under which the media items to be displayed are located.
   */
  public final ObjectProperty<MediaNode> rootMediaNode = new SimpleObjectProperty<>();

  /**
   * The currently focused media node.
   */
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();

  /**
   * Triggered when the options action is selected.
   */
  public final ObjectProperty<EventHandler<ActionEvent>> onOptionsSelect = new SimpleObjectProperty<>();

  /*
   * TODO the layout properties are accessed externally, but I donot think they should be as they are view specific -- another "CollectionView" for example could be constructed to not use layouts at all for example
   */
  /**
   * The current active layout.
   */
  public final ObjectProperty<Layout<MediaRoot, CollectionSelectorPresentation>> layout = new SimpleObjectProperty<>();

  /**
   * A list of suitable layouts.
   */
  public final ObservableList<Layout<MediaRoot, CollectionSelectorPresentation>> suitableLayouts = FXCollections.observableArrayList();

  private final BackgroundPane backgroundPane = new BackgroundPane();
  private final SettingUpdater<Layout<MediaRoot, CollectionSelectorPresentation>> settingUpdater;
  private final StackPane collectionSelectorPane = new StackPane();
  private final List<Layout<MediaRoot, CollectionSelectorPresentation>> allLayouts;

  private CollectionSelectorPresentation currentCollectionSelectorPresentation;

  @Inject
  public CollectionView(SettingsStore settingsStore, final Provider<CollectionSelectorPresentation> collectionSelectorPresentationProvider, List<Layout<MediaRoot, CollectionSelectorPresentation>> layouts) {
    this.allLayouts = layouts;

    getStylesheets().add("collection/collection-view.css");

    GridPane stage = GridPaneUtil.create(new double[] {100}, new double[] {90, 10});

    stage.add(backgroundPane, 0, 0);
    stage.add(new StackPane() {{
      getStyleClass().add("stage");
    }}, 0, 1);

    getChildren().addAll(stage, collectionSelectorPane);

    addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(KEY_O.match(event)) {
          Events.dispatchEvent(onOptionsSelect, new ActionEvent(), event);
        }
      }
    });

    backgroundPane.mediaNodeProperty().bind(focusedMediaNode);

    layout.addListener(new ChangeListener<Layout<MediaRoot, CollectionSelectorPresentation>>() {
      @Override
      public void changed(ObservableValue<? extends Layout<MediaRoot, CollectionSelectorPresentation>> observableValue, Layout<MediaRoot, CollectionSelectorPresentation> oldLayout, Layout<MediaRoot, CollectionSelectorPresentation> layout) {
        if(currentCollectionSelectorPresentation != null) {
          currentCollectionSelectorPresentation.focusedMediaNode.unbindBidirectional(focusedMediaNode);
        }

        currentCollectionSelectorPresentation = collectionSelectorPresentationProvider.get();

        currentCollectionSelectorPresentation.rootMediaNode.set(rootMediaNode.get());
        currentCollectionSelectorPresentation.focusedMediaNode.bindBidirectional(focusedMediaNode);

        collectionSelectorPane.getChildren().setAll(layout.create(currentCollectionSelectorPresentation));
      }
    });

    // TODO move setting load/save code into presentation
    rootMediaNode.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        determineValidLayouts(current.getMediaRoot());

        settingUpdater.setBackingSetting("MediaSystem:Collection", PersistLevel.PERMANENT, current.getMediaRoot().getId().toString("View"));

        Layout<MediaRoot, CollectionSelectorPresentation> lastSelectedLayout = settingUpdater.getStoredValue(layouts.get(0));

        if(!lastSelectedLayout.equals(layout.get())) {
          layout.set(lastSelectedLayout);
        }

        currentCollectionSelectorPresentation.rootMediaNode.set(current);
      }
    });

    settingUpdater = new SettingUpdater<>(settingsStore, new StringConverter<Layout<MediaRoot, CollectionSelectorPresentation>>() {
      @Override
      public Layout<MediaRoot, CollectionSelectorPresentation> fromString(String id) {
        for(Layout<MediaRoot, CollectionSelectorPresentation> layout : layouts) {
          if(layout.getId().equals(id)) {
            return layout;
          }
        }

        return null;
      }

      @Override
      public String toString(Layout<MediaRoot, CollectionSelectorPresentation> layout) {
        return layout.getId();
      }
    });

    layout.addListener(settingUpdater);
  }

  @Override
  public void requestFocus() {
    if(currentCollectionSelectorPresentation != null) {
      currentCollectionSelectorPresentation.defaultInputFocus.get().requestFocus();
    }
  }

  private void determineValidLayouts(MediaRoot root) {
    suitableLayouts.clear();

    for(Layout<MediaRoot, CollectionSelectorPresentation> layout : allLayouts) {
      if(layout.isSuitableFor(root)) {
        suitableLayouts.add(layout);
      }
    }

    Collections.sort(suitableLayouts, new Comparator<Layout<MediaRoot, CollectionSelectorPresentation>>() {
      @Override
      public int compare(Layout<MediaRoot, CollectionSelectorPresentation> o1, Layout<MediaRoot, CollectionSelectorPresentation> o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });
  }
}
