package hs.mediasystem.ext.media.serie;

import hs.mediasystem.util.io.RuntimeIOException;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SerieScanner {
  private static final Set<FileVisitOption> FOLLOW_LINKS = new HashSet<FileVisitOption>() {{
    add(FileVisitOption.FOLLOW_LINKS);
  }};

  public List<Path> scan(final Path scanPath) {
    try {
      final List<Path> results = new ArrayList<>();

      Files.walkFileTree(scanPath, FOLLOW_LINKS, 2, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if(dir.equals(scanPath)) {
            return FileVisitResult.CONTINUE;
          }

          if(!dir.getFileName().toString().startsWith(".")) {
            results.add(dir);
          }

          return FileVisitResult.SKIP_SUBTREE;
        }
      });

      return results;
    }
    catch(IOException e) {
      throw new RuntimeIOException("Exception while scanning \"" + scanPath + "\"", e);
    }
  }
}
