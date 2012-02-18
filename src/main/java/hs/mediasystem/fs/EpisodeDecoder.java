package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.fs.NameDecoder.DecodeResult;

import java.nio.file.Path;

public class EpisodeDecoder implements Decoder {

  @Override
  public LocalInfo<?> decode(Path path, String mediaType) {
    DecodeResult result = NameDecoder.decode(path.getFileName().toString());

    String serieName = result.getTitle();
    String sequence = result.getSequence();
    String title = result.getSubtitle();
    Integer year = result.getReleaseYear();

    Integer season = null;
    Integer episode = null;

    if(sequence != null) {
      String[] split = sequence.split(",");

      season = Integer.parseInt(split[0]);
      episode = Integer.parseInt(split[1]);
    }

    return new LocalInfo<>(path.toString(), mediaType, serieName, title, null, null, year, season, episode, null);
  }
}
