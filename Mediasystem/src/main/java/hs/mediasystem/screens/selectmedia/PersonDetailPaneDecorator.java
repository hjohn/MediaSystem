package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Person;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.selectmedia.CastingsRow.Type;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;

import java.util.Date;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

public class PersonDetailPaneDecorator implements DetailPaneDecorator<Person> {
  private final ObjectProperty<Person> data = new SimpleObjectProperty<>();
  @Override public ObjectProperty<Person> dataProperty() { return data; }

  protected final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(dataProperty(), "photo");
  protected final AsyncImageProperty poster = new AsyncImageProperty();

  protected final ObjectBinding<ObservableList<Casting>> castings = MapBindings.select(dataProperty(), "castings");

  protected final StringBinding birthPlace = MapBindings.selectString(dataProperty(), "birthPlace");
  protected final StringBinding biography = MapBindings.selectString(dataProperty(), "biography");
  protected final ObjectBinding<Date> birthDate = MapBindings.select(dataProperty(), "birthDate");

  protected final DetailPane.DecoratablePane decoratablePane;

  public PersonDetailPaneDecorator(DetailPane.DecoratablePane decoratablePane) {
    this.decoratablePane = decoratablePane;
    poster.imageHandleProperty().bind(posterHandle);
  }

  @Override
  public void decorate(boolean interactive) {
    decoratablePane.getStylesheets().add("select-media/person-detail-pane.css");

    decoratablePane.add("title-area", 2, new Label() {{
      getStyleClass().add("title");
      textProperty().bind(dataProperty().get().name);
    }});

    ScaledImageView image = new ScaledImageView() {{
      getStyleClass().add("poster-image");
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
      setAlignment(Pos.TOP_CENTER);
    }};
    VBox.setVgrow(image, Priority.ALWAYS);

    decoratablePane.add("title-image-area", 1, image);

    decoratablePane.add("description-area", 5, createMiscelaneousFieldsBlock());
    decoratablePane.add("description-area", 6, createBiographyBlock());

    CastingsRow castingsRow = createCastingsRow(interactive);
    Pane titledCastingsRow = createTitledBlock("APPEARANCES", castingsRow, castingsRow.empty.not());
    HBox.setHgrow(titledCastingsRow, Priority.ALWAYS);

    decoratablePane.add("link-area", 1, titledCastingsRow);
  }

  protected Pane createBiographyBlock() {
    Node biographyField = createBiographyField();
    VBox.setVgrow(biographyField, Priority.ALWAYS);

    return createTitledBlock("BIOGRAPHY", biographyField, biography.isNotEqualTo(""));
  }

  protected Node createBiographyField() {
    return new Label() {{
      getStyleClass().addAll("field", "biography");
      textProperty().bind(biography);
    }};
  }

  protected Pane createBirthDateBlock() {
    Label label = new Label() {{
      getStyleClass().addAll("field", "birthdate");
      textProperty().bind(MediaItemFormatter.formattedDate(birthDate));
    }};

    return createTitledBlock("BIRTH DATE", label, birthDate.isNotNull());
  }

  protected Pane createBirthPlaceBlock() {
    Label label = new Label() {{
      getStyleClass().addAll("field", "birthplace");
      textProperty().bind(birthPlace);
    }};

    return createTitledBlock("BIRTH PLACE", label, birthPlace.isNotEqualTo(""));
  }

  protected Pane createMiscelaneousFieldsBlock() {
    return new FlowPane() {{
      getStyleClass().add("fields");
      getChildren().add(createBirthDateBlock());
      getChildren().add(createBirthPlaceBlock());
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

  protected CastingsRow createCastingsRow(boolean interactive) {
    CastingsRow castingsRow = new CastingsRow(Type.APPEAREANCES, interactive);

    castingsRow.castings.bind(castings);
    castingsRow.onCastingSelected.set(new EventHandler<CastingSelectedEvent>() {
      @Override
      public void handle(CastingSelectedEvent event) {
        decoratablePane.decoratorContentProperty().set(event.getCasting().media.get());
      }
    });

    return castingsRow;
  }
}
