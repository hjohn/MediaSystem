package hs.mediasystem.framework;


import hs.mediasystem.fs.NamedItem;

import java.util.Collection;

public interface Group {
  Collection<? extends NamedItem> children();
}
