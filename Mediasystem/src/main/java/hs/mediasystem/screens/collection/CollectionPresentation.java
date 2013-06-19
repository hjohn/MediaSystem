package hs.mediasystem.screens.collection;

import hs.ddif.AnnotationDescriptor;
import hs.ddif.Injector;
import hs.ddif.Value;
import hs.mediasystem.MediaRootType;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SettingUpdater;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.StandardTitleComparator;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.Presentation;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.OptionDialogPane;
import hs.mediasystem.util.StringBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.StringConverter;

import javax.inject.Inject;

public class CollectionPresentation implements Presentation {
  public final ObservableList<MediaGroup> availableGroupSets = FXCollections.observableArrayList();
  public final ObjectProperty<MediaGroup> groupSet = new SimpleObjectProperty<>();
  public final ObjectProperty<CollectionSelectorLayoutConf> layoutConf = new SimpleObjectProperty<>();

  public final EventHandler<ActionEvent> onOptionsSelect = new OptionsSelectEventHandler();

  private final SettingUpdater<MediaGroup> mediaGroupSettingUpdater;
  private final ProgramController controller;
  private final CollectionView view;
  private final Injector injector;

  private MediaRoot currentRoot;

  @Inject
  public CollectionPresentation(final ProgramController controller, final CollectionView view, final SettingsStore settingsStore, Injector injector) {
    this.controller = controller;
    this.view = view;
    this.injector = injector;

    view.onOptionsSelect.set(onOptionsSelect);

    groupSet.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        final MediaNode mediaNode = createRootNode(currentRoot);

        view.focusedMediaNode.set(null);
        view.rootMediaNode.set(mediaNode);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            String id = settingsStore.getSetting("MediaSystem:Collection", currentRoot.getId().toString("LastSelected", currentRoot.getRootName()));
            MediaNode nodeToSelect = null;

            if(id != null) {
              nodeToSelect = mediaNode.findMediaNode(id);
            }

            if(nodeToSelect == null) {
              if(groupSet.get().showTopLevelExpanded()) {
                nodeToSelect = mediaNode.getChildren().get(0).getChildren().get(0);
              }
              else {
                nodeToSelect = mediaNode.getChildren().get(0);
              }
            }

            view.focusedMediaNode.set(nodeToSelect);
          }
        });
      }
    });

    view.focusedMediaNode.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        if(current != null) {
          settingsStore.storeSetting("MediaSystem:Collection", PersistLevel.TEMPORARY, currentRoot.getId().toString("LastSelected", currentRoot.getRootName()), current.getId());
        }
      }
    });

    mediaGroupSettingUpdater = new SettingUpdater<>(settingsStore, new StringConverter<MediaGroup>() {
      @Override
      public MediaGroup fromString(String id) {
        for(MediaGroup mediaGroup : availableGroupSets) {
          if(mediaGroup.getId().equals(id)) {
            return mediaGroup;
          }
        }

        return null;
      }

      @Override
      public String toString(MediaGroup mediaGroup) {
        return mediaGroup.getId();
      }
    });

    groupSet.addListener(mediaGroupSettingUpdater);
  }

  @Override
  public Node getView() {
    return view;
  }

  private void setTreeRoot(final MediaRoot root) {
    currentRoot = root;

    List<MediaGroup> mediaGroups = new ArrayList<>(injector.getInstances(MediaGroup.class, AnnotationDescriptor.describe(MediaRootType.class, new Value("value", root.getClass()))));

    if(mediaGroups.isEmpty()) {
      mediaGroups.add(new AbstractMediaGroup("alpha", "Alphabetically", false) {
        @Override
        public List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends MediaItem> mediaItems) {
          Collections.sort(mediaItems, StandardTitleComparator.INSTANCE);
          List<MediaNode> nodes = new ArrayList<>();

          for(MediaItem mediaItem : mediaItems) {
            nodes.add(new MediaNode(mediaItem));
          }

          return nodes;
        }
      });
    }

    Collections.sort(mediaGroups, new Comparator<MediaGroup>() {
      @Override
      public int compare(MediaGroup o1, MediaGroup o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });

    availableGroupSets.setAll(mediaGroups);

    mediaGroupSettingUpdater.setBackingSetting("MediaSystem:Collection", PersistLevel.PERMANENT, currentRoot.getId().toString("SortGroup"));

    MediaGroup selectedMediaGroup = mediaGroupSettingUpdater.getStoredValue(availableGroupSets.get(0));

    groupSet.set(selectedMediaGroup);
  }

  public void setMediaRoot(final MediaRoot mediaRoot) {
    setTreeRoot(mediaRoot);
  }

  // TODO RootNode is a stupid concept, there is really no root node needed -- items should just be an obervablelist -- this will simplify MediaNode code as well
  public MediaNode createRootNode(MediaRoot root) {
    MediaGroup mediaGroup = groupSet.get();

    return new MediaNode(root, null, mediaGroup.showTopLevelExpanded(), false, mediaGroup);
  }

  @Override
  public void dispose() {
  }

  class OptionsSelectEventHandler implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
      @SuppressWarnings("unchecked")
      List<? extends Option> options = FXCollections.observableArrayList(
        new ListOption<>("Sorting/Grouping", groupSet, availableGroupSets, new StringBinding(groupSet) {
          @Override
          protected String computeValue() {
            return groupSet.get().getTitle();
          }
        }),
        new ListOption<>("View", view.layoutConf, view.suitableLayoutConfs, new StringBinding(view.layoutConf) {
          @Override
          protected String computeValue() {
            return view.layoutConf.get().getTitle();
          }
        })
      );

      controller.showDialog(new OptionDialogPane("Media - Options", options));
      event.consume();
    }
  }

  static class GroupItem extends Media<GroupItem> {
  }
}
