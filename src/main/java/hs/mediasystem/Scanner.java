package hs.mediasystem;

import java.nio.file.Path;
import java.util.List;

public interface Scanner {
  Serie scan(Path path);
}
