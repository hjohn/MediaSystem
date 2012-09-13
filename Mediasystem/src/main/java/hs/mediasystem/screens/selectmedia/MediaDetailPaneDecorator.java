package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.StarRating;
import hs.mediasystem.screens.selectmedia.CastingsRow.Type;
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
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MediaDetailPaneDecorator implements DetailPaneDecorator<Media<?>> {
  private final ObjectProperty<Media<?>> data = new SimpleObjectProperty<>();
  @Override public ObjectProperty<Media<?>> dataProperty() { return data; }

  protected final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(dataProperty(), "image");

  protected final AsyncImageProperty poster = new AsyncImageProperty();

  protected final StringProperty groupName = new SimpleStringProperty();
  protected final StringBinding title = MapBindings.selectString(dataProperty(), "title");
  protected final StringBinding subtitle = MapBindings.selectString(dataProperty(), "subtitle");
  protected final StringBinding releaseTime = MediaItemFormatter.releaseTimeBinding(dataProperty());
  protected final StringBinding plot = MapBindings.selectString(dataProperty(), "description");
  protected final DoubleBinding rating = MapBindings.selectDouble(dataProperty(), "rating");
  protected final IntegerBinding runtime = MapBindings.selectInteger(dataProperty(), "runtime");
  protected final ObjectBinding<ObservableList<Casting>> castings = MapBindings.select(dataProperty(), "castings");
  protected final StringBinding genres = new StringBinding() {
    final ObjectBinding<String[]> selectGenres = MapBindings.select(dataProperty(), "genres");

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

  protected final DetailPane.DecoratablePane decoratablePane;

  public MediaDetailPaneDecorator(DetailPane.DecoratablePane decoratablePane) {
    this.decoratablePane = decoratablePane;
    poster.imageHandleProperty().bind(posterHandle);
  }

  @Override
  public void decorate() {
    decoratablePane.getStylesheets().add("select-media/media-detail-pane.css");

    decoratablePane.add("title-area", 1, new Label() {{
      getStyleClass().add("group-name");
      textProperty().bind(groupName);
      managedProperty().bind(groupName.isNotEqualTo(""));
      visibleProperty().bind(groupName.isNotEqualTo(""));
    }});

    decoratablePane.add("title-area", 2, new Label() {{
      getStyleClass().add("title");
      textProperty().bind(title);
    }});

    decoratablePane.add("title-area", 3, createSubtitleField());
    decoratablePane.add("title-area", 4, createRating());
    decoratablePane.add("title-area", 5, createGenresField());

    ScaledImageView image = new ScaledImageView() {{
      getStyleClass().add("poster-image");
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
      setAlignment(Pos.TOP_CENTER);
    }};
    VBox.setVgrow(image, Priority.ALWAYS);

    decoratablePane.add("title-image-area", 1, image);

    decoratablePane.add("description-area", 6, createPlotBlock());
    decoratablePane.add("description-area", 7, createMiscelaneousFieldsBlock());

    CastingsRow castingsRow = createCastingsRow();
    Pane titledCastingsRow = createTitledBlock("CAST", castingsRow, castingsRow.empty.not());
    HBox.setHgrow(titledCastingsRow, Priority.ALWAYS);

    decoratablePane.add("link-area", 1, titledCastingsRow);
  }

  protected Node createSubtitleField() {
    return new Label() {{
      getStyleClass().add("subtitle");
      textProperty().bind(subtitle);
      managedProperty().bind(textProperty().isNotEqualTo(""));
    }};
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

  protected Node createGenresField() {
    return new Label() {{
      getStyleClass().add("genres");
      textProperty().bind(genres);
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

  protected Pane createMiscelaneousFieldsBlock() {
    return new FlowPane() {{
      getStyleClass().add("fields");
      getChildren().add(createReleaseDateBlock());
      getChildren().add(createRuntimeBlock());
    }};
  }

  protected Pane createPlotBlock() {
    Node plotField = createPlotField();
    VBox.setVgrow(plotField, Priority.ALWAYS);

    return createTitledBlock("PLOT", plotField, plot.isNotEqualTo(""));
  }

  protected Node createPlotField() {
    return new Label() {{
      getStyleClass().addAll("field", "plot");
      textProperty().bind(plot);
      managedProperty().bind(plot.isNotEqualTo(""));
      visibleProperty().bind(plot.isNotEqualTo(""));
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

  protected CastingsRow createCastingsRow() {
    CastingsRow castingsRow = new CastingsRow(Type.CAST);

    castingsRow.castings.bind(castings);
    castingsRow.onCastingSelected.set(new EventHandler<CastingSelectedEvent>() {
      @Override
      public void handle(CastingSelectedEvent event) {
        decoratablePane.decoratorContentProperty().set(event.getCasting().person.get());
      }
    });

    return castingsRow;
  }
}
