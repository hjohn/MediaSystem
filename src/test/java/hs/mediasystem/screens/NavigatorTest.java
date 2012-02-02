package hs.mediasystem.screens;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import hs.mediasystem.screens.Navigator.Destination;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

public class NavigatorTest {
  private Navigator navigator;
  private Deque<String> trip;

  @Before
  public void before() {
    navigator = new Navigator();

    trip = new LinkedList<>();
  }

  @Test
  public void shouldNavigateToDestination() {
    Destination a = new TestDestination(trip, "A");

    navigator.navigateTo(a);

    assertEquals("A/in", trip.pollFirst());
    assertEquals("A", trip.pollFirst());
    assertTrue(trip.isEmpty());
  }

  @Test
  public void shouldIgnoreBackAtTopLevel() {
    Destination a = new TestDestination(trip, "A");

    navigator.navigateTo(a);
    trip.clear();

    navigator.back();

    assertTrue(trip.isEmpty());
  }

  @Test
  public void shouldGoBackToPrevious() {
    Destination a = new TestDestination(trip, "A");
    Destination b = new TestDestination(trip, "B");

    navigator.navigateTo(a);
    navigator.navigateTo(b);
    trip.clear();

    navigator.back();

    assertEquals("B/out", trip.pollFirst());
    assertEquals("A/in", trip.pollFirst());
    assertEquals("A", trip.pollFirst());
    assertTrue(trip.isEmpty());
  }

  @Test
  public void shouldHandleNestedNavigations() {
    final Destination ba = new TestDestination(trip, "B-A");
    Destination bb = new TestDestination(trip, "B-B");
    Destination bm = new TestDestination(trip, "B-M");
    Destination play = new TestDestination(trip, "play");

    Destination a = new TestDestination(trip, "A");
    Destination b = new TestDestination(trip, "B") {
      @Override
      public void intro() {
        super.intro();
        navigator.navigateTo(ba);
      }
    };

    navigator.navigateTo(a);

    assertEquals("A/in", trip.pollFirst());
    assertEquals("A", trip.pollFirst());

    navigator.navigateTo(b);

    assertEquals("A/out", trip.pollFirst());
    assertEquals("B/in", trip.pollFirst());
    assertEquals("B-A/in", trip.pollFirst());
    assertEquals("B-A", trip.pollFirst());
    assertEquals("B", trip.pollFirst());

    navigator.navigateTo(bb);

    assertEquals("B-A/out", trip.pollFirst());
    assertEquals("B-B/in", trip.pollFirst());
    assertEquals("B-B", trip.pollFirst());

    navigator.back();

    assertEquals("B-B/out", trip.pollFirst());
    assertEquals("B", trip.pollFirst());
    assertEquals("B-A/in", trip.pollFirst());
    assertEquals("B-A", trip.pollFirst());

    navigator.navigateToModal(bm);

    assertEquals("B-M/in", trip.pollFirst());
    assertEquals("B-M", trip.pollFirst());

    navigator.back();

    assertEquals("B-M/out", trip.pollFirst());

    navigator.navigateParentTo(play);

    assertEquals(ba, play.getPrevious());
    assertNull(play.getParent());
    assertEquals("B-A/out", trip.pollFirst());
    assertEquals("play/in", trip.pollFirst());
    assertEquals("play", trip.pollFirst());

    navigator.back();

    assertEquals("play/out", trip.pollFirst());
    assertEquals("B", trip.pollFirst());
    assertEquals("B-A/in", trip.pollFirst());
    assertEquals("B-A", trip.pollFirst());

    navigator.back();

    assertEquals("B-A/out", trip.pollFirst());
    assertEquals("B/out", trip.pollFirst());
    assertEquals("A/in", trip.pollFirst());
    assertEquals("A", trip.pollFirst());
    assertTrue(trip.isEmpty());
  }

  private class TestDestination extends Destination {
    private final Deque<String> trip;

    public TestDestination(Deque<String> trip, String description) {
      super(description);
      this.trip = trip;
    }

    @Override
    public void intro() {
      trip.add(getDescription() + "/in");
    }

    @Override
    public void go() {
      trip.add(getDescription());
    }

    @Override
    public void outro() {
      trip.add(getDescription() + "/out");
    }
  }
}
