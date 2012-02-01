package hs.mediasystem.screens;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class NavigatorTest {
  private static Runnable EMPTY_RUNNABLE = new Runnable() {
    @Override
    public void run() {
    }
  };

  private Navigator navigator;

  @Before
  public void before() {
    navigator = new Navigator();
  }

  @Test
  public void shouldNavigateToDestination() {
    Destination dest = mock(Destination.class);

    navigator.navigateTo(dest);

    verify(dest).intro();
    verify(dest).go();
    verifyNoMoreInteractions(dest);
  }

  @Test
  public void shouldGoBackToPrevious() {
    Destination dest = mock(Destination.class);
    Destination dest2 = mock(Destination.class);

    navigator.navigateTo(dest);

    verify(dest).intro();
    verify(dest).go();
    verifyNoMoreInteractions(dest, dest2);
    Mockito.reset(dest, dest2);

    navigator.navigateTo(dest2);

    verify(dest).outro();
    verify(dest2).intro();
    verify(dest2).go();
    verifyNoMoreInteractions(dest, dest2);
    Mockito.reset(dest, dest2);

    navigator.back();

    verify(dest2).outro();
    verify(dest).intro();
    verify(dest).go();
    verifyNoMoreInteractions(dest, dest2);
  }

  @Test
  public void shouldHandleNestedNavigations() {
    Deque<String> trip = new LinkedList<>();

    final Destination ba = new TestDestination(trip, "B-A", EMPTY_RUNNABLE);
    Destination bb = new TestDestination(trip, "B-B", EMPTY_RUNNABLE);

    Destination a = new TestDestination(trip, "A", EMPTY_RUNNABLE);
    Destination b = new TestDestination(trip, "B", new Runnable() {
      @Override
      public void run() {
        navigator.navigateTo(ba);
      }
    });

    navigator.navigateTo(a);

    assertEquals("A/in", trip.pollFirst());
    assertEquals("A", trip.pollFirst());

    navigator.navigateTo(b);

    assertEquals("A/out", trip.pollFirst());
    assertEquals("B/in", trip.pollFirst());
    assertEquals("B", trip.pollFirst());
    assertEquals("B-A/in", trip.pollFirst());
    assertEquals("B-A", trip.pollFirst());

    navigator.navigateTo(bb);

    assertEquals("B-A/out", trip.pollFirst());
    assertEquals("B-B/in", trip.pollFirst());
    assertEquals("B-B", trip.pollFirst());

    navigator.back();

    assertEquals("B-B/out", trip.pollFirst());
    assertEquals("B-A/in", trip.pollFirst());
    assertEquals("B-A", trip.pollFirst());

    navigator.back();

    assertEquals("B-A/out", trip.pollFirst());
    assertEquals("B/out", trip.pollFirst());
    assertEquals("A/in", trip.pollFirst());
    assertEquals("A", trip.pollFirst());
  }

  private class TestDestination extends Destination {
    private final Deque<String> trip;

    public TestDestination(Deque<String> trip, String description, Runnable runnable) {
      super(description, runnable);
      this.trip = trip;
    }

    @Override
    public void intro() {
      trip.add(getDescription() + "/in");
    }

    @Override
    public void go() {
      trip.add(getDescription());
      super.go();
    }

    @Override
    public void outro() {
      trip.add(getDescription() + "/out");
    }
  }
}
