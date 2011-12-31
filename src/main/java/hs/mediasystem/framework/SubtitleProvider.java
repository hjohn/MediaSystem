package hs.mediasystem.framework;

import hs.sublight.SubtitleDescriptor;

import java.util.List;

public interface SubtitleProvider {
  String getName();
  List<SubtitleDescriptor> query(MediaItem mediaItem) throws SubtitleProviderException;
}
