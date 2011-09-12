package hs.mediasystem;

import java.util.ArrayList;
import java.util.List;

public class NavigationHistory<T> {
  private final List<T> stack = new ArrayList<T>();

  private int currentStackIndex = -1;
  
  public T back() {
    if(currentStackIndex == 0) {
      return null;
    }
    
    currentStackIndex--;
    
    return stack.get(currentStackIndex);
  }
  
  public void forward(T destination) { 
    while(stack.size() - 1 > currentStackIndex) {
      stack.remove(stack.size() - 1);
    }
    
    stack.add(destination);
    currentStackIndex++;
  }
  
  public T current() {
    return stack.get(currentStackIndex);
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }
}
