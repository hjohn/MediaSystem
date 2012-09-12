package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Person;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;

import java.util.List;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
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
  protected final ObjectBinding<ObservableList<Casting>> castings = MapBindings.select(dataProperty(), "castings");

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
        createCastingChildren(tilePane, castings.get());
      }
    });

    castings.addListener(new ChangeListener<ObservableList<Casting>>() {
      @Override
      public void changed(ObservableValue<? extends ObservableList<Casting>> observable, ObservableList<Casting> old, ObservableList<Casting> current) {
        if(current != null) {
          createCastingChildren(tilePane, current);
        }
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
              System.out.println(">>> Clicked on " + casting.media.get().title.get());
              decoratablePane.decoratorContentProperty().set(casting.media.get());
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

      if(casting.media.get().image.get() != null) {
        photo.imageHandleProperty().set(casting.media.get().image.get());
      }

      imageView.getStyleClass().add("cast-photo");
      imageView.imageProperty().bind(photo);
      imageView.setSmooth(true);
      imageView.setPreserveRatio(true);
      imageView.setMinHeight(122);
      imageView.setAlignment(Pos.CENTER);

      label.getStyleClass().add("cast-name");
      label.setText(casting.media.get().titleWithContext.get());
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
