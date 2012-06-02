package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.media.Media;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.Navigator;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.SelectMediaView;
import hs.mediasystem.screens.optiondialog.ActionOption;
import hs.mediasystem.screens.optiondialog.BooleanOption;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.Callable;
import hs.mediasystem.util.ImageCache;
import hs.mediasystem.util.StateCache;
import hs.mediasystem.util.StringBinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.util.Callback;

import javax.inject.Inject;

public class SelectMediaPresentation {
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);
  private static final Map<Class<? extends MediaRoot>, List<MediaGroup>> MEDIA_GROUPS = new HashMap<>();

  public static void registerMediaGroup(Class<? extends MediaRoot> mediaRootClass, MediaGroup mediaGroup) {
    List<MediaGroup> mediaGroups = MEDIA_GROUPS.get(mediaRootClass);

    if(mediaGroups == null) {
      mediaGroups = new ArrayList<>();
      MEDIA_GROUPS.put(mediaRootClass, mediaGroups);
    }

    mediaGroups.add(mediaGroup);
  }

  private final Navigator navigator;
  private final StateCache stateCache;

  private SelectMediaView view;

  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaView view, final StateCache stateCache) {
    this.stateCache = stateCache;
    this.navigator = new Navigator(controller.getNavigator());
    this.view = view;

    if(this.view != null) {
      this.view.onBack().set(null);
      this.view.onNodeSelected().set(null);
      this.view.onNodeAlternateSelect().set(null);
    }

    this.view = view;

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
        List<? extends Option> options = FXCollections.observableArrayList(
          new BooleanOption("Viewed", mediaItem.viewedProperty(), new StringBinding(mediaItem.viewedProperty()) {
            @Override
            protected String computeValue() {
              return mediaItem.viewedProperty().get() ? "Yes" : "No";
            }
          }),
          new ActionOption("Reload meta data", new Callable<Boolean>() {
            @Override
            public Boolean call() {
              mediaItem.reloadMetaData();

              Media media = mediaItem.get(Media.class);

              ImageCache.expunge(media.getBanner());
              ImageCache.expunge(media.getImage());
              ImageCache.expunge(media.getBackground());

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
            new ListOption<>("Group by", groupSetProperty(), availableGroupSetsProperty(), new StringBinding(groupSetProperty()) {
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
            String key = createKeyFromTrail();
            String id = stateCache.getState(key);
            MediaNode nodeToSelect = null;

            if(id != null) {
              nodeToSelect = mediaNode.findMediaNode(id);
            }

            view.setSelectedNode(nodeToSelect);
          }
        });
      }
    });
  }

  public Node getView() {
    return (Node)view;
  }

  private MediaRoot currentRoot;

  private void setTreeRoot(final MediaRoot root) {
    navigator.navigateTo(new Destination(root.getRootName()) {
      @Override
      public void execute() {
        currentRoot = root;

        List<MediaGroup> mediaGroups = MEDIA_GROUPS.get(root.getClass());

        availableGroupSetsProperty().setAll(mediaGroups);
        groupSetProperty().set(mediaGroups.get(0));
      }

      @Override
      protected void outro() {
        MediaNode selectedNode = view.getSelectedNode();

        if(selectedNode != null) {
          stateCache.putState(createKeyFromTrail(), selectedNode.getId());
        }
      }
    });
  }

  public void setMediaTree(final MediaRoot mediaRoot) {
    setTreeRoot(mediaRoot);
  }

  private String createKeyFromTrail() {
    String key = "";

    for(Destination destination : navigator.getTrail()) {
      if(!key.isEmpty()) {
        key += ";";
      }
      key += destination.getDescription();
    }

    return key;
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

          MediaNode groupNode = new MediaNode(media.getTitle(), shortTitle, media.getReleaseYear());

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
