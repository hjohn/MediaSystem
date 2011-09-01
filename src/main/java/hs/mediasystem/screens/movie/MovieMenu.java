package hs.mediasystem.screens.movie;

import hs.mediasystem.Controller;
import hs.mediasystem.Episode;
import hs.mediasystem.EpisodeGroup;
import hs.mediasystem.Scanner;
import hs.mediasystem.Serie;
import hs.mediasystem.SizeFormatter;
import hs.mediasystem.SwingWorker2;
import hs.mediasystem.TitleGrouper;
import hs.mediasystem.Worker;
import hs.mediasystem.screens.AbstractBlock;
import hs.models.BasicListModel;
import hs.models.Model;
import hs.models.ValueModel;
import hs.models.events.EventListener;
import hs.smartlayout.SmartLayout;
import hs.ui.HorizontalAlignment;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.DynamicLabel;
import hs.ui.controls.HorizontalGroup;
import hs.ui.controls.MultiLineDynamicLabel;
import hs.ui.controls.Picture;
import hs.ui.controls.VerticalGroup;
import hs.ui.controls.listbox.ListBox2;
import hs.ui.controls.listbox.SimpleList;
import hs.ui.events.ItemsEvent;
import hs.ui.events.KeyPressedEvent;
import hs.ui.swing.JPaintablePanel;
import hs.ui.swing.Painter;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;

public class MovieMenu extends AbstractBlock {
  public enum Mode {BANNER, LIST}
  
  private static final int HEIGHT = 48;
  private static final int SUBTITLE_HEIGHT = 16;
  private static final int TITLE_HEIGHT = HEIGHT - SUBTITLE_HEIGHT;
  private static final Color TITLE_COLOR = new Color(155, 190, 255, 200);

  private final BasicListModel<Episode> menuModel = new BasicListModel<Episode>(new ArrayList<Episode>());
  private final Model<String> plot = new ValueModel<String>();
  private final Model<BufferedImage> cover = new ValueModel<BufferedImage>();
  private final Model<String> runtime = new ValueModel<String>();
  private final Path moviesPath;
  private final Mode mode;
  private final Scanner scanner;

  //private static final BufferedImage EMPTY_IMAGE

  public MovieMenu(Path moviesPath, Scanner scanner, Mode mode) {
    this.moviesPath = moviesPath;
    this.scanner = scanner;
    this.mode = mode;
  }
  
