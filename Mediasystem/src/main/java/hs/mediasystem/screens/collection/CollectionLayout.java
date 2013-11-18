package hs.mediasystem.screens.collection;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.Location;
import hs.mediasystem.screens.MediaNode;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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

    // TODO this listener sets the last selected node after every groupset change...
    // - [no more] it is triggered initially because groupsets are set up (good, although it should not need to be a listener for that -- this is why it must be before groupset is bound)
    // - it is triggered after every groupset change because the View is too stupid to maintain selection itself (bad)

    presentation.groupSet.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        selectLastSelected(presentation, view);
      }
    });

    Bindings.bindContentBidirectional(view.mediaNodes, presentation.mediaNodes);

    view.layout.bindBidirectional(presentation.layout);
    view.expandTopLevel.bind(presentation.expandTopLevel);

    view.onSelect.set(presentation::handleMediaNodeSelectEvent);
    view.onOptionsSelect.set(presentation::handleOptionsSelectEvent);

    selectLastSelected(presentation, view);

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

  private void selectLastSelected(CollectionPresentation presentation, CollectionView view) {
    view.focusedMediaNode.set(null);

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        String id = settingsStore.getSetting("MediaSystem:Collection", presentation.mediaRoot.get().getId().toString("LastSelected", presentation.mediaRoot.get().getRootName()));

        view.selectMediaNodeById(id);
      }
    });
  }
}
