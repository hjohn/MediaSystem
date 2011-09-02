package hs.mediasystem;

import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.db.SerieProvider;
import hs.mediasystem.db.SerieRecord;
import hs.mediasystem.screens.movie.Element;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

public class Serie extends NamedItem {
  private final List<Episode> episodes = new ArrayList<Episode>();
  private final Path path;
  private final SerieProvider serieProvider;
  
  public Serie(Path path, String name, SerieProvider serieProvider) {
    super(name);
    this.path = path;
    this.serieProvider = serieProvider;
  }
  
  public BufferedImage getBanner() {
    ensureDataLoaded();
    
    try {
      if(record.getBanner() != null) {
        return ImageIO.read(new ByteArrayInputStream(record.getBanner()));
      }
    }
    catch(IOException e) {
      // Ignore
    }
    
    return new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
  }
  
  public Episode addEpisode(Element element, ItemProvider itemProvider) {
    Episode episode = new Episode(element, itemProvider);
    
    episodes.add(episode);
    
    return episode;
  }

  public void addAll(Collection<Episode> episodes) {
    episodes.addAll(episodes);
  }
  
  public Collection<Episode> episodes() {
    return Collections.unmodifiableList(episodes);
  }
  
  private SerieRecord record;
  
  private void ensureDataLoaded() {
    if(record == null) {
      try {
        if(serieProvider != null) {
          record = serieProvider.getSerie(getTitle());
        }
        else {
          record = new SerieRecord();
        }
      }
      catch(ItemNotFoundException e) {
        record = new SerieRecord();
      }
    }
  }
}
