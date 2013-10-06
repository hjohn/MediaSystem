package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.actions.Action;
import hs.mediasystem.framework.actions.PropertyDescriptor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

@Named
public class GroupSetPropertyDescriptor implements PropertyDescriptor<CollectionPresentation> {
  private static final List<Action<CollectionPresentation>> ACTIONS = new ArrayList<>();

  static {
    ACTIONS.add(new Action<CollectionPresentation>() {
      @Override
      public String getId() {
        return "groupSet.decrease";
      }

      @Override
      public String getDescription() {
        return "Previous Grouping";
      }

      @Override
      public void perform(CollectionPresentation presentation) {
        int index = presentation.availableGroupSets.indexOf(presentation.groupSet.get()) - 1;

        if(index < 0) {
          index = presentation.availableGroupSets.size() - 1;
        }

        presentation.groupSet.set(presentation.availableGroupSets.get(index));
      }
    });

    ACTIONS.add(new Action<CollectionPresentation>() {
      @Override
      public String getId() {
        return "groupSet.increase";
      }

      @Override
      public String getDescription() {
        return "Next Grouping";
      }

      @Override
      public void perform(CollectionPresentation presentation) {
        int index = presentation.availableGroupSets.indexOf(presentation.groupSet.get()) + 1;

        if(index >= presentation.availableGroupSets.size()) {
          index = 0;
        }

        presentation.groupSet.set(presentation.availableGroupSets.get(index));
      }
    });
  }

  /**
   * The presentation this action acts on.
   *
   * @return the presentation this action acts on
   */
  @Override
  public Class<CollectionPresentation> getPresentationClass() {
    return null;
  }

  @Override
  public List<Action<CollectionPresentation>> getActions() {
    return ACTIONS;
  }

  /*

    Settings:
    playback KEY_PLUS volume increase
    playback KEY_MINUS volume decrease

    KEY_PLUS -> "playback.volume.increase"
    KEY_MINUS -> "playback.volume.decrease"


   increase -> +5
   decrease -> -5


  new ActionFactory("increase", "Next Presentation") {
    public Action create(P presentation) {
      return new Action() {
        presentation.groupSet++;
      };
    }
  }

   *
   */


}
