package hs.mediasystem;

import hs.mediasystem.screens.movie.State;

/**
 * Represents a specific screen and its state, a page visit.
 */
public class View {
  private final Screen screen;
  
  private State state;

  public View(Screen screen, State state) {
    this.screen = screen;
    this.state = state;
  }
  
  public Screen getScreen() {
    return screen;
  }
  
  public State getState() {
    return state;
  }

  public void updateState() {
    state = screen.getState();
  }
}
