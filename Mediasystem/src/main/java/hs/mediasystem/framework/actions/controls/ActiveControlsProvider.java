package hs.mediasystem.framework.actions.controls;

import hs.mediasystem.Component;
import hs.mediasystem.config.PresentationControlsConfiguration;
import hs.mediasystem.framework.actions.ActionTarget;
import hs.mediasystem.framework.actions.ActionTargetProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@Component
public class ActiveControlsProvider {
  private final ActionTargetProvider actionTargetProvider;

  @Inject
  public ActiveControlsProvider(ActionTargetProvider actionTargetProvider) {
    this.actionTargetProvider = actionTargetProvider;
  }

  public List<NodeFactory> getControlFactories(Object presentation) {
    List<NodeFactory> activeControlFactories = new ArrayList<>();
    Map<String, String> activeActions = PresentationControlsConfiguration.getActiveActionsByPresentationClass().getOrDefault(presentation.getClass(), Collections.emptyMap());
    List<ActionTarget> actionTargets = actionTargetProvider.getActionTargets(presentation);

    for(String propertyName : activeActions.keySet()) {
      for(ActionTarget actionTarget : actionTargets) {
        if(propertyName.equals(actionTarget.getMemberName())) {
          activeControlFactories.add(new NodeFactory(actionTarget, activeActions.get(propertyName)));
        }
      }
    }

    return activeControlFactories;
  }
}
