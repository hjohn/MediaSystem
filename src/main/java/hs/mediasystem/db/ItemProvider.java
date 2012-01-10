package hs.mediasystem.db;

public interface ItemProvider {

  Item findItem(String type, String name, String subtitle, String year, String season, String episode);
  
}
