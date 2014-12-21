package hs.mediasystem.framework.actions;

import hs.mediasystem.config.Resources;

import java.util.List;

import javafx.beans.property.Property;
import javafx.event.Event;

/**
 * A (static) target for potential actions.
 */
public class ActionTarget {
  private final ExposedMember exposedMember;
  private final String memberName;
  private final List<Member> path;

  public ActionTarget(ExposedMember exposedMember, List<Member> path) {
    if(exposedMember == null) {
      throw new IllegalArgumentException("exposedMember cannot be null");
    }

    this.exposedMember = exposedMember;
    this.memberName = exposedMember.getMember().getDeclaringClass().getName() + "." + exposedMember.getMember().getName();
    this.path = path;
  }

  public ExposedMember getExposedMember() {
    return exposedMember;
  }

  public List<Member> getPath() {
    return path;
  }

  public String getMemberName() {
    return memberName;
  }

  public String getLabel(String controlType) {
    return Resources.getResource(memberName, controlType + ".label");
  }

  /**
   * Triggers the given action.
   *
   * @param action the action to trigger
   * @param root the root object this actionTarget is nested under via its path
   * @param event an {@link Event}
   */
  public void doAction(String action, Object root, Event event) {
    Object parent = findDirectParentFromRoot(root);

    System.out.println("[INFO] Doing '" + action + "' for '" + memberName + "' of " + parent);

    exposedMember.doAction(action, parent, event);
  }

  @SuppressWarnings("unchecked")
  public <T> Property<T> getProperty(Object parent) {
    return (Property<T>)exposedMember.getMember().get(parent);
  }

  public Object findDirectParentFromRoot(Object root) {
    Object parent = root;
    Object property = null;

    for(Member pathMember : path) {
      @SuppressWarnings("unchecked")
      Object propertyValue = property == null ? root : ((Property<Object>)property).getValue();

      parent = propertyValue;
      property = pathMember.get(propertyValue);
    }

    return parent;
  }
}