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
  }),
  FILTER_SHOW_DIALOG(event -> {
    CollectionPresentation presentation = event.getPresentation();
    FilterPane filterPane = new FilterPane();

    presentation.inclusionFilter.set(Dialogs.showAndWait(event, new DialogPane<CollectionPresentation.InclusionFilter>() {
      {
        CollectionPresentation.InclusionFilter inclusionFilter = presentation.inclusionFilter.get();

        getChildren().add(filterPane);

        if(inclusionFilter != null) {
          Integer afterYear = inclusionFilter.getAfterYear();
          Integer beforeYear = inclusionFilter.getBeforeYear();

          if(afterYear == null && beforeYear == null) {
            filterPane.releaseFilterMode.set(FilterPane.ReleaseFilterMode.ANYTIME);
          }
          else if(afterYear != null && beforeYear == null) {
            filterPane.releaseFilterMode.set(FilterPane.ReleaseFilterMode.AFTER);
          }
          else if(afterYear == null && beforeYear != null) {
            filterPane.releaseFilterMode.set(FilterPane.ReleaseFilterMode.BEFORE);
            afterYear = beforeYear;
            beforeYear = null;
          }
          else if(afterYear != null && beforeYear != null && beforeYear - afterYear == 2) {
            filterPane.releaseFilterMode.set(FilterPane.ReleaseFilterMode.IN);
            afterYear = afterYear + 1;
            beforeYear = null;
          }
          else {
            filterPane.releaseFilterMode.set(FilterPane.ReleaseFilterMode.BETWEEN);
          }

          filterPane.includeViewed.set(inclusionFilter.isIncludeViewed());
          filterPane.includeNotViewed.set(inclusionFilter.isIncludeNotViewed());
          filterPane.year1.set(afterYear);
          filterPane.year2.set(beforeYear);
          filterPane.matchGenres.clear();
          filterPane.matchGenres.addAll(inclusionFilter.getGenres());
        }
      }

      @Override
      protected CollectionPresentation.InclusionFilter getResult() {
        Integer year1 = filterPane.year1.get();
        Integer year2 = filterPane.year2.get();
        FilterPane.ReleaseFilterMode mode = filterPane.releaseFilterMode.get();

        if(mode == FilterPane.ReleaseFilterMode.IN && year1 != null) {
          year1--;
          year2 = year1 + 2;
        }
        else if(mode == FilterPane.ReleaseFilterMode.AFTER) {
          year2 = null;
        }
        else if(mode == FilterPane.ReleaseFilterMode.BEFORE) {
          year2 = year1;
          year1 = null;
        }
        else if(mode == FilterPane.ReleaseFilterMode.ANYTIME) {
          year1 = null;
          year2 = null;
        }
        else if(year1 != null && year2 != null && year2 < year1) {
          int year = year2;

          year2 = year1;
          year1 = year;
        }

        return new CollectionPresentation.InclusionFilter(year1, year2, filterPane.includeViewed.get(), filterPane.includeNotViewed.get(), filterPane.matchGenres);
      }
    }));
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
