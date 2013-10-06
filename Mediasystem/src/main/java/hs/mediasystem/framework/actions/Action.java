package hs.mediasystem.framework.actions;

public interface Action<P> {
  String getId();
  String getDescription();
  void perform(P presentation);
}
