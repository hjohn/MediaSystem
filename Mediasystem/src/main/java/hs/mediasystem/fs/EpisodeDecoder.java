package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.fs.NameDecoder.DecodeResult;

import java.nio.file.Path;

public class EpisodeDecoder implements Decoder {
  private final String serieName;

  public EpisodeDecoder(String serieName) {
    assert serieName != null;

    this.serieName = serieName;
  }

  @Override
  public LocalInfo decode(Path path) {
    DecodeResult result = NameDecoder.decode(path.getFileName().toString());

    String sequence = result.getSequence();
    String title = result.getSubtitle();
    Integer year = result.getReleaseYear();

    Integer season = null;
    Integer episode = null;
    Integer endEpisode = null;

    if(sequence != null) {
      String[] split = sequence.split("[-,]");

      season = Integer.parseInt(split[0]);
      episode = Integer.parseInt(split[1]);
      endEpisode = split.length > 2 ? Integer.parseInt(split[2]) : episode;
    }

    return new LocalInfo(path.toString(), serieName, title, null, null, year, season, episode, endEpisode);
  }
}
