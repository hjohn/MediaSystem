package hs.mediasystem.framework;

import javax.inject.Singleton;

import hs.mediasystem.entity.EntitySource;

@Singleton
public class DatabaseEntitySource extends EntitySource {

  public DatabaseEntitySource() {
    super("DB", 5.0, Integer.class);
  }
}
