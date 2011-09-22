package hs.mediasystem.framework;

import javax.swing.JComponent;

public interface Renderer<T> {
  JComponent render(T item, boolean hasFocus);
  MediaItem getPrototypeCellValue();
}
