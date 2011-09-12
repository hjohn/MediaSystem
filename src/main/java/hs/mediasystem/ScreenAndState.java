package hs.mediasystem;

import hs.mediasystem.screens.movie.State;

public class ScreenAndState {
  private final Screen screen;
  private State state;

  public ScreenAndState(Screen screen, State state) {
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
