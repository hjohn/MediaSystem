package hs.mediasystem.screens.optiondialog;

import hs.mediasystem.util.StringConverter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;

import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PathSelectOption extends ListViewOption<Path> {
  public static final Filter<Path> ONLY_DIRECTORIES_FILTER = new Filter<Path>() {
    @Override
    public boolean accept(Path entry) throws IOException {
      return Files.isDirectory(entry);
    }
  };

  public static final Filter<Path> ALLOW_ALL_FILTER = new Filter<Path>() {
    @Override
    public boolean accept(Path entry) throws IOException {
      return true;
    }
  };

  private static final Comparator<Path> DIRECTORY_COMPARATOR = new Comparator<Path>() {
    @Override
    public int compare(Path o1, Path o2) {
      int result = Boolean.compare(Files.isDirectory(o1), Files.isDirectory(o2));

      if(result == 0) {
        String name1 = o1.getFileName() == null ? o1.toString() : o1.getFileName().toString();
        String name2 = o2.getFileName() == null ? o2.toString() : o2.getFileName().toString();

        result = name1.compareTo(name2);
      }

      return result;
    }
  };

  private final Filter<Path> filter;

  private Path currentPath = null;

  public PathSelectOption(final String description, final ObjectProperty<Path> property, Filter<Path> filter) {
    super(description, property, new StringConverter<Path>() {
      @Override
      public String toString(Path object) {
        String name = object.getFileName() == null ? object.toString() : object.getFileName().toString();

        return Files.isDirectory(object) ? "[" + name + "]" : name;
      }
    });

    this.filter = filter == null ? ALLOW_ALL_FILTER : filter;

    populateView();
  }

  @Override
  public void left() {
    if(currentPath != null) {
      Path oldPath = currentPath;
      currentPath = currentPath.getParent();
      populateView();

      listView.getSelectionModel().select(oldPath);
    }
  }

  @Override
  public void right() {
    if(Files.isDirectory(listView.getFocusModel().getFocusedItem())) {
      currentPath = listView.getFocusModel().getFocusedItem();
      populateView();

      if(!listView.getItems().isEmpty()) {
        listView.getSelectionModel().select(0);
      }
    }
  }

  private void populateView() {
    ObservableList<Path> paths = FXCollections.observableArrayList();

    if(currentPath == null) {
      for(Path path : FileSystems.getDefault().getRootDirectories()) {
        paths.add(path);
      }
    }
    else {
      try(DirectoryStream<Path> stream = Files.newDirectoryStream(currentPath, filter)) {
        for(Path path : stream) {
          paths.add(path);
        }
      }
      catch(IOException e) {
        System.out.println("[WARN] Unable to read dir '" + currentPath + "': " + e);
        e.printStackTrace(System.out);
      }
    }

    Collections.sort(paths, DIRECTORY_COMPARATOR);

    listView.setItems(paths);
  }
}