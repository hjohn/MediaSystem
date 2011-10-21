package hs.mediasystem;

import hs.mediasystem.framework.Player;
import hs.mediasystem.framework.Screen;
import hs.mediasystem.framework.State;
import hs.mediasystem.framework.View;
import hs.mediasystem.fs.Episode;
import hs.models.BasicListModel;
import hs.models.ListModel;
import hs.models.events.Listener;
import hs.sublight.SubtitleDescriptor;
import hs.ui.AcceleratorScope;
import hs.ui.ControlListener;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.VerticalGroup;
import hs.ui.frames.AbstractFrame;
import hs.ui.image.ImageHandle;
import hs.ui.swing.Painter;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jdesktop.core.animation.timing.Animator;
import org.jdesktop.core.animation.timing.Interpolator;
import org.jdesktop.core.animation.timing.TimingTargetAdapter;
import org.jdesktop.swing.animation.timing.sources.SwingTimerTimingSource;

public class Controller {
  private static final SwingTimerTimingSource TIMING_SOURCE = new SwingTimerTimingSource();
  
  private final Player player;
  private final VerticalGroup mainGroup;
  private final ImageHandle backgroundHandle;
  private final NavigationHistory<View> navigationHistory = new NavigationHistory<View>();
  private final StateCache stateCache = new StateCache();
  
  private boolean backgroundActive = true;

  static {
    TIMING_SOURCE.init();

    Animator.setDefaultTimingSource(TIMING_SOURCE);
  }
  
  private final Map<Integer, KeyHandler> videoKeyHandlers = new HashMap<Integer, KeyHandler>() {{
//    put(KeyEvent.VK_ESCAPE, new KeyHandler() {
//      @Override
//      public void keyPressed() {
//        System.out.println("CONTROLLER: Exiting, escape pressed");
//        getMediaPlayer().dispose();
//        parentFrame.dispose();
//      }
//    });

    put(KeyEvent.VK_SPACE, new KeyHandler() {
      @Override
      public void keyPressed() {
        getMediaPlayer().pause();
      }
    });
    
    put(KeyEvent.VK_NUMPAD4, new KeyHandler() {
      @Override
      public void keyPressed() {
        skip(-10 * 1000);
      }
    });
    
    put(KeyEvent.VK_NUMPAD6, new KeyHandler() {
      @Override
      public void keyPressed() {
        skip(10 * 1000);
      }
    });

    put(KeyEvent.VK_NUMPAD2, new KeyHandler() {
      @Override
      public void keyPressed() {
        skip(-90 * 1000);
      }
    });
    
    put(KeyEvent.VK_NUMPAD8, new KeyHandler() {
      @Override
      public void keyPressed() {
        skip(90 * 1000);
      }
    });

    put(KeyEvent.VK_S, new KeyHandler() {
      @Override
      public void keyPressed() {
        stop();
      }
    });

    put(KeyEvent.VK_9, new KeyHandler() {
      @Override
      public void keyPressed() {
        int volume = getMediaPlayer().getVolume();
        
        volume--;
        
        if(volume < 0) {
          volume = 0;
        }
        
        getMediaPlayer().setVolume(volume);
      }
    });

    put(KeyEvent.VK_0, new KeyHandler() {
      @Override
      public void keyPressed() {
        int volume = getMediaPlayer().getVolume();
        
        volume++;
        
        if(volume > 100) {
          volume = 100;
        }
        
        getMediaPlayer().setVolume(volume);
      }
    });
    
    put(KeyEvent.VK_X, new KeyHandler() {
      @Override
      public void keyPressed() {
        int delay = getMediaPlayer().getSubtitleDelay();
        
        delay -= 100;
        
        getMediaPlayer().setSubtitleDelay(delay);
      }
    });

    put(KeyEvent.VK_Z, new KeyHandler() {
      @Override
      public void keyPressed() {
        int delay = getMediaPlayer().getSubtitleDelay();
        
        delay += 100;
        
        getMediaPlayer().setSubtitleDelay(delay);
      }
    });
    
    put(KeyEvent.VK_M, new KeyHandler() {
      @Override
      public void keyPressed() {
        getMediaPlayer().setMute(!getMediaPlayer().isMute());
      }
    });
  }};
  
  private final AbstractFrame<?> parentFrame;
  private final JPanel glassPane2;
  private final VerticalGroup glassPane = new VerticalGroup();
  private final Thread windowToFrontThread;
  
  private abstract class KeyHandler {
    public abstract void keyPressed();
  }
  
