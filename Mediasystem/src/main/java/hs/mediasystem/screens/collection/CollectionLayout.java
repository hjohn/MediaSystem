package hs.mediasystem.screens.collection;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.screens.Layout;
import hs.mediasystem.screens.Location;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.MediaNodeEvent;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    // - it is triggered initially because groupsets are set up (good, although it should not need to be a listener for that -- this is why it must be before groupset is bound)
    // - it is triggered after every groupset change because the View is too stupid to maintain selection itself (bad)
    view.groupSet.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        view.focusedMediaNode.set(null);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            String id = settingsStore.getSetting("MediaSystem:Collection", view.mediaRoot.get().getId().toString("LastSelected", view.mediaRoot.get().getRootName()));

            view.selectMediaNodeById(id);
          }
        });
      }
    });

    view.mediaRoot.bindBidirectional(presentation.mediaRoot);
    view.layout.bindBidirectional(presentation.layout);
    view.groupSet.bindBidirectional(presentation.groupSet);

    view.onSelect.set(new EventHandler<MediaNodeEvent>() {  // WORKAROUND METHOD_REFERENCE
      @Override
      public void handle(MediaNodeEvent event) {
        presentation.handleMediaNodeSelectEvent(event);
      }
    });

    view.onOptionsSelect.set(new EventHandler<ActionEvent>() {  // WORKAROUND METHOD_REFERENCE
      @Override
      public void handle(ActionEvent event) {
        presentation.handleOptionsSelectEvent(event);
      }
    });

    view.focusedMediaNode.addListener(new ChangeListener<MediaNode>() {
      @Override
      public void changed(ObservableValue<? extends MediaNode> observable, MediaNode old, MediaNode current) {
        if(current != null) {
          settingsStore.storeSetting("MediaSystem:Collection", PersistLevel.TEMPORARY, view.mediaRoot.get().getId().toString("LastSelected", view.mediaRoot.get().getRootName()), current.getId());
        }
      }
    });

    return view;
  }
}
