package hs.mediasystem.util;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Simplistic implementation of a List which only allows unique elements.
 *
 * It is not allowed to set an index to a new value as its behaviour when set would result
 * in a duplicate element is hard to define.
 *
 * Note that addAll with index can work a bit unexpected.  When adding a collection which
 * contains elements already in this UniqueList then this element will be skipped --
 * therefore the element already in the list will retain its position.
 *
 * For example, consider a list: D, E, F, B.  Adding a list A, B, C at index 0 would allow A
 * and C to be inserted, but not B.  The final result would be: A, C, D, E, F, B.
 *
 * As a result of the unique constraint, the contains method performs faster than for
 * regular lists.
 *
 * @param <T> element type
 */
public class UniqueArrayList<T> extends AbstractList<T> implements UniqueList<T> {
  private final List<T> backingList = new ArrayList<>();
  private final Set<T> backingSet = new HashSet<>();

  @Override
  public T get(int index) {
    return backingList.get(index);
  }

  @Override
  public int size() {
    return backingList.size();
  }

  @Override
  public boolean add(T element) {
    if(backingSet.add(element)) {
      backingList.add(element);
      return true;
    }

    return false;
  }

  @Override
  public void add(int index, T element) {
    addInternal(index, element);
  }

  public boolean addInternal(int index, T element) {
    if(backingSet.add(element)) {
      backingList.add(index, element);
      return true;
    }

    return false;
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    int i = index;
    boolean modified = false;

    for(T element : c) {
      if(addInternal(i, element)) {
        i++;
        modified = true;
      }
    }

    return modified;
  }

  @Override
  public T remove(int index) {
    T removedElement = backingList.remove(index);

    backingSet.remove(removedElement);

    return removedElement;
  }

  @Override
  public void clear() {
    backingList.clear();
    backingSet.clear();
  }

  @Override
  public boolean contains(Object o) {
    return backingSet.contains(o);
  }
}
