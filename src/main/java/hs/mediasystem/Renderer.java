package hs.mediasystem;

import javax.swing.JComponent;

public interface Renderer<T> {
  JComponent render(T item, boolean hasFocus);
}
