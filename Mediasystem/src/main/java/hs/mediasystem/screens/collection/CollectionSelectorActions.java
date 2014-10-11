package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.actions.PresentationActionEvent;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.util.javafx.Dialogs;
import javafx.event.EventHandler;

public enum CollectionSelectorActions implements EventHandler<PresentationActionEvent<CollectionSelectorPresentation>> {
  VIEWED_TOGGLE {
    @Override
    public void handle(PresentationActionEvent<CollectionSelectorPresentation> event) {
      CollectionSelectorPresentation presentation = event.getPresentation();

      MediaNode mediaNode = presentation.focusedMediaNode.get();
      MediaData mediaData = mediaNode.media.get().getMediaItem().mediaData.get();

      mediaData.viewed.set(!mediaData.viewed.get());
    }
  },

  INFORMATION_SHOW_DIALOG {
    @Override
    public void handle(PresentationActionEvent<CollectionSelectorPresentation> event) {
      Dialogs.show(event, event.getPresentation().createInformationDialog());
    }
  }
}
