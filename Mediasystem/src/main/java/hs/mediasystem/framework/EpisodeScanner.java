package hs.mediasystem.framework;

import hs.mediasystem.dao.LocalInfo;

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
import java.util.regex.Pattern;

public class EpisodeScanner implements Scanner<LocalInfo> {
  private static final Pattern EXTENSION_PATTERN = Pattern.compile("(?i).+\\.(avi|flv|mkv|mov|mp4|mpg|mpeg)");
  private static final Set<FileVisitOption> FOLLOW_LINKS = new HashSet<FileVisitOption>() {{
    add(FileVisitOption.FOLLOW_LINKS);
  }};

  private final Decoder decoder;
  private final int maxDepth;

  public EpisodeScanner(Decoder decoder, int maxDepth) {
    this.decoder = decoder;
    this.maxDepth = maxDepth;
  }

  @Override
  public List<LocalInfo> scan(Path rootPath) {
    try {
      final List<LocalInfo> results = new ArrayList<>();

      Files.walkFileTree(rootPath, FOLLOW_LINKS, maxDepth, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if(!attrs.isDirectory() && file.getFileName().toString().matches(EXTENSION_PATTERN.pattern())) {
            LocalInfo localInfo = decoder.decode(file);

            if(localInfo != null) {
              results.add(localInfo);
            }
            else {
              System.err.println("EpisodeScanner: Could not decode as episode: " + file);
            }
          }
          return FileVisitResult.CONTINUE;
        }
      });

      return results;
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
