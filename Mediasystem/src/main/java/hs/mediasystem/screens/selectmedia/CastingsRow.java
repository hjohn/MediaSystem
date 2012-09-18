package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.Casting;
import hs.mediasystem.util.Events;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.TilePane;

public class CastingsRow extends TilePane {
  public enum Type {
    CAST, APPEAREANCES;
  }

  public final ObjectProperty<ObservableList<Casting>> castings = new SimpleObjectProperty<>();
  public final ObjectProperty<EventHandler<CastingSelectedEvent>> onCastingSelected = new SimpleObjectProperty<>();
  public final BooleanProperty empty = new SimpleBooleanProperty(true);

  private final Type type;
  private final boolean interactive;

  public CastingsRow(Type type, boolean interactive) {
    this.type = type;
    this.interactive = interactive;

    getStyleClass().add("castings-row");

    widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        createCastingChildren(castings.get());
      }
    });

    castings.addListener(new ChangeListener<ObservableList<Casting>>() {
      private final ListChangeListener<Casting> listChangeListener = new ListChangeListener<Casting>() {
        @Override
        public void onChanged(ListChangeListener.Change<? extends Casting> c) {
          createCastingChildren(c.getList());
        }
      };

      private final WeakListChangeListener<Casting> weakListChangeListener = new WeakListChangeListener<>(listChangeListener);

      @Override
      public void changed(ObservableValue<? extends ObservableList<Casting>> observable, ObservableList<Casting> old, ObservableList<Casting> current) {
        if(old != null) {
          old.removeListener(weakListChangeListener);
        }

        if(current != null) {
          createCastingChildren(current);

          current.addListener(weakListChangeListener);
        }
      }
    });
  }

  private void createCastingChildren(ObservableList<? extends Casting> castings) {
    getChildren().clear();
    empty.set(true);

    double castingSize = 100 + getHgap();

    if(castings != null) {
      double space = getWidth() - castingSize;

      for(final Casting casting : castings) {
        empty.set(false);

        if(space < 0) {
          break;
        }

        if(casting.role.get().equals("Actor")) {
          CastingImage castingImage = new CastingImage();

          castingImage.setFocusTraversable(interactive);

          if(type == Type.CAST) {
            castingImage.title.bind(casting.person.get().name);
            castingImage.image.bind(casting.person.get().photo);
          }
          else {
            castingImage.title.bind(casting.media.get().titleWithContext);
            castingImage.image.bind(casting.media.get().image);
          }
          castingImage.characterName.bind(casting.characterName);

          castingImage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
              Events.dispatchEvent(onCastingSelected, new CastingSelectedEvent(casting), event);
            }
          });

          getChildren().add(castingImage);

          space -= castingSize;
        }
      }
    }
  }
}
