package hs.mediasystem;

import hs.models.events.EventListener;
import hs.models.events.ListenerList;

// TODO Refactor Listener system to take this new pattern into account
public abstract class Listener<E> implements EventListener<E> {
  private ListenerList<E> source;

  public void link(ListenerList<E> source) {
    if(this.source != null) {
      this.source.unregister(this);
    }
    
    this.source = source;
    this.source.call(this);
  }
}
