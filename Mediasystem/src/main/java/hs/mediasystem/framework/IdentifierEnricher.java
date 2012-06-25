package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

public class IdentifierEnricher implements Enricher<Identifier> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(TaskTitle.class);
    add(MediaData.class);
    add(Media.class);
  }};

  private final IdentifierDao identifierDao;
  private final TypeBasedItemEnricher typeBasedItemEnricher;

  @Inject
  public IdentifierEnricher(IdentifierDao identifierDao, TypeBasedItemEnricher typeBasedItemEnricher) {
    this.identifierDao = identifierDao;
    this.typeBasedItemEnricher = typeBasedItemEnricher;
  }

  @Override
  public List<Class<?>> getInputTypes() {
    return INPUT_PARAMETERS;
  }

  @Override
  public List<EnrichTask<Identifier>> enrich(Parameters parameters, boolean bypassCache) {
    List<EnrichTask<Identifier>> enrichTasks = new ArrayList<>();

    if(!bypassCache) {
      enrichTasks.add(createCachedTask(parameters.unwrap(TaskTitle.class), parameters.get(MediaData.class)));
    }
    enrichTasks.add(createTask(parameters.unwrap(TaskTitle.class), parameters.get(MediaData.class), parameters.get(Media.class)));

    return enrichTasks;
  }

  public EnrichTask<Identifier> createCachedTask(final String title, final MediaData mediaData) {
    return new EnrichTask<Identifier>(true) {
      {
        updateTitle("Cache:" + title);
      }

      @Override
      public Identifier call() throws IOException {
        Identifier identifier = identifierDao.getIdentifierByMediaDataId(mediaData.getId());

        /*
         * It's possible this Identifier is an empty placeholder to prevent attempts at identifying a Media every time it
         * is accessed.  This is negative caching a failed identification.  However, if this was more than a week ago the
         * identification should be re-attempted.  To trigger this, null is returned.
         */

        if(identifier != null && identifier.getMediaType() == null && identifier.getLastUpdated().getTime() < System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000) {
          return null;
        }

        return identifier;
      }
    };
  }

  public EnrichTask<Identifier> createTask(final String title, final MediaData mediaData, final Media media) {
    return new EnrichTask<Identifier>(false) {
      {
        updateTitle(title);
        updateProgress(0, 2);
      }

      @Override
      protected Identifier call() throws Exception {
        try {
          Identifier identifier = null;

          try {
            identifier = typeBasedItemEnricher.identifyItem(media);
          }
          catch(IdentifyException e) {
            identifier = new Identifier();
          }

          updateProgress(1, 2);

          identifier.setMediaDataId(mediaData.getId());
          identifier.setLastUpdated(new Date());

          Identifier existingIdentifier = identifierDao.getIdentifierByMediaDataId(mediaData.getId());

          if(existingIdentifier != null) {
            identifier.setId(existingIdentifier.getId());
          }

          identifierDao.storeIdentifier(identifier);

          return identifier.getMediaType() == null ? null : identifier;
        }
        finally {
          updateProgress(2, 2);
        }
      }
    };
  }
}