package hs.mediasystem.framework;

import hs.mediasystem.Controller;
import hs.ui.Container;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.GUIControl;
import hs.ui.controls.HorizontalGroup;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;

public abstract class AbstractBlock<C extends Config<?>> {
  private AbstractGroup<?> content;
    
  public AbstractGroup<?> getContent(Controller controller, Extensions extensions) {
    if(content == null) {
      content = create(controller);
    }
    
    for(String placeHolderName : placeHolders.keySet()) {
      HorizontalGroup placeHolder = placeHolders.get(placeHolderName);
      
      placeHolder.removeAll();
      
      Screen screen = extensions.get(placeHolderName);
      if(screen != null) {
        System.out.println("Setting extension for " + placeHolderName);
        AbstractGroup<?> extensionContent = screen.getContent(controller);
        Container<GUIControl> parent = extensionContent.getParent();
        if(parent != null) {
          parent.remove(extensionContent);
        }
        
        placeHolder.add(extensionContent);
      }
    }
    
    return content;
  }
  
  public State getState(final Extensions extensions) {
    return new State() {
      private final List<State> states = new ArrayList<State>();
      
      {
        states.add(currentState());

        for(Screen screen : extensions) {
          states.add(screen.getState());
        }
      }
      
      @Override
      public void apply() {
        for(State state : states) {
          state.apply();
        }
      }
    };
  }
  
  @SuppressWarnings("static-method")
  protected State currentState() {
    return new State() {
      @Override
      public void apply() {
      }
    };
  }
  
  protected abstract AbstractGroup<?> create(Controller controller);

  protected void applyConfig(C config) {
  }
  
  private final Map<String, HorizontalGroup> placeHolders = new HashMap<String, HorizontalGroup>();
  
  protected void addExtension(AbstractGroup<?> group, String extensionName) {
    HorizontalGroup placeHolder = new HorizontalGroup();

    placeHolder.border().set(BorderFactory.createLineBorder(Color.RED));
    
    group.add(placeHolder);
    placeHolders.put(extensionName, placeHolder);
  }

  @SuppressWarnings("unchecked")
  public void applyConfigWithCast(Config<?> config) {
    applyConfig((C)config);
  }
}
