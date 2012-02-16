package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.Decoder;
import hs.mediasystem.fs.NameDecoder.DecodeResult;

import java.nio.file.Path;

public class MovieDecoder implements Decoder {

  @Override
  public LocalInfo decode(Path path, MediaType mediaType) {
    DecodeResult result = NameDecoder.decode(path.getFileName().toString());

    String title = result.getTitle();
    String sequence = result.getSequence();
    String subtitle = result.getSubtitle();
    Integer year = result.getReleaseYear();

    String imdb = result.getCode();
    String imdbNumber = null;

    if(imdb != null && !imdb.isEmpty()) {
      imdbNumber = String.format("tt%07d", Integer.parseInt(imdb));
    }

    Integer episode = sequence == null ? 1 : Integer.parseInt(sequence);

    return new LocalInfo(path.toString(), mediaType, null, title, subtitle, imdbNumber, year, null, episode);
  }
}
