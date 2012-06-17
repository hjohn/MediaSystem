package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Episode;
import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.StarRating;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

public class StandardDetailPane extends GridPane implements DetailPane {
  private final ObjectProperty<MediaNode> mediaNode = new SimpleObjectProperty<>();
  @Override public ObjectProperty<MediaNode> mediaNodeProperty() { return mediaNode; }

  private final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(mediaNode, "dataMap", Media.class, "image");
  private final AsyncImageProperty poster = new AsyncImageProperty();

  private final StringBinding groupName = MapBindings.selectString(mediaNode, "dataMap", Episode.class, "serie", "dataMap", Media.class, "title");
  private final StringBinding title = MapBindings.selectString(mediaNode, "dataMap", Media.class, "title");
  private final StringBinding subtitle = MapBindings.selectString(mediaNode, "dataMap", Media.class, "subtitle");
  private final StringBinding releaseTime = MediaItemFormatter.releaseTimeBinding(mediaNode);
  private final StringBinding plot = MapBindings.selectString(mediaNode, "dataMap", Media.class, "description");
  private final DoubleBinding rating = MapBindings.selectDouble(mediaNode, "dataMap", Media.class, "rating");
  private final IntegerBinding runtime = MapBindings.selectInteger(mediaNode, "dataMap", Media.class, "runtime");
  private final StringBinding genres = new StringBinding() {
    final ObjectBinding<String[]> selectGenres = MapBindings.select(mediaNode, "dataMap", Media.class, "genres");

    {
      bind(selectGenres);
    }

    @Override
    protected String computeValue() {
      String genreText = "";
      String[] genres = selectGenres.get();

      if(genres != null) {
        for(String genre : genres) {
          if(!genreText.isEmpty()) {
            genreText += " • ";
          }

          genreText += genre;
        }
      }

      return genreText;
    }
  };
  public StandardDetailPane() {
    getStylesheets().add("select-media/detail-pane.css");

    poster.imageHandleProperty().bind(posterHandle);

    getColumnConstraints().addAll(
      new ColumnConstraints() {{
        setPercentWidth(50);
      }},
      new ColumnConstraints() {{
        setPercentWidth(50);
      }}
    );

    getRowConstraints().addAll(
      new RowConstraints() {{
        setPercentHeight(100);
      }}
    );

    add(new ScaledImageView() {{
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
    }}, 0, 0);
    add(new BorderPane() {{
      setTop(new VBox() {{
        visibleProperty().bind(title.isNotNull());
        getChildren().add(new Label() {{
          getStyleClass().add("group-name");
          textProperty().bind(groupName);
          managedProperty().bind(groupName.isNotEqualTo(""));
          visibleProperty().bind(groupName.isNotEqualTo(""));
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("title");
          textProperty().bind(title);
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("subtitle");
          textProperty().bind(subtitle);
          managedProperty().bind(textProperty().isNotEqualTo(""));
        }});
        getChildren().add(new HBox() {{
          setAlignment(Pos.CENTER_LEFT);
          getChildren().add(new StarRating(12, 5, 5) {{
            ratingProperty().bind(rating.divide(10));
          }});
          getChildren().add(new Label() {{
            getStyleClass().add("rating");
            textProperty().bind(Bindings.format("%3.1f/10", rating));
          }});
          managedProperty().bind(rating.greaterThan(0.0));
          visibleProperty().bind(rating.greaterThan(0.0));
        }});
        getChildren().add(new Label() {{
          getStyleClass().add("genres");
          textProperty().bind(genres);
          managedProperty().bind(textProperty().isNotEqualTo(""));
        }});
        getChildren().add(new Label("PLOT") {{
          getStyleClass().add("header");
          managedProperty().bind(plot.isNotEqualTo(""));
          visibleProperty().bind(plot.isNotEqualTo(""));
        }});
        getChildren().add(new Label() {{
          getStyleClass().addAll("field", "plot");
          textProperty().bind(plot);
          managedProperty().bind(plot.isNotEqualTo(""));
          visibleProperty().bind(plot.isNotEqualTo(""));
          VBox.setVgrow(this, Priority.ALWAYS);
        }});
        getChildren().add(new FlowPane() {{
          getStyleClass().add("fields");
          getChildren().add(new VBox() {{
            getChildren().add(new Label("RELEASED") {{
              getStyleClass().add("header");
            }});
            getChildren().add(new Label() {{
              getStyleClass().addAll("field", "release-time");
              textProperty().bind(releaseTime);
            }});
            managedProperty().bind(releaseTime.isNotEqualTo(""));
            visibleProperty().bind(releaseTime.isNotEqualTo(""));
          }});
          getChildren().add(new VBox() {{
            getChildren().add(new Label("RUNTIME") {{
              getStyleClass().add("header");
            }});
            getChildren().add(new Label() {{
              getStyleClass().addAll("field", "runtime");
              textProperty().bind(Bindings.format("%d minutes", runtime));
            }});
            managedProperty().bind(runtime.greaterThan(0.0));
            visibleProperty().bind(runtime.greaterThan(0.0));
          }});
        }});
      }});
    }}, 1, 0);
  }
}
