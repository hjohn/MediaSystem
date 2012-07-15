package hs.mediasystem.util;

import javafx.stage.Stage;

public interface Dialog {
  void showDialog(Stage parentStage, boolean synchronous);
}
