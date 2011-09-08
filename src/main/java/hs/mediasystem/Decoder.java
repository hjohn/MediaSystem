package hs.mediasystem;

import hs.mediasystem.screens.movie.Element;

import java.nio.file.Path;

public interface Decoder {
  Element decode(Path path);
}
