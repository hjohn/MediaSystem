package hs.mediasystem.screens.collection;

import hs.mediasystem.controls.TablePane;

import java.time.LocalDate;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;

public class FilterPane extends BorderPane {
  public static final String[] GENRES = {"Action", "Adventure", "Animation", "Crime", "Comedy", "Drama", "Family", "Fantasy", "Horror", "Romance", "Science Fiction", "Thriller", "Western"};

  public final BooleanProperty includeViewed = new SimpleBooleanProperty(true);
  public final BooleanProperty includeNotViewed = new SimpleBooleanProperty(true);
  public final ObservableSet<String> matchGenres = FXCollections.observableSet();
  public final ObjectProperty<ReleaseFilterMode> releaseFilterMode = new SimpleObjectProperty<>(ReleaseFilterMode.ANYTIME);
  public final ObjectProperty<Integer> year1 = new SimpleObjectProperty<>();
  public final ObjectProperty<Integer> year2 = new SimpleObjectProperty<>();

  private final ObservableList<ReleaseFilterMode> releaseFilterModes = FXCollections.observableArrayList(ReleaseFilterMode.values());

  private final ObservableList<Integer> years = FXCollections.observableArrayList();
  private final BooleanProperty year1Visible = new SimpleBooleanProperty();
  private final BooleanProperty year2Visible = new SimpleBooleanProperty();

  public enum ReleaseFilterMode {
    ANYTIME(0),
    IN(1),
    BEFORE(1),
    AFTER(1),
    BETWEEN(2);

    private final int parameterCount;

    ReleaseFilterMode(int parameterCount) {
      this.parameterCount = parameterCount;
    }
  }

  private final InvalidationListener releaseInvalidationListener = new InvalidationListener() {
    @Override
    public void invalidated(Observable observable) {
      ReleaseFilterMode mode = releaseFilterMode.get();

      year1Visible.set(mode.parameterCount > 0);
      year2Visible.set(mode.parameterCount > 1);
    }
  };

  public FilterPane() {
    getStylesheets().add(getClass().getResource("FilterPane.css").toExternalForm());

    setTop(new Label("Filter") {{
      getStyleClass().add("title");
      BorderPane.setAlignment(this, Pos.CENTER);
    }});

    setCenter(new TablePane() {{
      getStyleClass().add("filters");

      add(new Label("Viewed"));
      add(new CheckBox() {{
        selectedProperty().bindBidirectional(includeViewed);
      }});
      nextRow();

      add(new Label("Not Viewed"));
      add(new CheckBox() {{
        selectedProperty().bindBidirectional(includeNotViewed);
      }});
      nextRow();

      add(new Label("Released"));
      add(new HBox() {{
        getStyleClass().add("released");
        getChildren().add(new ComboBox<ReleaseFilterMode>(releaseFilterModes) {{
          valueProperty().bindBidirectional(releaseFilterMode);
        }});
        getChildren().add(new ComboBox<Integer>(years) {{
          valueProperty().bindBidirectional(year1);
          visibleProperty().bind(year1Visible);
        }});
        getChildren().add(new ComboBox<Integer>(years) {{
          valueProperty().bindBidirectional(year2);
          visibleProperty().bind(year2Visible);
        }});
      }});
      nextRow();

      add(new Label("Genres"));
      add(new TilePane() {{
        getStyleClass().add("genres");
        setTileAlignment(Pos.CENTER_LEFT);
        for(String genre : GENRES) {
          getChildren().add(new CheckBox(genre) {{
            selectedProperty().addListener(new ChangeListener<Boolean>() {
              @Override
              public void changed(ObservableValue<? extends Boolean> observable, Boolean old, Boolean current) {
                if(current) {
                  matchGenres.add(genre);
                }
                else {
                  matchGenres.remove(genre);
                }
              }
            });

            matchGenres.addListener(new SetChangeListener<String>() {
              @Override
              public void onChanged(SetChangeListener.Change<? extends String> change) {
                setSelected(matchGenres.contains(genre));
              }
            });
          }});
        }
      }});
    }});

    for(int i = LocalDate.now().getYear(); i > 1900; i--) {
      years.add(i);
    }

    releaseFilterMode.addListener(releaseInvalidationListener);
  }
}
