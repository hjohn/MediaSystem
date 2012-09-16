package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SettingUpdater;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.StandardTitleComparator;
import hs.mediasystem.screens.DefaultMediaGroup;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.Presentation;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.OptionDialogPane;
import hs.mediasystem.util.DialogPane;
import hs.mediasystem.util.GridPaneUtil;
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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

import javax.inject.Inject;

import org.osgi.framework.BundleContext;

public class SelectMediaPresentation implements Presentation {
  private static final KeyCombination KEY_O = new KeyCodeCombination(KeyCode.O);

  private final ServiceTracker<MediaGroup> mediaGroupTracker;
  private final SettingUpdater<MediaGroup> mediaGroupSettingUpdater;
  private final BundleContext bundleContext;
  private final ProgramController controller;

  private SelectMediaView view;


  @Inject
  public SelectMediaPresentation(final ProgramController controller, final SelectMediaView view, final SettingsStore settingsStore, BundleContext bundleContext) {
    this.controller = controller;
    this.bundleContext = bundleContext;
    this.view = view;

    mediaGroupTracker = new ServiceTracker<>(bundleContext, MediaGroup.class);

    view.onNodeSelected().set(new EventHandler<MediaNodeEvent>() {
      @Override
      public void handle(MediaNodeEvent event) {
        if(event.getMediaNode().getMediaItem() instanceof MediaRoot) {
          controller.setLocation(new SelectMediaLocation((MediaRoot)event.getMediaNode().getMediaItem()));
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
        controller.showDialog(createInformationDialog(event.getMediaNode()));

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

          controller.showDialog(new OptionDialogPane("Media - Options", options));
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
            String key = createKey("LastSelected");
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
          settingsStore.storeSetting("MediaSystem:SelectMedia", PersistLevel.TEMPORARY, createKey("LastSelected"), current.getId());
        }
      }
    });
  }

  @Override
  public Node getView() {
    return (Node)view;
  }

  private MediaRoot currentRoot;

  private void setTreeRoot(final MediaRoot root) {
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

    mediaGroupSettingUpdater.setBackingSetting("MediaSystem:SelectMedia", PersistLevel.PERMANENT, createKey("SortGroup"));

    MediaGroup selectedMediaGroup = mediaGroupSettingUpdater.getStoredValue(availableGroupSetsProperty().get(0));

    groupSetProperty().set(selectedMediaGroup);
  }

  public void setMediaTree(final MediaRoot mediaRoot) {
    setTreeRoot(mediaRoot);
  }

  private String createKey(String prefix) {
    return prefix + ":" + controller.getLocation().getId();
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

          Media<?> media = mediaGroup.createMediaFromFirstItem(group.get(0));
          String shortTitle = mediaGroup.getShortTitle(group.get(0));

          Media<?> groupMedia = new Media<>(null);
          groupMedia.title.set(media.title.get());
          MediaNode groupNode = new MediaNode(mediaGroup.getId() + "[" + media.title.get() + "]", groupMedia, shortTitle);

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

  private DialogPane createInformationDialog(final MediaNode mediaNode) {
    final DetailPane detailPane = new DetailPane(bundleContext) {
      {
        getStylesheets().add("controls.css");
      }

      @Override
      protected void initialize(DecoratablePane decoratablePane) {
        BorderPane borderPane = new BorderPane();
        GridPane gridPane = GridPaneUtil.create(new double[] {33, 34, 33}, new double[] {100});
        gridPane.setHgap(20);

        HBox hbox = new HBox();
        hbox.setId("link-area");

        decoratablePane.getChildren().add(borderPane);

        borderPane.setCenter(gridPane);
        borderPane.setBottom(hbox);

        gridPane.add(new VBox() {{
          setId("title-image-area");
        }}, 0, 0);

        gridPane.add(new VBox() {{
          getChildren().add(new VBox() {{
            setId("title-area");
          }});

          getChildren().add(new VBox() {{
            setId("description-area");
          }});
        }}, 1, 0);

        gridPane.add(new VBox() {{
          setId("action-area");
          setSpacing(2);
        }}, 2, 0);
      }
    };

    detailPane.contentProperty().set(mediaNode.getMedia());

    DialogPane dialogPane = new DialogPane() {
      @Override
      public void close() {
        super.close();

        detailPane.contentProperty().set(null);
      }
    };

    dialogPane.getChildren().add(detailPane);

    return dialogPane;
  }

  @Override
  public void dispose() {
  }
}