  @Override
  protected AbstractGroup<?> create(final Controller controller) {
    Serie serie = scanner.scan(moviesPath);
    
    for(Episode episode : serie.episodes(new TitleGrouper())) {
      menuModel.add(episode);
    }

    Collections.sort(menuModel, new Comparator<Episode>() {
      @Override
      public int compare(Episode o1, Episode o2) {
        return o1.getTitle().compareTo(o2.getTitle());
      }
    });
    
    final MyCellRenderer cellRenderer = new MyCellRenderer();

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
            fgColor().set(TITLE_COLOR);
            font().set(new Font("Sans Serif", Font.PLAIN, 16));
          }});
          add(new DynamicLabel() {{
            text().link(runtime);
            fgColor().set(TITLE_COLOR);
            horizontalAlignment.set(HorizontalAlignment.RIGHT);
            font().set(new Font("Sans Serif", Font.BOLD, 14));
          }});
        }});
        add(new SimpleList<Episode>() {{
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
          setCellRenderer(cellRenderer);
          selectFirstItem();
          onItemDoubleClick().call(new EventListener<ItemsEvent<Episode>>() {
            @Override
            public void onEvent(ItemsEvent<Episode> event) {
              final Episode item = event.getFirstItem();

              controller.setBackground(false);
              controller.playMedia(item);
              controller.forward("VideoPlayingMenu");
            }
          });
          onItemSelected().call(new EventListener<ItemsEvent<Episode>>() {
            @Override
            public void onEvent(ItemsEvent<Episode> event) {
              final Episode item = event.getFirstItem();

              plot.set("");
              cover.set(null);
              runtime.set("");

              movieUpdater.doTask(400, new Worker() {
                @Override
                public void doInBackground() {
                  item.getPlot();
                }

                @Override
                public void done() {
                  plot.set(item.getPlot());
                  cover.set(item.getImage());

                  if(item.getRuntime() != 0) {
                    runtime.set(SizeFormatter.DURATION.format(item.getRuntime() * 60));
                  }
                }
              });

              //getController().setContent(item.getScreen());
            }
          });
          onKeyPressed().call(new EventListener<KeyPressedEvent>() {
            @Override
            public void onEvent(KeyPressedEvent event) {
              if(event.getKeyCode() == KeyEvent.VK_RIGHT && event.isPressed()) {
                System.out.println("Expand");

                @SuppressWarnings("unchecked")
                Episode activeRow = ((SimpleList<Episode>) event.getSource()).getActiveRow();

                if(activeRow instanceof EpisodeGroup) {
                  menuModel.addAll(menuModel.indexOf(activeRow) + 1, ((EpisodeGroup) activeRow).episodes());
                }
              }
            }
          });
        }});
      }});
    }};
  }

  private static class MyCellRenderer implements ListCellRenderer<Episode> {
    private final JPaintablePanel panel = new JPaintablePanel(new SmartLayout(true, 1, 0, 0)) {
      @Override
      public boolean isVisible() {
        return false; // Workaround for bug 6700748 (cursor flickers during D&D)
      }
    };
    private final JLabel line1 = new JLabel();
    private final JLabel line2 = new JLabel();

    public MyCellRenderer() {
      panel.setPainter(new Painter() {
        @Override
        public void paint(Graphics2D g, int width, int height) {
          Rectangle2D stringBounds = line1.getFont().getStringBounds(line1.getText(), g.getFontRenderContext());

          int lineHeight = (int) stringBounds.getHeight();

          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

          g.setFont(line1.getFont());
          g.setColor(line1.getForeground());
          g.drawString(line1.getText(), line1.getIconTextGap() + 5, (int) -stringBounds.getY() + (line2.getText().isEmpty() ? (SUBTITLE_HEIGHT - 4)/ 2 : 0));

          if(!line2.getText().isEmpty()) {
            stringBounds = line2.getFont().getStringBounds(line2.getText(), g.getFontRenderContext());
  
            g.setFont(line2.getFont());
            g.setColor(line2.getForeground());
            g.drawString(line2.getText(), line2.getIconTextGap() + 5, (int) -stringBounds.getY() + lineHeight - 4);
          }
        }
      });

      panel.setBorder(new EmptyBorder(5, 10, 5, 10));
    }
    

    @Override
    public Component getListCellRendererComponent(JList<? extends Episode> list, Episode value, int index, boolean isSelected, boolean cellHasFocus) {
      Episode entry = value;
      boolean subItemDisplay = entry.getEpisodeGroup() != null;
      
      if(subItemDisplay) {
        if(!isSelected) {
          line1.setForeground(TITLE_COLOR);
          line2.setForeground(TITLE_COLOR);
          panel.setBackground(list.getBackground());
        }
        else {
          line1.setForeground(TITLE_COLOR.brighter());
          line2.setForeground(TITLE_COLOR.brighter());
          panel.setBackground(new Color(255, 255, 255, 60));
        }

        line1.setText(entry.getTitle() + " " + entry.getSequence());
        if(entry.getSubtitle().isEmpty()) {
          line2.setText(entry.getTitle() + (entry.getSequence() < 2 ? "" : " " + toRomanLiteral(entry.getSequence())));
        }
        else {
          line2.setText(entry.getSubtitle());
        }
        line2.setFont(new Font("Sans Serif", Font.PLAIN, TITLE_HEIGHT - 4));
        line1.setFont(new Font("Sans Serif", Font.PLAIN, SUBTITLE_HEIGHT - 4));
        line1.setIconTextGap(25);
        line2.setIconTextGap(35);
      }
      else {
        if(!isSelected) {
          line1.setForeground(TITLE_COLOR);
          line2.setForeground(TITLE_COLOR);
          panel.setBackground(list.getBackground());
        }
        else {
          line1.setForeground(TITLE_COLOR.brighter());
          line2.setForeground(TITLE_COLOR.brighter());
          panel.setBackground(new Color(255, 255, 255, 60));
        }

        line1.setText(entry.getTitle());
        line2.setText(entry.getSubtitle());
        line1.setFont(new Font("Sans Serif", Font.PLAIN, TITLE_HEIGHT - 4));
        line2.setFont(new Font("Sans Serif", Font.PLAIN, SUBTITLE_HEIGHT - 4));
        line1.setIconTextGap(0);
        line2.setIconTextGap(10);
      }

      return panel;
    }


  }

  public static void main(String[] args) {
    for(int i = 1; i < 50; i++) {
      System.out.println(toRomanLiteral(i));
    }
  }
  
  public static String toRomanLiteral(int value) {
    StringBuilder builder = new StringBuilder();
    String[] digits = {"L", "IL", "XL", "X", "IX", "V", "IV", "I"};
    int[] limits = {50, 49, 40, 10, 9, 5, 4, 1};

    for(int limit = 0; limit < limits.length; limit++) {
      while(value > 0) {
        if(value >= limits[limit]) {
          builder.append(digits[limit]);
          value -= limits[limit];
        }
        else {
          break;
        }
      }
    }

    return builder.toString();
  }
}
