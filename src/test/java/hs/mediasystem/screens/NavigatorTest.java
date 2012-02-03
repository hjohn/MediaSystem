package hs.mediasystem.screens;

import static org.junit.Assert.assertEquals;
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
  public void shouldGoBackToTopLevel() {
    Destination a = new TestDestination(trip, "A");
    Destination b = new TestDestination(trip, "B");
    Destination subA = new TestDestination(trip, "subA");
    Destination subB = new TestDestination(trip, "subB");
    Navigator subNavigator = new Navigator(navigator);

    navigator.navigateTo(a);
    navigator.navigateTo(b);
    subNavigator.navigateTo(subA);
    subNavigator.navigateTo(subB);

    trip.clear();

    subNavigator.back();

    assertEquals("subB/out", trip.pollFirst());
    assertEquals("subA/in", trip.pollFirst());
    assertEquals("subA", trip.pollFirst());
    assertTrue(trip.isEmpty());

    subNavigator.back();

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
