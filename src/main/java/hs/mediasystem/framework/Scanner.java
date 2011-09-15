package hs.mediasystem.framework;

import java.nio.file.Path;
import java.util.List;

public interface Scanner<T> {
  List<T> scan(Path path);
}
