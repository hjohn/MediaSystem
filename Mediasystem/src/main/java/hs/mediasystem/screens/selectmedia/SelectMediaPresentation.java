package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SettingUpdater;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.fs.StandardTitleComparator;
import hs.mediasystem.screens.DefaultMediaGroup;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.Navigator;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.optiondialog.ActionOption;
import hs.mediasystem.screens.optiondialog.BooleanOption;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.PropertyEq;
import hs.mediasystem.util.ServiceTracker;
import hs.mediasystem.util.StringBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.inject.Inject;

import org.osgi.framework.BundleContext;

public class SelectMediaPresentation {
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);

  private final Navigator navigator;
  private final ServiceTracker<MediaGroup> mediaGroupTracker;
  private final SettingUpdater<MediaGroup> mediaGroupSettingUpdater;

  private SelectMediaView view;

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaView view, final SettingsStore settingsStore, BundleContext bundleContext) {
    this.navigator = new Navigator(controller.getNavigator());
    this.view = view;

    mediaGroupTracker = new ServiceTracker<>(bundleContext, MediaGroup.class);

    view.onBack().set(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        navigator.back();
        event.consume();
      }
    });

    view.onNodeSelected().set(new EventHandler<MediaNodeEvent>() {
      @Override
      public void handle(MediaNodeEvent event) {
        if(event.getMediaNode().getMediaItem() instanceof MediaRoot) {
          setTreeRoot((MediaRoot)event.getMediaNode().getMediaItem());
        }
        else {
          controller.play(event.getMediaNode().getMediaItem());
        }
        event.consume();
      }
    });

    view.onNodeAlternateSelect().set(new EventHandler<MediaNodeEvent>() {
      @Override
      public void handle(MediaNodeEvent event) {
        final MediaItem mediaItem = event.getMediaNode().getMediaItem();

        List<Option> options = new ArrayList<>();

        if(mediaItem != null) {
          MediaData mediaData = mediaItem.get(MediaData.class);

          if(mediaData != null) {
            final BooleanProperty viewedProperty = mediaData.viewedProperty();

            options.add(new BooleanOption("Viewed", viewedProperty, new StringBinding(viewedProperty) {
              @Override
              protected String computeValue() {
                return viewedProperty.get() ? "Yes" : "No";
              }
            }));
          }

          options.add(new ActionOption("Reload meta data", new Callable<Boolean>() {
            @Override
            public Boolean call() {
              mediaItem.reloadMetaData();

              Media media = mediaItem.get(Media.class);

              ImageCache.expunge(media.getBanner());
              ImageCache.expunge(media.getImage());
              ImageCache.expunge(media.getBackground());

              return true;
            }
          }));

          controller.showOptionScreen("Options: " + mediaItem.getTitle(), options);
        }

        event.consume();
      }
    });

    getView().setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if(KEY_O.match(event)) {
          @SuppressWarnings("unchecked")
          List<? extends Option> options = FXCollections.observableArrayList(
            new ListOption<>("Sorting/Grouping", groupSetProperty(), availableGroupSetsProperty(), new StringBinding(groupSetProperty()) {
              @Override
              protected String computeValue() {
                return groupSetProperty().get().getTitle();
              }
            }),
            new ListOption<>("View", view.layoutExtensionProperty(), view.availableLayoutExtensionsList(), new StringBinding(view.layoutExtensionProperty()) {
              @Override
              protected String computeValue() {
                return view.layoutExtensionProperty().get().getTitle();
              }
            })
          );

          controller.showOptionScreen("Media - Options", options);
          event.consume();
        }
      }
    });

    groupSetProperty().addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        final MediaNode mediaNode = createRootNode(currentRoot);

        view.setRoot(mediaNode);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            String key = createKeyFromTrail("LastSelected");
            String id = settingsStore.getSetting("MediaSystem:SelectMedia", key);
            MediaNode nodeToSelect = null;

            if(id != null) {
              nodeToSelect = mediaNode.findMediaNode(id);
            }

            view.setSelectedNode(nodeToSelect);
          }
        });
      }
    });

    mediaGroupSettingUpdater = new SettingUpdater<>(settingsStore, new StringConverter<MediaGroup>() {
      @Override
      public MediaGroup fromString(String id) {
        for(MediaGroup mediaGroup : availableGroupSetsProperty()) {
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

    groupSetProperty().addListener(mediaGroupSettingUpdater);

    view.focusedNodeProperty().addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        if(current != null) {
          settingsStore.storeSetting("MediaSystem:SelectMedia", PersistLevel.TEMPORARY, createKeyFromTrail("LastSelected"), current.getId());
        }
      }
    });
  }

  public Node getView() {
    return (Node)view;
  }

  private MediaRoot currentRoot;

  private void setTreeRoot(final MediaRoot root) {
    navigator.navigateTo(new Destination(root.getId(), root.getRootName()) {
      @Override
      public void execute() {
        currentRoot = root;

        List<MediaGroup> mediaGroups = mediaGroupTracker.getServices(new PropertyEq(MediaGroup.Constants.MEDIA_ROOT_CLASS.name(), root.getClass()));

        if(mediaGroups.isEmpty()) {
          mediaGroups.add(new DefaultMediaGroup("alpha", "Alphabetically", null, StandardTitleComparator.INSTANCE, false, false));
        }

        Collections.sort(mediaGroups, new Comparator<MediaGroup>() {
          @Override
          public int compare(MediaGroup o1, MediaGroup o2) {
            return o1.getTitle().compareTo(o2.getTitle());
          }
        });
        availableGroupSetsProperty().setAll(mediaGroups);

        mediaGroupSettingUpdater.setBackingSetting("MediaSystem:SelectMedia", PersistLevel.PERMANENT, createKeyFromTrail("SortGroup"));

        MediaGroup selectedMediaGroup = mediaGroupSettingUpdater.getStoredValue(availableGroupSetsProperty().get(0));

        groupSetProperty().set(selectedMediaGroup);
      }
    });
  }

  public void setMediaTree(final MediaRoot mediaRoot) {
    setTreeRoot(mediaRoot);
  }

  private String createKeyFromTrail(String prefix) {
    String key = "";

    for(Destination destination : navigator.getTrail()) {
      if(!key.isEmpty()) {
        key += "/";
      }
      key += destination.getId();
    }

    return prefix + ":" + key;
  }

  private List<MediaNode> getChildren(MediaRoot mediaRoot) {
    List<? extends MediaItem> children = mediaRoot.getItems();
    List<MediaNode> output = new ArrayList<>();

    MediaGroup mediaGroup = groupSet.get();

    output.addAll(applyGroup(children, mediaGroup));

    return output;
  }

  private List<MediaNode> applyGroup(List<? extends MediaItem> children, MediaGroup mediaGroup) {
    Collections.sort(children, mediaGroup.getSortComparator());
    Grouper<MediaItem> grouper = mediaGroup.getGrouper();
    List<MediaNode> output = new ArrayList<>();

    if(grouper != null) {
      Collection<List<MediaItem>> groupedItems = Groups.group(children, grouper);

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1 || mediaGroup.isAllowedSingleItemGroups()) {
          Collections.sort(group, mediaGroup.getSortComparator());

          Media media = mediaGroup.createMediaFromFirstItem(group.get(0));
          String shortTitle = mediaGroup.getShortTitle(group.get(0));

          Media groupMedia = new Media(media.getTitle());
          MediaNode groupNode = new MediaNode(mediaGroup.getId() + "[" + media.getTitle() + "]", groupMedia, shortTitle);

          List<MediaNode> nodeChildren = new ArrayList<>();

          for(MediaItem item : group) {
            nodeChildren.add(new MediaNode(item));
          }

          groupNode.setChildren(nodeChildren);
          output.add(groupNode);
        }
        else {
          output.add(new MediaNode(group.get(0)));
        }
      }
    }
    else {
      for(MediaItem mediaItem : children) {
        output.add(new MediaNode(mediaItem));
      }
    }

    return output;
  }

  public MediaNode createRootNode(MediaRoot root) {
    MediaGroup mediaGroup = groupSet.get();

    return new MediaNode(root, mediaGroup.showTopLevelExpanded(), new Callback<MediaRoot, List<MediaNode>>() {
      @Override
      public List<MediaNode> call(MediaRoot mediaRoot) {
        return getChildren(mediaRoot);
      }
    });
  }

  private final ObservableList<MediaGroup> groupSets = FXCollections.observableArrayList();
  public ObservableList<MediaGroup> availableGroupSetsProperty() { return groupSets; }

  private final ObjectProperty<MediaGroup> groupSet = new SimpleObjectProperty<>();
  public ObjectProperty<MediaGroup> groupSetProperty() { return groupSet; }
}
