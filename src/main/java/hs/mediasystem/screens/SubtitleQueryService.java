package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleProvider;
import hs.sublight.SubtitleDescriptor;

import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class SubtitleQueryService extends Service<List<SubtitleDescriptor>> {
  private MediaItem mediaItem;
  private SubtitleProvider subtitleProvider;

  public void setMediaItem(MediaItem mediaItem) {
    this.mediaItem = mediaItem;
  }

  public void setSubtitleProvider(SubtitleProvider subtitleProvider) {
    this.subtitleProvider = subtitleProvider;
  }

  @Override
  protected Task<List<SubtitleDescriptor>> createTask() {
    final MediaItem mediaItem = this.mediaItem;
    final SubtitleProvider provider = subtitleProvider;

    return new Task<List<SubtitleDescriptor>>() {
      @Override
      protected List<SubtitleDescriptor> call() throws Exception {
        return provider.query(mediaItem);
      }
    };
  }
}