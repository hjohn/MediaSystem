package hs.mediasystem.framework;

import hs.subtitle.SubtitleDescriptor;

import java.util.List;

public interface SubtitleProvider {
  String getName();
  List<? extends SubtitleDescriptor> query(MediaItem mediaItem) throws SubtitleProviderException;
}
