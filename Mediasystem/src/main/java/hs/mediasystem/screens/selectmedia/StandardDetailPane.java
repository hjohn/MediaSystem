package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.StarRating;
import hs.mediasystem.util.GridPaneUtil;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class StandardDetailPane extends StackPane implements DetailPane {
  private final ObjectProperty<MediaNode> mediaNode = new SimpleObjectProperty<>();
  @Override public ObjectProperty<MediaNode> mediaNodeProperty() { return mediaNode; }

  protected final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(mediaNode, "dataMap", Media.class, "image");
  protected final AsyncImageProperty poster = new AsyncImageProperty();

  protected final StringProperty groupName = new SimpleStringProperty();
  protected final StringBinding title = MapBindings.selectString(mediaNode, "dataMap", Media.class, "title");
  protected final StringBinding subtitle = MapBindings.selectString(mediaNode, "dataMap", Media.class, "subtitle");
  protected final StringBinding releaseTime = MediaItemFormatter.releaseTimeBinding(mediaNode);
  protected final StringBinding plot = MapBindings.selectString(mediaNode, "dataMap", Media.class, "description");
  protected final DoubleBinding rating = MapBindings.selectDouble(mediaNode, "dataMap", Media.class, "rating");
  protected final IntegerBinding runtime = MapBindings.selectInteger(mediaNode, "dataMap", Media.class, "runtime");
  protected final StringBinding genres = new StringBinding() {
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
    getStyleClass().add("detail-pane");

    poster.imageHandleProperty().bind(posterHandle);

    sceneProperty().addListener(new ChangeListener<Scene>() {
      private boolean initialized;

      @Override
      public void changed(ObservableValue<? extends Scene> observable, Scene oldValue, Scene newValue) {
        if(!initialized) {
          initialized = true;
          getChildren().add(createContent());
        }
      }
    });
  }



  protected Node createContent() {
    BorderPane borderPane = new BorderPane();

    GridPane grid = GridPaneUtil.create(new double[] {60, 40}, new double[] {100});

    grid.getStyleClass().add("content-grid");

    borderPane.setTop(createTitleBlock());
    borderPane.setCenter(grid);

    Node image = createImage(Pos.BOTTOM_CENTER);

    grid.add(createInfoBlock(), 0, 0);
    grid.add(image, 1, 0);

    GridPane.setValignment(image, VPos.BOTTOM);
    GridPane.setVgrow(image, Priority.ALWAYS);

    return borderPane;
  }

  protected Node createImage(final Pos alignment) {
    return new ScaledImageView() {{
      getStyleClass().add("poster-image");
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
      setAlignment(alignment);
    }};
  }

  protected Node createGroupNameField() {
    return new Label() {{
      getStyleClass().add("group-name");
      textProperty().bind(groupName);
      managedProperty().bind(groupName.isNotEqualTo(""));
      visibleProperty().bind(groupName.isNotEqualTo(""));
    }};
  }

  protected Node createTitleField() {
    return new Label() {{
      getStyleClass().add("title");
      textProperty().bind(title);
    }};
  }

  protected Node createPlotField() {
    return new Label() {{
      getStyleClass().addAll("field", "plot");
      textProperty().bind(plot);
      managedProperty().bind(plot.isNotEqualTo(""));
      visibleProperty().bind(plot.isNotEqualTo(""));
    }};
  }

  protected Node createGenresField() {
    return new Label() {{
      getStyleClass().add("genres");
      textProperty().bind(genres);
      managedProperty().bind(textProperty().isNotEqualTo(""));
    }};
  }

  protected Node createSubtitleField() {
    return new Label() {{
      getStyleClass().add("subtitle");
      textProperty().bind(subtitle);
      managedProperty().bind(textProperty().isNotEqualTo(""));
    }};
  }

  protected Pane createReleaseDateBlock() {
    Label label = new Label() {{
      getStyleClass().addAll("field", "release-time");
      textProperty().bind(releaseTime);
    }};

    return createTitledBlock("RELEASED", label, releaseTime.isNotEqualTo(""));
  }

  protected Pane createRuntimeBlock() {
    Label label = new Label() {{
      getStyleClass().addAll("field", "runtime");
      textProperty().bind(Bindings.format("%d minutes", runtime));
    }};

    return createTitledBlock("RUNTIME", label, runtime.greaterThan(0.0));
  }

  protected Node createRating() {
    return new HBox() {{
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
    }};
  }

  protected Pane createTitleBlock() {
    return new VBox() {{
      getChildren().add(createGroupNameField());
      getChildren().add(createTitleField());
      getChildren().add(createSubtitleField());
      getChildren().add(createRating());
      getChildren().add(createGenresField());
    }};
  }

  protected Pane createPlotBlock() {
    Node plotField = createPlotField();
    VBox.setVgrow(plotField, Priority.ALWAYS);

    return createTitledBlock("PLOT", plotField, plot.isNotEqualTo(""));
  }

  protected Pane createInfoBlock() {
    return new VBox() {{
      getChildren().add(createPlotBlock());
      getChildren().add(createMiscelaneousFieldsBlock());
    }};
  }

  protected Pane createMiscelaneousFieldsBlock() {
    return new FlowPane() {{
      getStyleClass().add("fields");
      getChildren().add(createReleaseDateBlock());
      getChildren().add(createRuntimeBlock());
    }};
  }

  protected Pane createTitledBlock(final String title, final Node content, final BooleanExpression visibleCondition) {
    return new VBox() {{
      setFillWidth(true);
      getChildren().add(new Label(title) {{
        getStyleClass().add("header");
      }});
      getChildren().add(content);

      if(visibleCondition != null) {
        managedProperty().bind(visibleCondition);
        visibleProperty().bind(visibleCondition);
      }
    }};
  }
}
