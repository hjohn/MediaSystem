package hs.mediasystem;

import java.util.Collection;

public interface Group {
  Collection<? extends NamedItem> children();
}
