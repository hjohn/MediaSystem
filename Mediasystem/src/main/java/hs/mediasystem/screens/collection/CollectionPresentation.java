package hs.mediasystem.screens.collection;

import hs.ddif.AnnotationDescriptor;
import hs.ddif.Injector;
import hs.ddif.Value;
import hs.mediasystem.MediaRootType;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.SettingUpdater;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.StandardTitleComparator;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.LocationChangeEvent;
import hs.mediasystem.screens.MainLocationPresentation;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import hs.mediasystem.screens.UserLayout;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.OptionDialogPane;
import hs.mediasystem.screens.playback.PlaybackLocation;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.javafx.Dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.util.StringConverter;

import javax.inject.Inject;

public class CollectionPresentation extends MainLocationPresentation<CollectionLocation> {
  private final ReadOnlyListWrapper<MediaNode> mediaNodesWrapper = new ReadOnlyListWrapper<>(FXCollections.<MediaNode>observableArrayList());
  private final ReadOnlyBooleanWrapper expandTopLevelWrapper = new ReadOnlyBooleanWrapper();

  /**
   * The collection root under which the media items to be displayed are located.
   */
  public final ObjectProperty<MediaRoot> mediaRoot = new SimpleObjectProperty<>();

  /**
   * The MediaNodes to display.
   */
  public final ReadOnlyListProperty<MediaNode> mediaNodes = mediaNodesWrapper.getReadOnlyProperty();

  /**
   * Whether or not the top level of MediaNodes should be displayed in expanded form.
   */
  public final ReadOnlyBooleanProperty expandTopLevel = expandTopLevelWrapper.getReadOnlyProperty();

  /**
   * The current active layout.
   */
  public final ObjectProperty<UserLayout<MediaRoot, CollectionSelectorPresentation>> layout = new SimpleObjectProperty<>();

  /**
   * All suitable layouts that are available to choose from.
   */
  public final ObservableList<UserLayout<MediaRoot, CollectionSelectorPresentation>> suitableLayouts = FXCollections.observableArrayList();

  /**
   * The current active group set.
   */
  public final ObjectProperty<MediaGroup> groupSet = new SimpleObjectProperty<>();

  /**
   * All available group sets that are available to choose from.
   */
  public final ObservableList<MediaGroup> availableGroupSets = FXCollections.observableArrayList();

  /**
   * The filter to apply to the collection.
   */
  public final ObjectProperty<InclusionFilter> inclusionFilter = new SimpleObjectProperty<>();

  private final List<UserLayout<MediaRoot, CollectionSelectorPresentation>> layouts;

