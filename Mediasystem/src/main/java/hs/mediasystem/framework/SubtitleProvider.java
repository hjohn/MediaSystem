package hs.mediasystem.framework;

import hs.subtitle.SubtitleDescriptor;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SubtitleProvider {
  String getName();
  List<? extends SubtitleDescriptor> query(Map<String, Object> criteria) throws SubtitleProviderException;
  Set<String> getMediaTypes();
}
