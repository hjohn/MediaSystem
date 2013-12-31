package hs.mediasystem.framework;

import hs.mediasystem.entity.EntitySource;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SourceMatcher {
  private final Set<EntitySource> allSources;

  @Inject
  public SourceMatcher(Set<EntitySource> allSources) {
    this.allSources = allSources;
  }

  public EntitySource sourceFromString(String sourceName) {
    for(EntitySource source : allSources) {
      if(source.getName().equals(sourceName)) {
        return source;
      }
    }

    return null;
  }
}
