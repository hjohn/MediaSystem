package hs.mediasystem.test;

import javafx.application.Application;
import javafx.stage.Stage;

import org.junit.BeforeClass;

public class JavaFXTestCase {

  @BeforeClass
  public static void beforeClass() {
    new Thread() {
      @Override
      public void run() {
        Application.launch(FXRunnerApplication.class);
      }
    }.start();
  }

  public static class FXRunnerApplication extends Application {
    @Override
    public void start(Stage paramStage) throws Exception {
    }
  }

  protected void sleep(int millis) {
    try {
      Thread.sleep(millis);
    }
    catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
