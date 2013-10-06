package hs.mediasystem.framework.actions;

import java.util.List;

public interface PropertyDescriptor<P> {
  Class<P> getPresentationClass();
  List<Action<P>> getActions();
}
