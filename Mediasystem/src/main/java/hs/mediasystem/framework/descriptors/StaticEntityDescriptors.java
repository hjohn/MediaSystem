package hs.mediasystem.framework.descriptors;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StaticEntityDescriptors extends AbstractEntityDescriptors {

  public StaticEntityDescriptors(Class<?> cls) {
    Set<Descriptor> mediaProperties = getFieldValues(cls, Descriptor.class);
    Set<DescriptorSet> sets = getFieldValues(cls, DescriptorSet.class);

    initializeDescriptors(mediaProperties);
    initializeSets(sets);
  }

  public static <T> Set<T> getFieldValues(Class<?> cls, Class<T> fieldType) {
    Set<T> values = new HashSet<>();

    for(Field field : cls.getFields()) {
      int modifiers = field.getModifiers();

      if(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers)) {
        if(field.getType() == fieldType) {
          try {
            field.setAccessible(true);

            @SuppressWarnings("unchecked")
            T t = (T)field.get(null);

            values.add(t);
          }
          catch(IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("exception while creating Descriptor", e);
          }
        }
      }
    }

    return values;
  }
}