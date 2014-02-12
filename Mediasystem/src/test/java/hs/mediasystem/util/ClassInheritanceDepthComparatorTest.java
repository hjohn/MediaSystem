package hs.mediasystem.util;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

public class ClassInheritanceDepthComparatorTest {

  @Test
  public void shouldSortInInheritanceDepthOrder() {
    Class<?>[] input = new Class[] {Chimp.class, Monkey.class, Object.class, Monkey.class};

    Arrays.sort(input, ClassInheritanceDepthComparator.INSTANCE);

    assertArrayEquals(
      new Object[] {Object.class, Monkey.class, Monkey.class, Chimp.class},
      input
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenClassesAreNotInSameInheritanceTree() {
    Arrays.sort(new Class<?>[] {Chimp.class, Cat.class}, ClassInheritanceDepthComparator.INSTANCE);
  }

  public static class Animal {
  }

  public static class Monkey extends Animal {
  }

  public static class Chimp extends Monkey {
  }

  public static class Cat extends Animal {
  }
}
