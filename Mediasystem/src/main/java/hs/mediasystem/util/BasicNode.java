package hs.mediasystem.util;

import javafx.collections.ObservableList;

public interface BasicNode {
  ObservableList<String> getStylesheets();
  ObservableList<String> getStyleClass();
  void requestFocus();
}
