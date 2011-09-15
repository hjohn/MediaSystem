package hs.mediasystem;

import hs.mediasystem.framework.Player;
import hs.mediasystem.framework.Screen;
import hs.mediasystem.framework.State;
import hs.mediasystem.framework.View;
import hs.mediasystem.fs.Episode;
import hs.models.BasicListModel;
import hs.models.ListModel;
import hs.sublight.SubtitleDescriptor;
import hs.ui.AcceleratorScope;
import hs.ui.ControlListener;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.VerticalGroup;
import hs.ui.frames.AbstractFrame;
import hs.ui.swing.Painter;

import java.awt.Color;
import java.awt.DefaultKeyboardFocusManager;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class Controller {
  private final Player player;
  private final VerticalGroup mainGroup;
  private final Image background;
  private final NavigationHistory<View> navigationHistory = new NavigationHistory<View>();
  private final StateCache stateCache = new StateCache();
  
  private boolean backgroundActive = true;
  
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
        long time = getMediaPlayer().getPosition();
        
        time -= 10 * 1000;
        
        if(time < 0) {
          time = 0;
        }
        
        getMediaPlayer().setPosition(time);
//        System.out.println("isFinished() : " + getMediaPlayer().isPlaying() + " : " + getMediaPlayer().getPosition() + " : " + getMediaPlayer().getTime());
//        if(!getMediaPlayer().isPlaying()) {
//          getMediaPlayer().play();
//        }
      }
    });
    
    put(KeyEvent.VK_NUMPAD6, new KeyHandler() {
      @Override
      public void keyPressed() {
        long time = getMediaPlayer().getPosition();
        
        time += 10 * 1000;
        
        if(time > getMediaPlayer().getLength() - 1000) {
          time = getMediaPlayer().getLength() - 1000;
          
          if(time < 0) {
            time = 0;
          }
        }
        
        getMediaPlayer().setPosition(time);
//        System.out.println("isFinished() : " + getMediaPlayer().isPlaying() + " : " + getMediaPlayer().getPosition() + " : " + getMediaPlayer().getTime());
      }
    });
    
    put(KeyEvent.VK_S, new KeyHandler() {
      @Override
      public void keyPressed() {
        getMediaPlayer().stop();
        setBackground(true);
        if(activeScreen() == MediaSystem.VIDEO_PLAYING) {
          back();
        }
        else {
          mainGroup.repaint();
        }
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
  private final Thread windowToFrontThread;
  
  private abstract class KeyHandler {
    public abstract void keyPressed();
  }
  
  public Controller(final Player player, final AbstractFrame<?> parentFrame) {
    this.player = player;
    this.parentFrame = parentFrame;
    
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

    background = new ImageIcon("images/Media Center background.jpg").getImage();
    mainGroup = new VerticalGroup() {{
      setPainter(new Painter() {
        @Override
        public void paint(Graphics2D g, int width, int height) {
          if(backgroundActive) {
            g.drawImage(background, 0, 0, null);
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
    
    State state = stateCache.getState(navigationHistory.getKey());
    
    if(state != null) {
      state.apply();
    }
    
    parentFrame.validate();
    mainGroup.repaint();
    setDefaultFocus();
    parentFrame.toFront();
  }
  
  public void back() {
    View view = navigationHistory.back();
    
    if(view != null) {
      changeView(view);
    }
  }
  
  public void forward(View view) {
    if(!navigationHistory.isEmpty()) {
      stateCache.putState(navigationHistory.getKey(), navigationHistory.current().getScreen().getState());
    }
    
    navigationHistory.forward(view);
    
    changeView(view);
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

  public View cloneCurrentView() {
    return navigationHistory.current().copy();
  } 
}
