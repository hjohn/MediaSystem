package hs.mediasystem.framework.descriptors;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class DescriptorSet {
  private final Set<DescriptorSet.Attribute> attributes;
  private final List<Descriptor> descriptors;

  public enum Attribute {SORTABLE, PREFERRED, CONCISE}

  public DescriptorSet(List<Descriptor> descriptors, Set<DescriptorSet.Attribute> attributes) {
    this.descriptors = ImmutableList.copyOf(descriptors);
    this.attributes = ImmutableSet.copyOf(attributes);
  }

  public Set<DescriptorSet.Attribute> getAttributes() {
    return attributes;
  }

  public List<Descriptor> getDescriptors() {
    return descriptors;
  }
}