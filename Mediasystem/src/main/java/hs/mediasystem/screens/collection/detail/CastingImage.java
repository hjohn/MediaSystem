package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.ScaledImageView;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CastingImage extends Button {
  public final ObjectProperty<ImageHandle> image = new SimpleObjectProperty<>();
  public final StringProperty title = new SimpleStringProperty();
  public final StringProperty characterName = new SimpleStringProperty();

  private final StringBinding formattedCharacterName = new StringBinding() {
    {
      bind(characterName);
    }

    @Override
    protected String computeValue() {
      return characterName.get() == null ? null : "as " + formatCharacterName(characterName.get(), 40);
    }
  };

  public CastingImage() {
    getStyleClass().add("cast-item");

    VBox vbox = new VBox();
    ScaledImageView imageView = new ScaledImageView(new Label("?"));

    Label label = new Label();

    AsyncImageProperty photo = new AsyncImageProperty();

    photo.imageHandleProperty().bind(image);

    imageView.getStyleClass().add("cast-photo");
    imageView.imageProperty().bind(photo);
    imageView.setSmooth(true);
    imageView.setPreserveRatio(true);
    imageView.setMinHeight(122);
    imageView.setAlignment(Pos.CENTER);

    label.getStyleClass().add("cast-name");
    label.textProperty().bind(title);
    label.setMinWidth(100);
    label.setMaxWidth(100);

    vbox.getChildren().addAll(imageView, label);

    Label characterNameLabel = new Label();

    characterNameLabel.getStyleClass().add("cast-character-name");
    characterNameLabel.textProperty().bind(formattedCharacterName);
    characterNameLabel.setMinWidth(100);
    characterNameLabel.setMaxWidth(100);

    vbox.getChildren().add(characterNameLabel);

    BooleanBinding visibleBinding = characterName.isNotNull().and(characterName.isNotEqualTo(""));

    characterNameLabel.managedProperty().bind(visibleBinding);
    characterNameLabel.visibleProperty().bind(visibleBinding);

    setGraphic(vbox);
  }

  private String formatCharacterName(String rawCharacterName, int cutOffLength) {
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