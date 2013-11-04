package hs.mediasystem.framework.actions;

public interface Action<P> {
  void perform(P presentation);
}
