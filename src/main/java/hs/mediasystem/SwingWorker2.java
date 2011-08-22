package hs.mediasystem;

import javax.swing.SwingUtilities;

public class SwingWorker2 {
  private Thread task;
  
  public void doTask(final long initialDelayMillis, final Worker worker) {
    if(task != null) {
      task.interrupt();
    }
    
    task = new Thread() {
      @Override
      public void run() {
        try {
          System.out.println("SwingWorker2: waiting for initial delay to expire");
          Thread.sleep(initialDelayMillis);
          System.out.println("SwingWorker2: executing task");
          worker.doInBackground();
          
          if(isInterrupted()) {
            return;
          }
          
          System.out.println("SwingWorker2: updating UI");
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              worker.done();
            }
          });
        }
        catch(InterruptedException e) {
          // do nothing
        }
      }
    };
    
    task.start();
  }
}