  @Inject
  public CollectionPresentation(SettingsStore settingsStore, Injector injector, List<UserLayout<MediaRoot, CollectionSelectorPresentation>> layouts) {
    this.layouts = layouts;

    /*
     * Create and register setting updaters for layout and groupSet properties.
     */

    SettingUpdater<UserLayout<MediaRoot, CollectionSelectorPresentation>> userLayoutSettingUpdater = new SettingUpdater<>(settingsStore, new UserLayoutStringConverter());
    SettingUpdater<MediaGroup> mediaGroupSettingUpdater = new SettingUpdater<>(settingsStore, new MediaGroupStringConverter(availableGroupSets));

    layout.addListener(userLayoutSettingUpdater);
    groupSet.addListener(mediaGroupSettingUpdater);

    /*
     * Add Location listener to update the underlying view with a new MediaRoot.
     */

    location.addListener(new ChangeListener<CollectionLocation>() {
      @Override
      public void changed(ObservableValue<? extends CollectionLocation> observableValue, CollectionLocation old, CollectionLocation current) {

        /*
         * First clear the current Layout so it will not respond to changes made to the groupSet --
         * although this should not cause errors, it does potentially cause a lot of overhead when
         * the new layout first applies the wrong groupSet before the correct one is set.
         */

        userLayoutSettingUpdater.clearBackingSetting();  // clear this first otherwise it will try to store the change of layout to null
        layout.set(null);

        mediaRoot.set(current.getMediaRoot());

        /*
         * Update available group sets and sort them.
         */

        List<MediaGroup> mediaGroups = new ArrayList<>(injector.getInstances(MediaGroup.class, AnnotationDescriptor.describe(MediaRootType.class, new Value("value", current.getMediaRoot().getClass()))));

        if(mediaGroups.isEmpty()) {

          /*
           * Provide a default if none are found.
           */

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

        /*
         * Change setting name for mediaGroupSettingUpdater as each MediaRoot has its own MediaGroup setting.
         */

        mediaGroupSettingUpdater.setBackingSetting("MediaSystem:Collection", PersistLevel.PERMANENT, mediaRoot.get().getId().toString("SortGroup"));

        /*
         * Restore the last selected MediaGroup for this MediaRoot.
         */

        MediaGroup selectedMediaGroup = mediaGroupSettingUpdater.getStoredValue(availableGroupSets.get(0));

        groupSet.set(selectedMediaGroup);

        /*
         * Update suitable layouts and sort them.
         */

        suitableLayouts.setAll(Layout.findAllSuitableLayouts(layouts, mediaRoot.get().getClass()));

        Collections.sort(suitableLayouts, new Comparator<UserLayout<MediaRoot, CollectionSelectorPresentation>>() {
          @Override
          public int compare(UserLayout<MediaRoot, CollectionSelectorPresentation> o1, UserLayout<MediaRoot, CollectionSelectorPresentation> o2) {
            return o1.getTitle().compareTo(o2.getTitle());
          }
        });

        /*
         * Change setting name for userLayoutSettingUpdater as each MediaRoot has its own Layout setting.
         */

        userLayoutSettingUpdater.setBackingSetting("MediaSystem:Collection", PersistLevel.PERMANENT, mediaRoot.get().getId().toString("View"));

        /*
         * Restore the last selected Layout for this MediaRoot.
         */

        UserLayout<MediaRoot, CollectionSelectorPresentation> lastSelectedLayout = userLayoutSettingUpdater.getStoredValue(layouts.get(0));

        if(!lastSelectedLayout.equals(layout.get())) {
          layout.set(lastSelectedLayout);
        }
      }
    });

    /*
     * Add invalidation listeners to update media nodes.
     */

    groupSet.addListener(this::createMediaNodes);
    inclusionFilter.addListener(this::createMediaNodes);
  }

  private void createMediaNodes(@SuppressWarnings("unused") Observable observable) {
    expandTopLevelWrapper.set(groupSet.get().showTopLevelExpanded());

    List<MediaItem> filteredItems = new ArrayList<>();
    InclusionFilter inclusionFilter = this.inclusionFilter.get();

    for(MediaItem mediaItem : mediaRoot.get().getItems()) {
      if(inclusionFilter == null || inclusionFilter.test(mediaItem)) {
        filteredItems.add(mediaItem);
      }
    }

    mediaNodesWrapper.setAll(groupSet.get().getMediaNodes(mediaRoot.get(), filteredItems));
  }

  /**
   * EventHandler for when a MediaNode is selected.  This will trigger either
   * a drill down or playback depending on the type of node.
   */
  public void handleMediaNodeSelectEvent(MediaNodeEvent event) {
    if(event.getMediaNode().getMediaItem() instanceof MediaRoot) {
      Event.fireEvent(event.getTarget(), new LocationChangeEvent(new CollectionLocation((MediaRoot)event.getMediaNode().getMediaItem())));
    }
    else {
      Event.fireEvent(event.getTarget(), new LocationChangeEvent(new PlaybackLocation(null, event.getMediaNode().getMediaItem(), 0)));
    }
    event.consume();
  }

  /**
   * EventHandler for showing the Options dialog.
   */
  public void handleOptionsSelectEvent(ActionEvent event) {
    @SuppressWarnings("unchecked")
    List<? extends Option> options = FXCollections.observableArrayList(
      new ListOption<>("Sorting/Grouping", groupSet, availableGroupSets, new StringBinding(groupSet) {
        @Override
        protected String computeValue() {
          return groupSet.get().getTitle();
        }
      }),
      new ListOption<>("View", layout, suitableLayouts, new StringBinding(layout) {
        @Override
        protected String computeValue() {
          return layout.get().getTitle();
        }
      })
    );

    Dialogs.show(event, new OptionDialogPane("Media - Options", options));
    event.consume();
  }

  /**
   * Converter for storing the MediaGroup as a Setting
   */
  class MediaGroupStringConverter extends StringConverter<MediaGroup> {
    private final ObservableList<MediaGroup> availableGroupSets;

    public MediaGroupStringConverter(ObservableList<MediaGroup> availableGroupSets) {
      this.availableGroupSets = availableGroupSets;
    }

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
  }

  /**
   * Converter for storing the UserLayout as a Setting
   */
  class UserLayoutStringConverter extends StringConverter<UserLayout<MediaRoot, CollectionSelectorPresentation>> {
    @Override
    public UserLayout<MediaRoot, CollectionSelectorPresentation> fromString(String id) {
      for(UserLayout<MediaRoot, CollectionSelectorPresentation> layout : layouts) {
        if(layout.getId().equals(id)) {
          return layout;
        }
      }

      return null;
    }

    @Override
    public String toString(UserLayout<MediaRoot, CollectionSelectorPresentation> layout) {
      return layout.getId();
    }
  }

  public static class InclusionFilter implements Predicate<MediaItem> {
    private final Integer afterYear;
    private final Integer beforeYear;
    private final boolean includeViewed;
    private final boolean includeNotViewed;
    private final Set<String> genres;

    public InclusionFilter(Integer afterYear, Integer beforeYear, boolean includeViewed, boolean includeNotViewed, Set<String> genres) {
      this.afterYear = afterYear;
      this.beforeYear = beforeYear;
      this.includeViewed = includeViewed;
      this.includeNotViewed = includeNotViewed;
      this.genres = genres == null ? new HashSet<>() : genres;
    }

    public Integer getAfterYear() {
      return afterYear;
    }

    public Integer getBeforeYear() {
      return beforeYear;
    }

    public boolean isIncludeViewed() {
      return includeViewed;
    }

    public boolean isIncludeNotViewed() {
      return includeNotViewed;
    }

    public Set<String> getGenres() {
      return Collections.unmodifiableSet(genres);
    }

    @Override
    public boolean test(MediaItem mediaItem) {
      if(!includeViewed || !includeNotViewed) {
        MediaData mediaData = mediaItem.mediaData.get();

        if(mediaData == null) {

          /*
           * Asked to filter on Viewed state, but Viewed state is unknown.
           */

          return false;
        }

        if(!includeViewed && mediaData.viewed.get()) {
          return false;
        }

        if(!includeNotViewed && !mediaData.viewed.get()) {
          return false;
        }
      }

      if(afterYear != null || beforeYear != null) {
        Media<?> media = mediaItem.media.get();

        if(media == null || media.releaseDate.get() == null) {

          /*
           * Asked to filter on Year, but Year is unknown.
           */

          return false;
        }

        int releaseYear = media.releaseDate.get().getYear();
        int afterYear = this.afterYear == null ? 0 : this.afterYear;
        int beforeYear = this.beforeYear == null ? Integer.MAX_VALUE : this.beforeYear;

        if(releaseYear <= afterYear || releaseYear >= beforeYear) {
          return false;
        }
      }

      if(genres != null && !genres.isEmpty()) {
        Media<?> media = mediaItem.media.get();

        if(media == null || media.genres.get() == null) {

          /*
           * Asked to filter on Genre, but Genres are unknown.
           */

          return false;
        }

        Set<String> mediaGenres = new HashSet<>(Arrays.asList(media.genres.get()));

        for(String genre : genres) {
          if(!mediaGenres.contains(genre)) {
            return false;
          }
        }
      }

      return true;
    }
  }
}
