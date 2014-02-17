package hs.mediasystem.framework.descriptors;

import java.util.Set;

public interface EntityDescriptors {

  Set<DescriptorSet> getDescriptorSets();

  Descriptor getDescriptor(String name);

  Set<Descriptor> getDescriptors();

  /*
   * The top level types distinguish between types that cannot be converted to each other and back again without losing information.
   *
   * Date: Can be converted to Text and back, so its a TextType
   * Image: Cannot be converted to Text and back, so its its own type
   */

  public class TextType {
    private final SubType type;
    private final Size size;

    public enum SubType { STRING, DATE, TIMESTAMP, NUMERIC }
    public enum Size { WORD, TITLE, SENTENCE, PARAGRAPH, SUMMARY }

    public TextType(SubType type, Size size) {
      this.type = type;
      this.size = size;
    }

    public SubType getType() {
      return type;
    }

    public Size getSize() {
      return size;
    }
  }

  public class ObjectType {
  }

  public class ImageType {
  }
}
