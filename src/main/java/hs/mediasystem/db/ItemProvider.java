package hs.mediasystem.db;

public interface ItemProvider {
  public Item getItem(String fileName, String title, String year, String imdbNumber) throws ItemNotFoundException;
}
