package hs.mediasystem.screens.movie;

import hs.mediasystem.Constants;
import hs.mediasystem.Controller;
import hs.mediasystem.MediaSystem;
import hs.mediasystem.SizeFormatter;
import hs.mediasystem.SwingWorker2;
import hs.mediasystem.Worker;
import hs.mediasystem.framework.AbstractBlock;
import hs.mediasystem.framework.Group;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Renderer;
import hs.mediasystem.framework.State;
import hs.mediasystem.framework.View;
import hs.mediasystem.fs.Episode;
import hs.models.BasicListModel;
import hs.models.Model;
import hs.models.ValueModel;
import hs.models.events.EventListener;
import hs.ui.HorizontalAlignment;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.DynamicLabel;
import hs.ui.controls.HorizontalGroup;
import hs.ui.controls.MultiLineDynamicLabel;
import hs.ui.controls.Picture;
import hs.ui.controls.VerticalGroup;
import hs.ui.controls.listbox.SimpleList;
import hs.ui.events.ItemsEvent;
import hs.ui.events.KeyPressedEvent;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class MediaSelection extends AbstractBlock<MediaSelectionConfig> {
  private final BasicListModel<MediaItem> menuModel = new BasicListModel<MediaItem>(new ArrayList<MediaItem>());
  private final Model<String> plot = new ValueModel<String>();
  private final Model<BufferedImage> cover = new ValueModel<BufferedImage>();
  private final Model<String> runtime = new ValueModel<String>();
  private final Model<Integer> listRowHeight = new ValueModel<Integer>(20);
  private final Model<ListCellRenderer<? super MediaItem>> listCellRenderer = new ValueModel<ListCellRenderer<? super MediaItem>>();
  private final Model<Integer> listFirstSelectedIndex = new ValueModel<Integer>(0);
  private final Model<Rectangle> listVisibleRectangle = new ValueModel<Rectangle>();
  
  @Override
  public State currentState() {
    return new State() {
      private final Rectangle savedVisibleRectangle = listVisibleRectangle.get();
      private final int selectedIndex = listFirstSelectedIndex.get();

      {
        System.out.println(">>> Creating State, selectedIndex = " + selectedIndex);
      }
      
      @Override
      public void apply() {
        listVisibleRectangle.set(savedVisibleRectangle);
        listFirstSelectedIndex.set(selectedIndex);
      }
    };
  }
    
  @Override
  public void applyConfig(MediaSelectionConfig config) {
    MediaTree mediaTree = config.getMediaTree();
    
    Renderer<MediaItem> renderer = mediaTree.getRenderer();
    
    listCellRenderer.set(new MyCellRenderer(renderer));
    listRowHeight.set(renderer.getPreferredHeight());
    
    menuModel.clear();
    menuModel.addAll(mediaTree.children());
    
    plot.set("");
    cover.set(null);
    runtime.set("");
  }
    
  @Override
  protected AbstractGroup<?> create(final Controller controller) {
//    new InternalContent(mediaTree).apply(); 
//    menuModel.addAll(mediaTree.children());
//    listCellRenderer.set(new MyCellRenderer(mediaTree.getRenderer()));
    
    final SwingWorker2 movieUpdater = new SwingWorker2();

    return new VerticalGroup() {{
      weightX().set(1.0);
      weightY().set(1.0);
      opaque().set(false);
      add(new HorizontalGroup() {{
        weightX().set(1.0);
        weightY().set(1.0);
        add(new VerticalGroup() {{
          border().set(new EmptyBorder(10, 10, 10, 10));
          weightX().set(1.0);
          add(new Picture() {{
            image.link(cover);
            opaque().set(false);
          }});
          //          add(new DynamicLabel().icon().link(cover));
          add(new MultiLineDynamicLabel() {{
            text.link(plot);
            weightY().set(0.2);
            fgColor().link(Constants.MAIN_TEXT_COLOR);
            font().set(new Font("Sans Serif", Font.PLAIN, 16));
          }});
          add(new DynamicLabel() {{
            text().link(runtime);
            fgColor().link(Constants.MAIN_TEXT_COLOR);
            horizontalAlignment.set(HorizontalAlignment.RIGHT);
            font().set(new Font("Sans Serif", Font.BOLD, 14));
          }});
        }});
        add(new SimpleList<MediaItem>() {{
          weightX().set(1.0);
          fgColor().set(new Color(155, 190, 255, 200));
          bgColor().set(new Color(0, 0, 0, 0));
          opaque().set(false);
          border().set(new EmptyBorder(0, 0, 0, 0));
          model.link(menuModel);
          font().set(new Font("Sans Serif", Font.PLAIN, 32));
          minHeight().set(400);
          //maxHeight().set(30000);
          minWidth().set(50);
          maxWidth().set(30000);
          rowHeight.link(listRowHeight);
          cellRenderer.link(listCellRenderer);
          visibleRectangle.proxy(listVisibleRectangle);
          firstSelectedIndex.link(listFirstSelectedIndex);
          selectFirstItem();
          onItemDoubleClick().call(new EventListener<ItemsEvent<MediaItem>>() {
            @Override
            public void onEvent(ItemsEvent<MediaItem> event) {
              final MediaItem item = event.getFirstItem();

              if(item instanceof Episode) {
                controller.setBackground(false);
                controller.playMedia((Episode)item);
                controller.forward(new View("Play", MediaSystem.VIDEO_PLAYING));
              }
              else if(item.isRoot()) {
                View view = controller.cloneCurrentView("" + item); // TODO use descriptive name here
                MediaSelectionConfig config = view.replaceConfig(MediaSelectionConfig.class);
                config.setMediaTree(item.getRoot());
                controller.forward(view);
              }
            }
          });
          onItemFocused().call(new EventListener<ItemsEvent<MediaItem>>() {
            @Override
            public void onEvent(ItemsEvent<MediaItem> event) {
              final MediaItem item = event.getFirstItem();

              plot.set("");
              cover.set(null);
              runtime.set("");

              if(item instanceof Episode) {
                final Episode episode = (Episode)item;
                
                movieUpdater.doTask(400, new Worker() {
                  @Override
                  public void doInBackground() {
                    episode.getPlot();
                  }
  
                  @Override
                  public void done() {
                    plot.set(episode.getPlot());
                    cover.set(episode.getImage());
  
                    if(episode.getRuntime() != 0) {
                      runtime.set(SizeFormatter.DURATION.format(episode.getRuntime() * 60));
                    }
                  }
                });
              }

              //getController().setContent(item.getScreen());
            }
          });
          onKeyPressed().call(new EventListener<KeyPressedEvent>() {
            @Override
            public void onEvent(KeyPressedEvent event) {
              if(event.getKeyCode() == KeyEvent.VK_RIGHT && event.isPressed()) {
                System.out.println("Expand");

                @SuppressWarnings("unchecked")
                MediaItem activeRow = ((SimpleList<MediaItem>)event.getSource()).getActiveRow();
                
                if(activeRow instanceof Group) {
                  Group group = (Group)activeRow;
                  menuModel.addAll(menuModel.indexOf(activeRow) + 1, group.children());
                }
              }
            }
          });
        }});
      }});
    }};
  }
  
  public static class MyCellRenderer implements ListCellRenderer<MediaItem> {
    private final Renderer<MediaItem> renderer;

    public MyCellRenderer(Renderer<MediaItem> renderer) {
      this.renderer = renderer;
    }
    
    @Override
    public Component getListCellRendererComponent(JList<? extends MediaItem> list, MediaItem value, int index, boolean isSelected, boolean cellHasFocus) {
      return renderer.render(value, isSelected);
    }
  }
}
