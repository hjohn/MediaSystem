package hs.mediasystem.screens.optiondialog;

import hs.mediasystem.util.PathToFullPathConverter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import javax.inject.Provider;

public class PathListOption extends OptionGroup {

  public PathListOption(String description, final ObservableList<Path> paths) {
    super(description, new Provider<List<Option>>() {
      @Override
      public List<Option> get() {
        List<Option> options = new ArrayList<>();

        options.add(new OptionGroup("Add folder", new Provider<List<Option>>() {
          @Override
          public List<Option> get() {
            List<Option> options = new ArrayList<>();
            SimpleObjectProperty<Path> selectedPath = new SimpleObjectProperty<>();

            selectedPath.addListener(new ChangeListener<Path>() {
              @Override
              public void changed(ObservableValue<? extends Path> observable, Path old, Path current) {
                if(!paths.contains(current)) {
                  paths.add(current);
                }
              }
            });

            options.add(new PathSelectOption("Select folder", selectedPath, PathSelectOption.ONLY_DIRECTORIES_FILTER));

            return options;
          }
        }));

        SimpleObjectProperty<Path> folderToDelete = new SimpleObjectProperty<>();

        folderToDelete.addListener(new ChangeListener<Path>() {
          @Override
          public void changed(ObservableValue<? extends Path> observable, Path old, Path current) {
            paths.remove(current);
          }
        });

        options.add(new ListViewOption<Path>("Selected folders", folderToDelete, paths, new PathToFullPathConverter()) {{
          bottomLabel.setText("Select to remove a folder");
        }});

        return options;
      }
    });
  }
}