  public Controller(final Player player, final AbstractFrame<?> parentFrame) {
    this.player = player;
    this.parentFrame = parentFrame;
    glassPane2 = (JPanel)((JFrame)parentFrame.getContainer()).getGlassPane();
    
    glassPane2.setLayout(new BorderLayout());
    glassPane2.add(glassPane.getComponent(), BorderLayout.CENTER);
    glassPane2.setVisible(true);
    
    player.onFinished().call(new Listener() {
      @Override
      public void onEvent() {
        stop();
      }
    });

    DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
      private long lastKeyProcessedTime;  // used to avoid flooding MPlayer...
      
      @Override
      public boolean dispatchKeyEvent(KeyEvent e) {
        //System.out.println("Key: '" + e.getKeyChar() + "'; code = " + e.getKeyCode());
        
        if(e.getID() == KeyEvent.KEY_PRESSED && !backgroundActive) {
          KeyHandler handler = videoKeyHandlers.get(e.getKeyCode());
          
          if(handler != null) {
            if(lastKeyProcessedTime + 100 < System.currentTimeMillis()) { 
              handler.keyPressed();
              lastKeyProcessedTime = System.currentTimeMillis();
              return true;
            }
          }
        }
        
        return false;
      }
    });
    
//    DefaultKeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventPostProcessor(new KeyEventPostProcessor() {
//      @Override
//      public boolean postProcessKeyEvent(KeyEvent e) {
//        if(!e.isConsumed()) {
//          System.out.println("NOT CONSUMED : post process key: " + e);
//        }
//        else {
//          System.out.println("CONSUMED : post process key: " + e);
//        }
//        return false;
//      }
//    });

    try {
      byte[] background = Files.readAllBytes(Paths.get("images/Media Center background.jpg"));
      backgroundHandle = new ImageHandle(background, ":background");
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
    
    mainGroup = new VerticalGroup() {{
      setPainter(new Painter() {
        @Override
        public void paint(Graphics2D g, int width, int height) {
          if(backgroundActive) {
            g.drawImage(backgroundHandle.getImage(width, height, false), 0, 0, null);
          }
          else {
            g.setBackground(new Color(0, 0, 0, 1));
            g.clearRect(0, 0, width, height);
          }
        }
      });
      overrideWeightX(1.0);
      overrideWeightY(1.0);
      bgColor().set(new Color(0, 0, 0, 0));
//      bgColor().set(Color.BLACK);
      opaque().set(true);
    }};
    
    mainGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), AcceleratorScope.ANCESTOR_WINDOW, new ControlListener<VerticalGroup>() {
      @Override
      public void onEvent(VerticalGroup control) {
        back();
      }
    });
    
    mainGroup.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), AcceleratorScope.ANCESTOR_WINDOW, new ControlListener<VerticalGroup>() {
      @Override
      public void onEvent(VerticalGroup control) {
        System.out.println("CONTROLLER: Exiting, escape pressed");
        getMediaPlayer().dispose();
        parentFrame.dispose();
        TIMING_SOURCE.dispose();
      }
    });
    
    parentFrame.add(mainGroup);
    
