package hs.mediasystem.screens.videoplaying;

import hs.mediasystem.Constants;
import hs.mediasystem.Controller;
import hs.mediasystem.framework.AbstractBlock;
import hs.mediasystem.framework.NoConfig;
import hs.models.Convertor;
import hs.models.events.EventListener;
import hs.smartlayout.Anchor;
import hs.sublight.SubtitleDescriptor;
import hs.ui.AcceleratorScope;
import hs.ui.ControlListener;
import hs.ui.HorizontalAlignment;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.Column;
import hs.ui.controls.HorizontalGroup;
import hs.ui.controls.Label;
import hs.ui.controls.VerticalGroup;
import hs.ui.controls.listbox.ListBox2;
import hs.ui.events.ItemsEvent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class VideoOptionsScreen extends AbstractBlock<NoConfig> {
      
  //private static final BufferedImage EMPTY_IMAGE

  @Override
  protected AbstractGroup<?> create(final Controller controller) {  
    return new VerticalGroup() {{
      setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), AcceleratorScope.ANCESTOR_WINDOW, new ControlListener<VerticalGroup>() {
        @Override
        public void onEvent(VerticalGroup control) {
          System.out.println("Home was pressed");
          
        }
      });
      weightX().set(1.0);
      weightY().set(1.0);
      opaque().set(false);
      add(new HorizontalGroup() {{
        add(new Label() {{
          fgColor().set(new Color(155, 190, 255));
          text().set("Media System");
        }});
        anchor().set(Anchor.CENTER);
        weightY().set(0.25);
      }});
      add(new HorizontalGroup() {{
        weightX().set(1.0);
        weightY().set(1.0);
        maxWidth().set(10000);
        add(new VerticalGroup() {{
          weightX().set(0.25);
        }});
        add(new VerticalGroup() {{
          border().set(BorderFactory.createLineBorder(Constants.PANEL_BG_COLOR, 40, true));  // TODO does not do proper transparency; interferes with bgcolor (they get added so a 2nd rounded corner appears)
          weightX().set(1.0);
          weightY().set(1.0);
          add(new ListBox2<SubtitleDescriptor>() {{
            fgColor().set(new Color(155, 190, 255, 200));
            bgColor().set(new Color(0, 0, 0, 0));
            opaque().set(false);
            border().set(new EmptyBorder(0, 0, 0, 0));
            columns().add(new Column<SubtitleDescriptor>() {{
              convertor().set(new Convertor<SubtitleDescriptor, Object>() {
                @Override
                public Object convert(SubtitleDescriptor value) {
                  return value;
                }
              });
              alignment().set(HorizontalAlignment.LEFT);
              width().set(750);
              renderer().set(new MySubtitleCellRenderer());
              anchor().set(Anchor.CENTER);
            }});
            items().link(controller.subtitles);
            font().set(new Font("Sans Serif", Font.PLAIN, 32));
            weightX().set(1.0);
            weightY().set(1.0);
            
//            minHeight().set(400);
//            maxHeight().set(400);
//            minWidth().set(1);
//            maxWidth().set(1300);
            rowHeight().set(40);
            onItemDoubleClick().call(new EventListener<ItemsEvent<SubtitleDescriptor>>() {
              @Override
              public void onEvent(ItemsEvent<SubtitleDescriptor> event) {
                SubtitleDescriptor item = event.getFirstItem();
                
                System.out.println("Selected subtitle : " + item);
                controller.setSubtitle(item);
                controller.back();
              }
            });
          }});
        }});
        add(new VerticalGroup() {{
          weightX().set(0.25);
        }});
      }});
      add(new HorizontalGroup() {{
        add(new Label() {{
          fgColor().set(new Color(155, 190, 255));
          text().set("" + new Date());
        }});
        anchor().set(Anchor.CENTER);
        weightY().set(0.25);
      }});
    }};
  }
  
  private class MySubtitleCellRenderer implements TableCellRenderer {
    private final DefaultTableCellRenderer defaultTableCellRenderer = new DefaultTableCellRenderer();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      SubtitleDescriptor entry = (SubtitleDescriptor)value;
      
      defaultTableCellRenderer.setForeground(table.getForeground());
      defaultTableCellRenderer.getTableCellRendererComponent(table, entry.getName(), isSelected, hasFocus, row, column);
      
      Dimension preferredSize = defaultTableCellRenderer.getPreferredSize();
      preferredSize.width += 2;  // Workaround for Label text width bug
      defaultTableCellRenderer.setMaximumSize(preferredSize);
      
      if(!isSelected) {
        defaultTableCellRenderer.setBackground(table.getBackground());
      }
      else {
        defaultTableCellRenderer.setBackground(new Color(255, 0, 0, 120));
      }
            
      //defaultTableCellRenderer.setOpaque(false);

      return defaultTableCellRenderer;
    }    
  }
}
