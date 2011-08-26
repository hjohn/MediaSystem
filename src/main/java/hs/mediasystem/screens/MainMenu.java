package hs.mediasystem.screens;

import hs.mediasystem.Controller;
import hs.mediasystem.IconHelper;
import hs.models.BasicListModel;
import hs.models.events.EventListener;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.VerticalGroup;
import hs.ui.controls.listbox.SimpleList;
import hs.ui.events.ItemsEvent;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import javax.swing.border.EmptyBorder;

public class MainMenu extends AbstractBlock {
  private BasicListModel<MenuElement> menuModel = new BasicListModel<MenuElement>(new ArrayList<MenuElement>());
  
  @Override
  protected AbstractGroup<?> create(final Controller controller) {
    menuModel.add(new MenuElement("Movies", IconHelper.readIcon("images/aktion.png", 32, 32), "MovieMenu"));
    menuModel.add(new MenuElement("Series", IconHelper.readIcon("images/aktion.png", 32, 32), "SerieMenu"));
    menuModel.add(new MenuElement("YouTube", IconHelper.readIcon("images/password.png", 32, 32), "MovieMenu"));
    menuModel.add(new MenuElement("TV", IconHelper.readIcon("images/tv.png", 32, 32), "MovieMenu"));
    menuModel.add(new MenuElement("Web", IconHelper.readIcon("images/browser.png", 32, 32), "MovieMenu"));
    menuModel.add(new MenuElement("Exit", IconHelper.readIcon("images/logout.png", 32, 32), "MovieMenu"));
    
    final MyListCellRenderer cellRenderer = new MyListCellRenderer();
    
    return new VerticalGroup() {{
      weightX().set(1.0);
      weightY().set(1.0);     
      opaque().set(false);
      
      add(new SimpleList<MenuElement>() {{
        fgColor().set(new Color(155, 190, 255, 200));
        bgColor().set(new Color(0, 0, 0, 0));
        opaque().set(false);
        border().set(new EmptyBorder(0, 0, 0, 0));
        setCellRenderer(cellRenderer);
//        columns().add(new Column<MenuElement>() {{
//          convertor().set(new Convertor<MenuElement, Object>() {
//            @Override
//            public Object convert(MenuElement value) {
//              return value;
//            }
//          });
//          alignment().set(HorizontalAlignment.LEFT);
//          width().set(300);
//          renderer().set(cellRenderer);
//          anchor().set(Anchor.CENTER);
//        }});
        items().link(menuModel);
        font().set(new Font("Sans Serif", Font.PLAIN, 32));
        minHeight().set(400);
        maxHeight().set(400);
        minWidth().set(300);
        maxWidth().set(300);
        rowHeight().set(40);
        selectFirstItem();
        onItemDoubleClick().call(new EventListener<ItemsEvent<MenuElement>>() {
          @Override
          public void onEvent(ItemsEvent<MenuElement> event) {
            MenuElement item = event.getFirstItem();
            
            controller.forward(item.getScreenName());
          }
        });
      }});
    }};
  }
}
