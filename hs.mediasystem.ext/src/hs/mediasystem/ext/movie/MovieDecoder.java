package hs.mediasystem.ext.movie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.fs.NameDecoder;
import hs.mediasystem.fs.NameDecoder.DecodeResult;
import hs.mediasystem.fs.NameDecoder.Hint;

import java.nio.file.Path;

public class MovieDecoder implements Decoder {
  private final NameDecoder nameDecoder = new NameDecoder(Hint.MOVIE);

  @Override
  public LocalInfo decode(Path path) {
    DecodeResult result = nameDecoder.decode(path.getFileName().toString());

    String title = result.getTitle();
    String sequence = result.getSequence();
    String subtitle = result.getSubtitle();
    Integer year = result.getReleaseYear();

    String imdb = result.getCode();
    String imdbNumber = null;

    if(imdb != null && !imdb.isEmpty()) {
      imdbNumber = String.format("tt%07d", Integer.parseInt(imdb));
    }

    Integer episode = sequence == null ? null : Integer.parseInt(sequence);

    return new LocalInfo(path.toString(), null, title, subtitle, imdbNumber, year, null, episode, episode);
  }
}
