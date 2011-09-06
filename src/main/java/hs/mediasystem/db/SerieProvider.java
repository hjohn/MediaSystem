package hs.mediasystem.db;

public interface SerieProvider {
  SerieRecord getSerie(String name) throws ItemNotFoundException;
}
