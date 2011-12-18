package hs.mediasystem.screens;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class MyCellFactoryCallBack implements Callback<ListView<Option>, ListCell<Option>> {

  @Override
  public ListCell<Option> call(ListView<Option> listView) {
    return new ListCell<Option>() {
      @Override
      protected void updateItem(Option item, boolean empty) {
        if(item != null) {
          setGraphic(item.getControl());
        }
      }
    };
  }

}
