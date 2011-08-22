package hs.mediasystem.screens;

import hs.mediasystem.Controller;
import hs.mediasystem.Extensions;
import hs.mediasystem.Screen;
import hs.ui.Container;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.GUIControl;
import hs.ui.controls.HorizontalGroup;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;

public abstract class AbstractBlock {
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
  
  protected abstract AbstractGroup<?> create(Controller controller);
  
  private final Map<String, HorizontalGroup> placeHolders = new HashMap<String, HorizontalGroup>();
  
  protected void addExtension(AbstractGroup<?> group, String extensionName) {
    HorizontalGroup placeHolder = new HorizontalGroup();

    placeHolder.border().set(BorderFactory.createLineBorder(Color.RED));
    
    group.add(placeHolder);
    placeHolders.put(extensionName, placeHolder);
  }
}
