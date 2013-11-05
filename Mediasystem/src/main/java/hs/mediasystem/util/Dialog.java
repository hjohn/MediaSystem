package hs.mediasystem.util;

import javafx.stage.Stage;

public interface Dialog<R> {
  R showDialog(Stage parentStage, boolean synchronous);
}
