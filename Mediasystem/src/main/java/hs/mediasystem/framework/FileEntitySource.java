package hs.mediasystem.framework;

import javax.inject.Singleton;

import hs.mediasystem.entity.EntitySource;

@Singleton
public class FileEntitySource extends EntitySource {

  public FileEntitySource() {
    super("FILE", 0.0, String.class);
  }
}
