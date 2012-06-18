package hs.subtitle.sublight;

import hs.subtitle.SubtitleDescriptor;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sublight.webservice.Subtitle;

public class SublightSubtitleDescriptor implements SubtitleDescriptor {
  private final Subtitle subtitle;
  private final SublightSubtitleClient source;

  private final String name;
  private final String languageName;

  public SublightSubtitleDescriptor(Subtitle subtitle, SublightSubtitleClient source) {
    this.subtitle = subtitle;
    this.source = source;

    this.name = getName(subtitle);
    this.languageName = SublightSubtitleClient.getLanguageName(subtitle.getLanguage());
  }

  private static String getName(Subtitle subtitle) {
    String releaseName = subtitle.getRelease();

    // check if release name contains sufficient information to be used as display name
    if(releaseName != null && !releaseName.isEmpty()) {
      boolean isValid = true;

      if(subtitle.getSeason() != null) {
        isValid &= releaseName.contains(subtitle.getSeason().toString());
      }

      if(subtitle.getEpisode() != null) {
        isValid &= releaseName.contains(subtitle.getEpisode().toString());
      }

      if(isValid) {
        return releaseName;
      }
    }

    // format proper display name
    StringBuilder builder = new StringBuilder(subtitle.getTitle());

    if(subtitle.getSeason() != null || subtitle.getEpisode() != null) {
      builder.append(String.format(" - S%02dE%02d", subtitle.getSeason(), subtitle.getEpisode()));
    }

    if(subtitle.getRelease() != null && !subtitle.getRelease().isEmpty()) {
      builder.append(String.format(" (%s)", subtitle.getRelease()));
    }

    return builder.toString();
  }


  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLanguageName() {
    return languageName;
  }

  @Override
  public String getType() {
    return subtitle.getSubtitleType().value().toLowerCase();
  }

  @Override
  public byte[] getSubtitleRawData() throws IOException {
    byte[] archive = source.getZipArchive(subtitle);

    // the zip archive will contain exactly one subtitle
    try(ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(archive))) {
      // move to subtitle entry
      ZipEntry entry = stream.getNextEntry();

      try(DataInputStream dis = new DataInputStream(stream)) {
        byte[] data = new byte[(int)entry.getSize()];

        dis.readFully(data);

        return data;
      }
    }
  }

  @Override
  public String toString() {
    return String.format("%s [%s]", getName(), getLanguageName());
  }
}
