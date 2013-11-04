package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.util.DialogPane;

import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.inject.Named;

@Named
public enum CollectionActions implements Action<CollectionPresentation> {
  GROUP_SET_NEXT(presentation -> {
    int index = presentation.availableGroupSets.indexOf(presentation.groupSet.get()) + 1;

    if(index >= presentation.availableGroupSets.size()) {
      index = 0;
    }

    presentation.groupSet.set(presentation.availableGroupSets.get(index));
  }),
  GROUP_SET_PREVIOUS(presentation -> {
    int index = presentation.availableGroupSets.indexOf(presentation.groupSet.get()) - 1;

    if(index < 0) {
      index = presentation.availableGroupSets.size() - 1;
    }

    presentation.groupSet.set(presentation.availableGroupSets.get(index));
  });

  private final Consumer<CollectionPresentation> action;

  CollectionActions(Consumer<CollectionPresentation> action) {
    this.action = action;
  }

  @Override
  public void perform(CollectionPresentation presentation) {
    action.accept(presentation);
  }
}
