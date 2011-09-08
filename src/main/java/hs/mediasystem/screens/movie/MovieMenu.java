package hs.mediasystem.screens.movie;

import hs.mediasystem.Constants;
import hs.mediasystem.Controller;
import hs.mediasystem.Episode;
import hs.mediasystem.EpisodeGroup;
import hs.mediasystem.MediaItem;
import hs.mediasystem.MediaTree;
import hs.mediasystem.Renderer;
import hs.mediasystem.SizeFormatter;
import hs.mediasystem.SwingWorker2;
import hs.mediasystem.Worker;
import hs.mediasystem.screens.AbstractBlock;
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
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class MovieMenu extends AbstractBlock {
  private static final int HEIGHT = 48;
  
  private final BasicListModel<MediaItem> menuModel = new BasicListModel<MediaItem>(new ArrayList<MediaItem>());
  private final Model<String> plot = new ValueModel<String>();
  private final Model<BufferedImage> cover = new ValueModel<BufferedImage>();
  private final Model<String> runtime = new ValueModel<String>();
  private final MediaTree mediaTree;

  //private static final BufferedImage EMPTY_IMAGE

  public MovieMenu(MediaTree mediaTree) {
    this.mediaTree = mediaTree;
  }
  
  @Override
  protected AbstractGroup<?> create(final Controller controller) {
    menuModel.addAll(mediaTree.children());
    
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
          items().link(menuModel);
          font().set(new Font("Sans Serif", Font.PLAIN, 32));
          minHeight().set(400);
          //maxHeight().set(30000);
          minWidth().set(50);
          maxWidth().set(30000);
          rowHeight().set(HEIGHT);
          setCellRenderer(new MyCellRenderer(mediaTree.getRenderer()));
          selectFirstItem();
          onItemDoubleClick().call(new EventListener<ItemsEvent<MediaItem>>() {
            @Override
            public void onEvent(ItemsEvent<MediaItem> event) {
              final MediaItem item = event.getFirstItem();

              if(item instanceof Episode) {
                controller.setBackground(false);
                controller.playMedia((Episode)item);
                controller.forward("VideoPlayingMenu");
              }
              else if(item.isRoot()) {
                System.out.println("MovieMenu: Clicked on root: " + item);
                menuModel.clear();
                MediaTree tree = item.getRoot();
                setCellRenderer(new MyCellRenderer(tree.getRenderer()));
                menuModel.addAll(tree.children());
              }
            }
          });
          onItemSelected().call(new EventListener<ItemsEvent<MediaItem>>() {
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
                MediaItem activeRow = ((SimpleList<MediaItem>) event.getSource()).getActiveRow();

                if(activeRow.isGroup()) {
                  menuModel.addAll(menuModel.indexOf(activeRow) + 1, ((EpisodeGroup) activeRow).children());
                }
              }
            }
          });
        }});
      }});
    }};
  }
  
  private class MyCellRenderer implements ListCellRenderer<MediaItem> {
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
