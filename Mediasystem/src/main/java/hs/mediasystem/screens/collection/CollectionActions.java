package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.actions.PresentationActionEvent;
import hs.mediasystem.util.DialogPane;
import hs.mediasystem.util.javafx.Dialogs;
import javafx.event.EventHandler;

import javax.inject.Named;

@Named
public enum CollectionActions implements EventHandler<PresentationActionEvent<CollectionPresentation>> {
  GROUP_SET_NEXT(event -> {
    CollectionPresentation presentation = event.getPresentation();
    int index = presentation.availableGroupSets.indexOf(presentation.groupSet.get()) + 1;

    if(index >= presentation.availableGroupSets.size()) {
      index = 0;
    }

    presentation.groupSet.set(presentation.availableGroupSets.get(index));
  }),
  GROUP_SET_PREVIOUS(event -> {
    CollectionPresentation presentation = event.getPresentation();
    int index = presentation.availableGroupSets.indexOf(presentation.groupSet.get()) - 1;

    if(index < 0) {
      index = presentation.availableGroupSets.size() - 1;
    }

    presentation.groupSet.set(presentation.availableGroupSets.get(index));
  });

  private final EventHandler<PresentationActionEvent<CollectionPresentation>> eventHandler;

  CollectionActions(EventHandler<PresentationActionEvent<CollectionPresentation>> eventHandler) {
    this.eventHandler = eventHandler;
  }

  @Override
  public void handle(PresentationActionEvent<CollectionPresentation> event) {
    eventHandler.handle(event);
  }
}
