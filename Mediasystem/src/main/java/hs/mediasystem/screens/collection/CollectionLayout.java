package hs.mediasystem.screens.collection;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.Location;
import hs.mediasystem.screens.MediaNode;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class CollectionLayout implements Layout<Location, CollectionPresentation> {
  private final SettingsStore settingsStore;
  private final Provider<CollectionPresentation> presentationProvider;

  @Inject
  public CollectionLayout(SettingsStore settingsStore, Provider<CollectionPresentation> presentationProvider) {
    this.settingsStore = settingsStore;
    this.presentationProvider = presentationProvider;
  }

  @Override
  public Class<?> getContentClass() {
    return CollectionLocation.class;
  }

  @Override
  public CollectionPresentation createPresentation() {
    return presentationProvider.get();
  }

  @Override
  public Node createView(CollectionPresentation presentation) {
    CollectionView view = new CollectionView();

    Bindings.bindContentBidirectional(view.mediaNodes, presentation.mediaNodes);

    view.layout.bindBidirectional(presentation.layout);
    view.expandTopLevel.bind(presentation.expandTopLevel);

    view.onSelect.set(presentation::handleMediaNodeSelectEvent);

    presentation.layout.addListener(new ChangeListener<Layout<?, ?>>() {
      @Override
      public void changed(ObservableValue<? extends Layout<?, ?>> observable, Layout<?, ?> old, Layout<?, ?> current) {

        /*
         * When layout changes, select the last selected node:
         */

        String id = settingsStore.getSetting("MediaSystem:Collection", presentation.mediaRoot.get().getId().toString("LastSelected", presentation.mediaRoot.get().getRootName()));

        if(id != null) {
          MediaNode nodeToSelect = null;

          for(MediaNode mediaNode : presentation.mediaNodes) {
            nodeToSelect = mediaNode.findMediaNodeById(id);

            if(nodeToSelect != null) {
              view.focusedMediaNode.set(nodeToSelect);
              break;
            }
          }
        }
      }
    });

    view.focusedMediaNode.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {

        /*
         * Check if the new node isn't null and that it doesn't have the same id.  Different
         * MediaNode instances can have the same id, which means some changes in focusedMediaNode
         * donot require updating the last selected setting.
         */

        if(current != null && (old == null || !old.getId().equals(current.getId()))) {
          settingsStore.storeSetting("MediaSystem:Collection", PersistLevel.TEMPORARY, presentation.mediaRoot.get().getId().toString("LastSelected", presentation.mediaRoot.get().getRootName()), current.getId());
        }
      }
    });

    return view;
  }
}
