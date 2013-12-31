package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Person;
import hs.mediasystem.screens.AreaLayout;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.collection.detail.CastingsRow.Type;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;

import java.time.LocalDate;

import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
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

/**
 * DetailPane for showing information about a Person.
 */
public class PersonDetailPane extends DetailPane<Person> {
  protected final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(content, "photo");
  protected final AsyncImageProperty poster = new AsyncImageProperty();

  protected final ObjectBinding<ObservableList<Casting>> castings = MapBindings.select(content, "castings");

  protected final StringBinding birthPlace = MapBindings.selectString(content, "birthPlace");
  protected final StringBinding biography = MapBindings.selectString(content, "biography");
  protected final ObjectBinding<LocalDate> birthDate = MapBindings.select(content, "birthDate");

  public static PersonDetailPane create(AreaLayout areaLayout, boolean interactive) {
    PersonDetailPane pane = new PersonDetailPane(areaLayout);

    pane.postConstruct(interactive);

    return pane;
  }

  protected PersonDetailPane(AreaLayout areaLayout) {
    super(areaLayout);

    poster.imageHandleProperty().bind(posterHandle);
  }

  protected void postConstruct(boolean interactive) {
    getStylesheets().add("collection/person-detail-pane.css");

    add("title-area", 2, new Label() {{
      getStyleClass().add("title");
      textProperty().bind(MapBindings.selectString(content, "name"));
    }});

    ScaledImageView image = new ScaledImageView() {{
      getStyleClass().add("poster-image");
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
      setAlignment(Pos.TOP_CENTER);
    }};
    VBox.setVgrow(image, Priority.ALWAYS);

    add("title-image-area", 1, image);

    add("description-area", 5, createMiscelaneousFieldsBlock());
    add("description-area", 6, createBiographyBlock());

    CastingsRow castingsRow = createCastingsRow(interactive);
    Pane titledCastingsRow = createTitledBlock("APPEARANCES", castingsRow, castingsRow.empty.not());
    HBox.setHgrow(titledCastingsRow, Priority.ALWAYS);

    add("link-area", 1, titledCastingsRow);
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
      textProperty().bind(MediaItemFormatter.formattedLocalDate(birthDate));
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
    CastingsRow castingsRow = new CastingsRow(Type.APPEARANCES, interactive);

    castingsRow.castings.bind(castings);
    castingsRow.onCastingSelected.set(new EventHandler<CastingSelectedEvent>() {
      @Override
      public void handle(CastingSelectedEvent event) {
        Events.dispatchEvent(onNavigate, new DetailNavigationEvent(event.getCasting().media.get()), event);
      }
    });

    return castingsRow;
  }
}