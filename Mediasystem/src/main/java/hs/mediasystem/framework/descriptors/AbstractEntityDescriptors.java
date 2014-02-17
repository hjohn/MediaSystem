package hs.mediasystem.framework.descriptors;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

public abstract class AbstractEntityDescriptors implements EntityDescriptors {
  private Set<Descriptor> descriptors = ImmutableSet.of();
  private Map<String, Descriptor> descriptorsByName = ImmutableMap.of();
  private Set<DescriptorSet> sets = ImmutableSet.of();

  @Override
  public final Descriptor getDescriptor(String name) {
    return descriptorsByName.get(name);
  }

  @Override
  public Set<Descriptor> getDescriptors() {
    return descriptors;
  }

  @Override
  public final Set<DescriptorSet> getDescriptorSets() {
    return sets;
  }

  protected final void initializeDescriptors(Set<Descriptor> descriptors) {
    Builder<String, Descriptor> builder = ImmutableMap.builder();

    for(Descriptor descriptor : descriptors) {
      builder.put(descriptor.getName(), descriptor);
    }

    this.descriptors = ImmutableSet.copyOf(descriptors);
    this.descriptorsByName = builder.build();
  }

  protected final void initializeDescriptors(Descriptor... descriptors) {
    Builder<String, Descriptor> builder = ImmutableMap.builder();

    for(Descriptor descriptor : descriptors) {
      builder.put(descriptor.getName(), descriptor);
    }

    this.descriptors = ImmutableSet.copyOf(descriptors);
    this.descriptorsByName = builder.build();
  }

  protected final void initializeSets(Set<DescriptorSet> sets) {
    this.sets = ImmutableSet.copyOf(sets);
  }

  protected final void initializeSets(DescriptorSet... sets) {
    this.sets = ImmutableSet.copyOf(sets);
  }
}
