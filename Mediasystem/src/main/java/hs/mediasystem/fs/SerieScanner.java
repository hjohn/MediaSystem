package hs.mediasystem.fs;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.framework.Scanner;

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

public class SerieScanner implements Scanner<LocalInfo> {
  private static final Set<FileVisitOption> FOLLOW_LINKS = new HashSet<FileVisitOption>() {{
    add(FileVisitOption.FOLLOW_LINKS);
  }};

  @Override
  public List<LocalInfo> scan(final Path scanPath) {
    try {
      final List<LocalInfo> results = new ArrayList<>();

      Files.walkFileTree(scanPath, FOLLOW_LINKS, 2, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          if(dir.equals(scanPath)) {
            return FileVisitResult.CONTINUE;
          }

          if(!dir.getFileName().toString().startsWith(".")) {
            results.add(new LocalInfo(dir.toString(), null, dir.getFileName().toString(), null, null, null, null, null, null));
          }

          return FileVisitResult.SKIP_SUBTREE;
        }
      });

      return results;
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
