package hs.mediasystem.util;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class KeyCombinationGroup extends KeyCombination {
  private final KeyCombination[] combinations;

  public KeyCombinationGroup(KeyCombination... combinations) {
    this.combinations = combinations;
  }

  @Override
  public boolean match(KeyEvent event) {
    for(KeyCombination combination : combinations) {
      if(combination.match(event)) {
        return true;
      }
    }

    return false;
  }
}
