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
  
  public Object getKey() {
    StringBuilder builder = new StringBuilder();
    
    for(int index = 0; index <= currentStackIndex; index++) {
      builder.append(stack.get(index) + " > ");
    }
    
    return builder.toString();
  }

  public T current() {
    return currentStackIndex >= 0 ? stack.get(currentStackIndex) : null;
  }

  public boolean isEmpty() {
    return stack.isEmpty();
  }
}