//    new Thread() {
//      public void run() {
//        for(;;) {
//          try {
//            Thread.sleep(2000);
//          }
//          catch(InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//          }
//          KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
//          System.out.println("--> " + manager.getFocusOwner());
//          
//        }
//      }
//    }.start();
    
    windowToFrontThread = new Thread() {
      @Override
      public void run() {
        for(;;) {
          try {
            Thread.sleep(2L << windowToFrontDelay.getAndIncrement());
            parentFrame.toFront();
          }
          catch(InterruptedException e) {
            // occurs when windowToFrontDelay changed
          }
        }
      }
    };
    
    windowToFrontThread.setDaemon(true);
    windowToFrontThread.setName("WindowToFront");
    windowToFrontThread.start();
  }
  
  private final AtomicInteger windowToFrontDelay = new AtomicInteger(8);
  
  private void emptyMainGroup() {
    mainGroup.removeAll();
  }

  private void changeView(View view) {
    emptyMainGroup();
    
    AbstractGroup<?> content = getContent(view.getScreen());
    mainGroup.add(content);
    
    view.applyConfig();
    
    State state = stateCache.getState(generateKey(navigationHistory.getPath()));
    
    if(state != null) {
      state.apply();
    }
    
    parentFrame.validate();
    mainGroup.repaint();
    setDefaultFocus();
    parentFrame.toFront();
  }
  
  public void back() {
    if(!navigationHistory.isEmpty()) {
      stateCache.putState(generateKey(navigationHistory.getPath()), navigationHistory.current().getScreen().getState());
    }
    
    View view = navigationHistory.back();
    
    if(view != null) {
      changeView(view);
    }
  }
  
  public void forward(View view) {
    if(!navigationHistory.isEmpty()) {
      stateCache.putState(generateKey(navigationHistory.getPath()), navigationHistory.current().getScreen().getState());
    }
    
    navigationHistory.forward(view);
    
    changeView(view);
  }
  
  private static Object generateKey(List<View> stack) {
    String key = "";
    
    for(View view : stack) {
      key += view.getName() + " > ";
    }
    
    return key;
  }
  
  private void setDefaultFocus() {
    final KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    System.out.println("FOCUS: Before RequestFocus " + manager.getFocusOwner());
    mainGroup.requestFocus();
    System.out.println("FOCUS: After RequestFocus " + manager.getFocusOwner());
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        manager.focusNextComponent();
        System.out.println("FOCUS: After FocusNext " + manager.getFocusOwner());
      }
    });
  }
  
  public void setBackground(boolean active) {
    this.backgroundActive = active;
  }
  
  private Player getMediaPlayer() {
    return player;
  }
    
  private AbstractGroup<?> getContent(Screen screen) {
    return screen.getContent(this);
  }
  
  public Screen activeScreen() {
    return navigationHistory.current().getScreen(); 
  }

  private Episode currentItem;
  
  public void playMedia(Episode episode) {
    currentItem = episode;
    player.play(episode.getPath());
    
    windowToFrontDelay.set(8);
    windowToFrontThread.interrupt();
  }
  
  public void stop() {
    getMediaPlayer().stop();
    setBackground(true);
    if(activeScreen() == MediaSystem.VIDEO_PLAYING) {
      back();
    }
    else {
      mainGroup.repaint();
    }
  }
  
  public Episode getCurrentItem() {
    return currentItem;
  }
  
  public final ListModel<SubtitleDescriptor> subtitles = new BasicListModel<SubtitleDescriptor>(new ArrayList<SubtitleDescriptor>());

  public void setSubtitle(SubtitleDescriptor item) {
    try {
      ByteBuffer fetch = item.fetch();
      
      FileOutputStream os = new FileOutputStream("tempsubtitle.srt");
      
      os.getChannel().write(fetch);
      os.close();
      
      getMediaPlayer().showSubtitle("tempsubtitle.srt");
      
//      System.out.println("sub track = " + getMediaPlayer().getSpu());
//      getMediaPlayer().setSubTitleFile("file:///d:/tempsubtitle.srt");
//      getMediaPlayer().addMediaOptions("sub-delay=-3000", "sub-file=file:///d:/tempsubtitle.srt");
//      System.out.println("sub track2= " + getMediaPlayer().getSpu());
//      getMediaPlayer().setSpu(1);
//      System.out.println("sub track3= " + getMediaPlayer().getSpu());
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public View cloneCurrentView(String name) {
    return navigationHistory.current().copy(name);
  }
  
  private final VideoPositionOverlay overlay = new VideoPositionOverlay();
  private final VerticalGroup bar = new VerticalGroup();
  
  public void skip(long millis) {
    long time = getMediaPlayer().getPosition();
    final long length = getMediaPlayer().getLength();
    
    time += millis;
    
    if(time > length - 1000) {
      time = length - 1000;
    }

    if(time < 0) {
      time = 0;
    }
    
    getMediaPlayer().setPosition(time);
    
    overlay.setPosition((double)time / length);
    bar.weightX().set(1.0);
    bar.weightY().set(1.0);
    
    bar.setPainter(overlay); 
    
    glassPane.removeAll();
    glassPane.add(bar);
    
    glassPane.revalidate();
    glassPane.getComponent().validate();
    
    overlay.reset();
    
//    System.err.println("glassPane = " + glassPane.getComponent().getSize());
//    System.err.println("bar = " + bar.getComponent().getSize());
  }
  
  private class VideoPositionOverlay extends TimingTargetAdapter implements Painter {
    private final Animator fadeIn = new Animator.Builder().setDuration(500, TimeUnit.MILLISECONDS).addTarget(this).build();
    private final Animator hold = new Animator.Builder().setInterpolator(new Interpolator() {
      @Override
      public double interpolate(double fraction) {
        return 1.0;
      }
    }).setDuration(1000, TimeUnit.MILLISECONDS).addTarget(this).build();
    private final Animator fadeOut = new Animator.Builder().setInterpolator(new Interpolator() {
      @Override
      public double interpolate(double fraction) {
        return 1.0 - fraction;
      }
    }).setDuration(2500, TimeUnit.MILLISECONDS).addTarget(this).build();
    
    private double transparency;
    private double position;
    
    public void setPosition(double position) {
      this.position = position;
    }
    
    public synchronized void reset() {
      if(hold.isRunning()) {
        hold.cancel();
        hold.start();
      }
      else if(fadeOut.isRunning() || !fadeIn.isRunning()) {
        fadeOut.cancel();
        fadeIn.start();
      }
    }
    
    @Override
    public synchronized void end(Animator source) {
      if(source == fadeIn) {
        hold.start();
      }
      else if(source == hold) {
        fadeOut.start();
      }
    }
    
    @Override
    public synchronized void timingEvent(Animator source, double fraction) {
      this.transparency = fraction;
      bar.repaint();
    }

    @Override
    public void paint(Graphics2D g, int width, int height) {
      int x = width * 10 / 100;
      int y = height * 80 / 100;
      int w = width * 80 / 100;
      int h = height * 5 / 100;
      int b = 10;
      
      g.setStroke(new BasicStroke(6.0f));
      Color c = Constants.MAIN_TEXT_COLOR.get();
      g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(255 * transparency)));
      
      g.drawRect(x, y, w, h);
      
      g.fillRect(x + b, y + b, (int)((w - 2 * b) * position), h - 2 * b);
    }
  }
}

