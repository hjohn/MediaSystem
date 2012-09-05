package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.screens.Casting;
import hs.mediasystem.screens.Person;
import hs.mediasystem.screens.StarRating;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;

import java.util.List;

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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class PersonDetailPaneDecorator implements DetailPaneDecorator<Person> {
  private final ObjectProperty<Person> data = new SimpleObjectProperty<>();
  @Override public ObjectProperty<Person> dataProperty() { return data; }

  protected final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(dataProperty(), "image");

  protected final AsyncImageProperty poster = new AsyncImageProperty();

  protected final StringProperty groupName = new SimpleStringProperty();
//  protected final StringBinding name = MapBindings.selectString(dataProperty(), "name");
  protected final StringBinding subtitle = MapBindings.selectString(dataProperty(), "subtitle");
//  protected final StringBinding biography = MapBindings.selectString(dataProperty(), "biography");
  protected final DoubleBinding rating = MapBindings.selectDouble(dataProperty(), "rating");
  protected final IntegerBinding runtime = MapBindings.selectInteger(dataProperty(), "runtime");
  //protected final ObjectBinding<ObservableList<Casting>> castings = MapBindings.select(dataProperty(), "castings");
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

  public PersonDetailPaneDecorator(DetailPane.DecoratablePane decoratablePane) {
    this.decoratablePane = decoratablePane;
    //poster.imageHandleProperty().bind(posterHandle);
  }

  @Override
  public void decorate() {
    decoratablePane.getStylesheets().add("select-media/person-detail-pane.css");

    decoratablePane.add("title-area", 2, new Label() {{
      getStyleClass().add("title");
      textProperty().bind(dataProperty().get().name);
    }});

    if(dataProperty().get().photo.get() != null) {
      poster.imageHandleProperty().set(dataProperty().get().photo.get());
    }

    ScaledImageView image = new ScaledImageView() {{
      getStyleClass().add("poster-image");
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
      setAlignment(Pos.TOP_CENTER);
    }};
    VBox.setVgrow(image, Priority.ALWAYS);

    decoratablePane.add("title-image-area", 1, image);

    decoratablePane.add("description-area", 6, createBiographyBlock());

    Pane castingsRow = createTitledBlock("APPEARANCES", createCastingsRow(), null);
    HBox.setHgrow(castingsRow, Priority.ALWAYS);

    decoratablePane.add("link-area", 1, castingsRow);
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

  protected Pane createRuntimeBlock() {
    Label label = new Label() {{
      getStyleClass().addAll("field", "runtime");
      textProperty().bind(Bindings.format("%d minutes", runtime));
    }};

    return createTitledBlock("RUNTIME", label, runtime.greaterThan(0.0));
  }

  protected Pane createBiographyBlock() {
    Node plotField = createBiographyField();
    VBox.setVgrow(plotField, Priority.ALWAYS);

    return createTitledBlock("BIOGRAPHY", plotField, dataProperty().get().biography.isNotEqualTo(""));
  }

  protected Node createBiographyField() {
    return new Label() {{
      getStyleClass().addAll("field", "plot");
      textProperty().bind(dataProperty().get().biography);
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
        createCastingChildren(tilePane, dataProperty().get().castings.get());
      }
    });

    dataProperty().addListener(new ChangeListener<Person>() {
      @Override
      public void changed(ObservableValue<? extends Person> observable, Person old, Person current) {
        createCastingChildren(tilePane, current.castings.get());
      }
    });

    return tilePane;
  }

  protected void createCastingChildren(TilePane parent, List<? extends Casting> castings) {
    parent.getChildren().clear();

    double castingSize = 100 + parent.getHgap();

    if(castings != null) {
      double space = parent.getWidth() - castingSize;

      for(final Casting casting : castings) {
        if(space < 0) {
          break;
        }

        if(casting.role.get().equals("Actor")) {
          CastingImage castingImage = new CastingImage(casting);

          castingImage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
              //decoratablePane.decoratorContentProperty().set(casting.getItem());
            }
          });

          parent.getChildren().add(castingImage);

          space -= castingSize;
        }
      }
    }
  }

  private static final class CastingImage extends Button {
    public CastingImage(Casting casting) {
      getStyleClass().add("cast-item");

      VBox vbox = new VBox();
      ScaledImageView imageView = new ScaledImageView(new Label("?"));

      Label label = new Label();

      AsyncImageProperty photo = new AsyncImageProperty();

      if(casting.media.get().getImage() != null) {
        photo.imageHandleProperty().set(casting.media.get().getImage());
      }

      imageView.getStyleClass().add("cast-photo");
      imageView.imageProperty().bind(photo);
      imageView.setSmooth(true);
      imageView.setPreserveRatio(true);
      imageView.setMinHeight(122);
      imageView.setAlignment(Pos.CENTER);

      label.getStyleClass().add("cast-name");
      label.setText(casting.media.get().getTitle());
      label.setMinWidth(100);
      label.setMaxWidth(100);

      vbox.getChildren().addAll(imageView, label);

      if(casting.characterName.get() != null && !casting.characterName.get().trim().isEmpty()) {
        Label characterNameLabel = new Label();

        characterNameLabel.getStyleClass().add("cast-character-name");
        characterNameLabel.setText("as " + formattedCharacterName(casting.characterName.get(), 40));
        characterNameLabel.setMinWidth(100);
        characterNameLabel.setMaxWidth(100);

        vbox.getChildren().add(characterNameLabel);
      }

      setGraphic(vbox);
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
}
