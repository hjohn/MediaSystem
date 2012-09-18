package hs.mediasystem.screens.optiondialog;

import hs.mediasystem.util.PathStringConverter;

import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;

import javax.inject.Provider;

public class PathOption extends OptionGroup {

  public PathOption(String description, final ObjectProperty<Path> path, final Filter<Path> filter) {
    super(description, new Provider<List<Option>>() {
      @Override
      public List<Option> get() {
        List<Option> options = new ArrayList<>();

        options.add(new PathSelectOption("Select folder", path, filter));

        return options;
      }
    });

    label.textProperty().bind(new StringBinding() {
      private PathStringConverter converter = new PathStringConverter();
      {
        bind(path);
      }

      @Override
      protected String computeValue() {
        return converter.toString(path.get());
      }
    });
  }
}
