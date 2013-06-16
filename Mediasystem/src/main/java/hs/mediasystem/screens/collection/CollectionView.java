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
 * Container Pane for managing the various layouts that can be used for displaying a collection of media.
 */
public class CollectionView extends StackPane {
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);

  public final ObjectProperty<CollectionSelectorLayoutConf> layoutConf = new SimpleObjectProperty<>();
  public final ObjectProperty<MediaNode> rootMediaNode = new SimpleObjectProperty<>();
  public final ObjectProperty<MediaNode> focusedMediaNode = new SimpleObjectProperty<>();
  public final ObjectProperty<EventHandler<ActionEvent>> onOptionsSelect = new SimpleObjectProperty<>();
  public final ObservableList<CollectionSelectorLayoutConf> suitableLayoutConfs = FXCollections.observableArrayList();

  private final BackgroundPane backgroundPane = new BackgroundPane();
  private final SettingUpdater<CollectionSelectorLayoutConf> settingUpdater;
  private final StackPane collectionSelectorPane = new StackPane();
  private final List<CollectionSelectorLayoutConf> allLayoutConfs;

  private CollectionSelectorPresentation currentCollectionSelectorPresentation;

  @Inject
  public CollectionView(SettingsStore settingsStore, final Provider<CollectionSelectorPresentation> collectionSelectorPresentationProvider, List<CollectionSelectorLayoutConf> layoutConfs) {
    this.allLayoutConfs = layoutConfs;

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

    layoutConf.addListener(new ChangeListener<CollectionSelectorLayoutConf>() {
      @Override
      public void changed(ObservableValue<? extends CollectionSelectorLayoutConf> observableValue, CollectionSelectorLayoutConf oldLayoutConf, CollectionSelectorLayoutConf layoutConf) {
        if(currentCollectionSelectorPresentation != null) {
          currentCollectionSelectorPresentation.focusedMediaNode.unbindBidirectional(focusedMediaNode);
        }

        currentCollectionSelectorPresentation = collectionSelectorPresentationProvider.get();

        currentCollectionSelectorPresentation.rootMediaNode.set(rootMediaNode.get());
        currentCollectionSelectorPresentation.focusedMediaNode.bindBidirectional(focusedMediaNode);

        collectionSelectorPane.getChildren().setAll(layoutConf.create(currentCollectionSelectorPresentation));
      }
    });

    rootMediaNode.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        determineValidLayoutConfs(current.getMediaRoot());

        settingUpdater.setBackingSetting("MediaSystem:Collection", PersistLevel.PERMANENT, "View:" + current.getMediaRoot().getId());

        CollectionSelectorLayoutConf lastSelectedLayoutConf = settingUpdater.getStoredValue(layoutConfs.get(0));

        if(!lastSelectedLayoutConf.equals(layoutConf.get())) {
          layoutConf.set(lastSelectedLayoutConf);
        }

        currentCollectionSelectorPresentation.rootMediaNode.set(current);
      }
    });

    settingUpdater = new SettingUpdater<>(settingsStore, new StringConverter<CollectionSelectorLayoutConf>() {
      @Override
      public CollectionSelectorLayoutConf fromString(String id) {
        for(CollectionSelectorLayoutConf layoutConf : layoutConfs) {
          if(layoutConf.getId().equals(id)) {
            return layoutConf;
          }
        }

        return null;
      }

      @Override
      public String toString(CollectionSelectorLayoutConf layoutConf) {
        return layoutConf.getId();
      }
    });

    layoutConf.addListener(settingUpdater);
  }

  @Override
  public void requestFocus() {
    if(currentCollectionSelectorPresentation != null) {
      currentCollectionSelectorPresentation.defaultInputFocus.get().requestFocus();
    }
  }

  private void determineValidLayoutConfs(MediaRoot root) {
    suitableLayoutConfs.clear();

    for(CollectionSelectorLayoutConf layoutConf : allLayoutConfs) {
      if(layoutConf.isSuitableFor(root)) {
        suitableLayoutConfs.add(layoutConf);
      }
    }

    Collections.sort(suitableLayoutConfs, new Comparator<CollectionSelectorLayoutConf>() {
      @Override
      public int compare(CollectionSelectorLayoutConf o1, CollectionSelectorLayoutConf o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });
  }
}