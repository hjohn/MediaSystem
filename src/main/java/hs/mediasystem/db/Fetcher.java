package hs.mediasystem.db;

import java.util.List;

public interface Fetcher<P, C> {
  List<C> fetch(P parent);
}
