package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Casting.MediaType;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.WeakBinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.beans.binding.Bindings;
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
import javafx.scene.Node;
import javafx.scene.layout.TilePane;

public class CastingsRow extends TilePane {
  public enum Type {
    CAST, APPEARANCES;
  }

  private static int determineGroup(Casting casting) {
    return casting.characterName.get().isEmpty() || casting.role.get().equals("Self") ? 1 :
                                           casting.mediaType.get() == MediaType.MOVIE ? 0 :
                                                       casting.episodeCount.get() > 5 ? 0 : 1;
  }

  private static final Comparator<Casting> CASTING_ORDER = Comparator.<Casting>comparingInt(c -> c.index.get())
      .thenComparingInt(CastingsRow::determineGroup)
      .thenComparing(c -> c.media.get().releaseDate.get(), Comparator.nullsLast((ld1, ld2) -> ld2.compareTo(ld1)));

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
        @SuppressWarnings("unchecked")
        @Override
        public void onChanged(ListChangeListener.Change<? extends Casting> c) {
          createCastingChildren((ObservableList<Casting>)c.getList());
        }
      };

      private final WeakListChangeListener<Casting> weakListChangeListener = new WeakListChangeListener<>(listChangeListener);

      @Override
      public void changed(ObservableValue<? extends ObservableList<Casting>> observable, ObservableList<Casting> old, ObservableList<Casting> current) {
        if(old != null) {
          old.removeListener(weakListChangeListener);
        }

        createCastingChildren(current);

        if(current != null) {
          current.addListener(weakListChangeListener);
        }
      }
    });
  }

  private final WeakBinder binder = new WeakBinder();

  private void createCastingChildren(ObservableList<Casting> castings) {

    /*
     * Cleanup old children (setting the image to null reduces the chance that an unneeded image is loaded in the background):
     */

    for(Node node : getChildren()) {
      ((CastingImage)node).image.set(null);
    }

    List<CastingImage> castingImages = new ArrayList<>();

    /*
     * Create new children:
     */

    double castingSize = 100 + getHgap();

    if(castings != null) {
      double space = getWidth() - castingSize;

      for(final Casting casting : castings.sorted(CASTING_ORDER)) {
        if(casting.role.get().equals("Actor")) {
          CastingImage castingImage = new CastingImage();

          castingImage.setFocusTraversable(interactive);

          if(type == Type.CAST) {
            binder.bind(castingImage.title, casting.person.get().name);
            binder.bind(castingImage.image, casting.person.get().photo);
          }
          else {
            binder.bind(castingImage.title, Bindings.concat(
              casting.media.get().titleWithContext,
              " (", casting.media.get().releaseYear.get(),
              Bindings.when(casting.episodeCount.greaterThan(0)).then(Bindings.concat(", ", casting.episodeCount.get(), " ep")).otherwise(""),
              ")"
            ));
            binder.bind(castingImage.image, casting.media.get().image);
          }

          binder.bind(castingImage.characterName, casting.characterName);

          castingImage.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
              Events.dispatchEvent(onCastingSelected, new CastingSelectedEvent(casting), event);
            }
          });

          castingImages.add(castingImage);

          space -= castingSize;

          if(space < 0) {
            break;
          }
        }
      }
    }

    this.empty.set(castingImages.isEmpty());
    getChildren().setAll(castingImages);
  }
}
