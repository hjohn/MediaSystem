package hs.mediasystem.framework.actions;

import hs.mediasystem.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;

@Component
public class ActionTargetProvider {
  private static final Map<Class<?>, List<ExposedMember>> exposedPropertiesByClass = new HashMap<>();
  private static final Map<Member, ExposedMember> exposedPropertiesByMember = new HashMap<>();
  private static final Logger LOGGER = Logger.getLogger(ActionTargetProvider.class.getName());

  public List<ActionTarget> getActionTargets(Object presentation) {
    return createActionTargetsRecursively(presentation, Collections.emptyList());
  }

  private List<ActionTarget> createActionTargetsRecursively(Object root, List<Member> currentPath) {
    List<ActionTarget> actionTargets = new ArrayList<>();

    for(ExposedMember exposedMember : findExposedMembers(root.getClass())) {
      LOGGER.fine("Attempting property : " + exposedMember.getMember() + " ---> " + exposedMember.getMember().getType());

      if(Property.class.isAssignableFrom(exposedMember.getMember().getType())) {
        Object property = exposedMember.getMember().get(root);
        List<Member> childPath = new ArrayList<>(currentPath);

        childPath.add(exposedMember.getMember());

        actionTargets.add(new ActionTarget(exposedMember, Collections.unmodifiableList(childPath)));

        Property<?> p = (Property<?>)property;

        if(p.getValue() != null) {
          actionTargets.addAll(createActionTargetsRecursively(p.getValue(), Collections.unmodifiableList(childPath)));
        }
      }
      else if(exposedMember instanceof ExposedMethod) {
        actionTargets.add(new ActionTarget(exposedMember, Collections.unmodifiableList(currentPath)));
      }
      else {
        throw new IllegalStateException("Unhandled exposed member: " + exposedMember.getMember().getType());
      }
    }

    return actionTargets;
  }

  private static List<ExposedMember> findExposedMembers(Class<?> presentationClass) {
    if(!exposedPropertiesByClass.containsKey(presentationClass)) {
      cacheExposedProperties(presentationClass);
    }

    return exposedPropertiesByClass.get(presentationClass);
  }

  private static ExposedMember getExposedProperty(Member member) {
    ExposedMember exposedProperty = exposedPropertiesByMember.get(member);

    if(exposedProperty == null) {
      exposedProperty = createExposedProperty(member);
      exposedPropertiesByMember.put(member, exposedProperty);
    }

    return exposedProperty;
  }

  private static ExposedMember createExposedProperty(Member member) {
    Expose expose = member.getAnnotation(Expose.class);
    Class<?> cls = member.getDeclaringClass();
    Class<?> type = member.getType();

    try {
      if(type.isAssignableFrom(ObjectProperty.class)) {
        if(!expose.values().isEmpty()) {
          return new ExposedListBackedObjectProperty(member, cls.getDeclaredField(expose.values()));
        }
        else if(expose.valueBuilder() != Expose.NullValueBuilder.class) {
          @SuppressWarnings("unchecked")
          ValueBuilder<Object> valueBuilder = (ValueBuilder<Object>)expose.valueBuilder().newInstance();
          return new ExposedActionObjectProperty(member, valueBuilder);
        }
      }
      else if(type.isAssignableFrom(BooleanProperty.class)) {
        return new ExposedBooleanProperty(member);
      }
      else if(type.isAssignableFrom(IntegerProperty.class) || type.isAssignableFrom(LongProperty.class) || type.isAssignableFrom(FloatProperty.class) || type.isAssignableFrom(DoubleProperty.class)) {
        return new ExposedNumberProperty(member);
      }
      else if(type == void.class) {
        // Just a method that is exposed, create action for it
        return new ExposedMethod(member);
      }
    }
    catch(IllegalAccessException | InstantiationException | NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }

    return new DummyExposedProperty(member);
  }

  private static void cacheExposedProperties(Class<?> cls) {
    List<ExposedMember> exposedMembers = exposedPropertiesByClass.computeIfAbsent(cls, k -> new ArrayList<>());

    for(Field field : cls.getFields()) {
      Expose expose = field.getAnnotation(Expose.class);

      if(expose != null) {
        exposedMembers.add(getExposedProperty(new Member(field)));

        Class<?> type = field.getType();

        if(type.isAssignableFrom(ObjectProperty.class)) {
          Type firstGenericType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];

          cacheExposedProperties(firstGenericType instanceof ParameterizedType ? (Class<?>)((ParameterizedType)firstGenericType).getRawType() : (Class<?>)firstGenericType);
        }
      }
    }

    for(Method method : cls.getMethods()) {
      Expose expose = method.getAnnotation(Expose.class);

      if(expose != null) {
        exposedMembers.add(getExposedProperty(new Member(method)));
      }
    }
  }
}
