package hs.mediasystem.framework;


import java.util.Collection;

public interface Group {
  Collection<? extends MediaItem> children();
}
