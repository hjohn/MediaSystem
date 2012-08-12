package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.dao.Casting;
import hs.mediasystem.framework.Media;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.MediaNode;
import hs.mediasystem.screens.StarRating;
import hs.mediasystem.util.CollectionPane;
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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class DetailPaneDecorator {
  private final ObjectProperty<MediaNode> mediaNode = new SimpleObjectProperty<>();
  public ObjectProperty<MediaNode> mediaNodeProperty() { return mediaNode; }

  protected final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(mediaNode, "dataMap", Media.class, "image");

  protected final AsyncImageProperty poster = new AsyncImageProperty();

  protected final StringProperty groupName = new SimpleStringProperty();
  protected final StringBinding title = MapBindings.selectString(mediaNode, "dataMap", Media.class, "title");
  protected final StringBinding subtitle = MapBindings.selectString(mediaNode, "dataMap", Media.class, "subtitle");
  protected final StringBinding releaseTime = MediaItemFormatter.releaseTimeBinding(mediaNode);
  protected final StringBinding plot = MapBindings.selectString(mediaNode, "dataMap", Media.class, "description");
  protected final DoubleBinding rating = MapBindings.selectDouble(mediaNode, "dataMap", Media.class, "rating");
  protected final IntegerBinding runtime = MapBindings.selectInteger(mediaNode, "dataMap", Media.class, "runtime");
  protected final ObjectBinding<ObservableList<Casting>> castings = MapBindings.select(mediaNode, "dataMap", Media.class, "castings");
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

  public DetailPaneDecorator() {
    poster.imageHandleProperty().bind(posterHandle);
  }

  public void decorate(CollectionPane collectionPane) {
    collectionPane.getStylesheets().add("select-media/detail-pane.css");
    collectionPane.getStyleClass().add("detail-pane");

    collectionPane.add("primary", 1, new Label() {{
      getStyleClass().add("group-name");
      textProperty().bind(groupName);
      managedProperty().bind(groupName.isNotEqualTo(""));
      visibleProperty().bind(groupName.isNotEqualTo(""));
    }});

    collectionPane.add("primary", 2, new Label() {{
      getStyleClass().add("title");
      textProperty().bind(title);
    }});

    collectionPane.add("primary", 3, createSubtitleField());
    collectionPane.add("primary", 4, createRating());
    collectionPane.add("primary", 5, createGenresField());

    ScaledImageView image = new ScaledImageView() {{
      getStyleClass().add("poster-image");
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
      setAlignment(Pos.TOP_CENTER);
    }};
    VBox.setVgrow(image, Priority.ALWAYS);

    collectionPane.add("primary", 6, image);

    collectionPane.add("secondary", 1, createPlotBlock());

    collectionPane.add("secondary", 2, createMiscelaneousFieldsBlock());

    Pane castingsRow = createTitledBlock("CAST", createCastingsRow(), null);
    HBox.setHgrow(castingsRow, Priority.ALWAYS);

    collectionPane.add("bottom", 1, castingsRow);
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

  protected Pane createCastingsRow() {
    final TilePane tilePane = new TilePane();

    tilePane.getStyleClass().add("castings-row");

    tilePane.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        createCastingChildren(tilePane, castings.get());
      }
    });

    castings.addListener(new ChangeListener<ObservableList<Casting>>() {
      private final ListChangeListener<Casting> listChangeListener = new ListChangeListener<Casting>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends Casting> c) {
          createCastingChildren(tilePane, c.getList());
        }
      };

      @Override
      public void changed(ObservableValue<? extends ObservableList<Casting>> observable, ObservableList<Casting> old, ObservableList<Casting> current) {
        if(old != null) {
          old.removeListener(listChangeListener);
        }

        if(current != null) {
          createCastingChildren(tilePane, current);

          current.addListener(listChangeListener);
        }
      }
    });

    return tilePane;
  }

  protected void createCastingChildren(TilePane parent, ObservableList<? extends Casting> castings) {
    parent.getChildren().clear();

    double castingSize = 100 + parent.getHgap();

    if(castings != null) {
      double space = parent.getWidth() - castingSize;

      for(final Casting casting : castings) {
        if(space < 0) {
          break;
        }

        if(casting.getRole().equals("Actor")) {
          parent.getChildren().add(new VBox() {{
            ScaledImageView imageView = new ScaledImageView(new Label("?"));

            Label label = new Label();

            AsyncImageProperty photo = new AsyncImageProperty();

            if(casting.getPerson().getPhoto() != null) {
              photo.imageHandleProperty().set(new SourceImageHandle(casting.getPerson().getPhoto(), "StandardDetailPane://" + casting.getPerson().getName()));
            }

            imageView.getStyleClass().add("cast-photo");
            imageView.imageProperty().bind(photo);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(true);
            imageView.setMinHeight(122);
            imageView.setAlignment(Pos.CENTER);

            label.getStyleClass().add("cast-name");
            label.setText(casting.getPerson().getName());
            label.setMinWidth(100);
            label.setMaxWidth(100);

            getChildren().addAll(imageView, label);

            if(casting.getCharacterName() != null && !casting.getCharacterName().trim().isEmpty()) {
              Label characterNameLabel = new Label();

              characterNameLabel.getStyleClass().add("cast-character-name");
              characterNameLabel.setText("as " + formattedCharacterName(casting.getCharacterName(), 40));
              characterNameLabel.setMinWidth(100);
              characterNameLabel.setMaxWidth(100);

              getChildren().add(characterNameLabel);
            }
          }});

          space -= castingSize;
        }
      }
    }
  }

  private String formattedCharacterName(String rawCharacterName, int cutOffLength) {
    String characterName = rawCharacterName.replaceAll(" / ", "|");
    int more = 0;

    for(;;) {
      int index = characterName.lastIndexOf('|');

      if(index == -1) {
        return characterName;
      }

      if(index > cutOffLength) { // too long, cut it off
        characterName = characterName.substring(0, index).trim();
        more++;
      }
      else {
        if(more == 0) {
          characterName = characterName.substring(0, index).trim() + " & " + characterName.substring(index + 1).trim();
        }
        else {
          characterName = characterName.substring(0, index).trim() + " (" + (more + 1) + "\u00a0more)";
        }
        return characterName.replaceAll(" *\\| *", ", ");
      }
    }
  }
}