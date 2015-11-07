package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.descriptors.AbstractEntityDescriptors;
import hs.mediasystem.framework.descriptors.Descriptor;
import hs.mediasystem.framework.descriptors.DescriptorSet;
import hs.mediasystem.framework.descriptors.DescriptorSet.Attribute;
import hs.mediasystem.framework.descriptors.EntityDescriptors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class Movie extends Media {
  private static final EntityDescriptors DESCRIPTORS = new AbstractEntityDescriptors() {{
    initializeDescriptors(
      new Descriptor("title", 0.99999, new TextType(TextType.SubType.STRING, TextType.Size.TITLE)),
      new Descriptor("releaseDate", 0.9999, new TextType(TextType.SubType.DATE, TextType.Size.WORD)),
      new Descriptor("releaseYear", 0.98, new TextType(TextType.SubType.NUMERIC, TextType.Size.WORD))
    );

    initializeSets(
      new DescriptorSet(
        ImmutableList.of(getDescriptor("title"), getDescriptor("releaseDate")),
        ImmutableSet.of(Attribute.SORTABLE)
      ),
      new DescriptorSet(
        ImmutableList.of(getDescriptor("title"), getDescriptor("releaseYear")),
        ImmutableSet.of(Attribute.SORTABLE, Attribute.PREFERRED, Attribute.CONCISE)
      )
    );
  }};

  public final ObjectProperty<Integer> sequence = new SimpleObjectProperty<>();
  public final StringProperty language = stringProperty("language");
  public final StringProperty tagLine = stringProperty("tagLine");
  public final StringProperty groupTitle = stringProperty("groupTitle");

  public Movie(MediaItem mediaItem) {
    super(DESCRIPTORS, mediaItem);
  }

  public Movie() {
    this(null);
  }
}
