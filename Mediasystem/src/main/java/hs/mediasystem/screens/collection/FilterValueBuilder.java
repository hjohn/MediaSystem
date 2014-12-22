package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.actions.ValueBuilder;
import hs.mediasystem.util.DialogPane;
import hs.mediasystem.util.javafx.Dialogs;
import javafx.event.Event;

public class FilterValueBuilder implements ValueBuilder<CollectionPresentation.InclusionFilter> {

  @Override
  public CollectionPresentation.InclusionFilter build(Event event, CollectionPresentation.InclusionFilter currentValue) {
    FilterPane filterPane = new FilterPane();

    return Dialogs.showAndWait(event, new DialogPane<CollectionPresentation.InclusionFilter>() {
      {
        CollectionPresentation.InclusionFilter inclusionFilter = currentValue;

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
    });
  }
}
