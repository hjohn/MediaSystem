package hs.mediasystem.screens.collection.detail;

import hs.mediasystem.beans.AsyncImageProperty;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Identifier;
import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.AreaLayout;
import hs.mediasystem.screens.MediaItemFormatter;
import hs.mediasystem.screens.StarRating;
import hs.mediasystem.screens.collection.detail.CastingsRow.Type;
import hs.mediasystem.screens.optiondialog.OptionButton;
import hs.mediasystem.screens.optiondialog.OptionCheckBox;
import hs.mediasystem.util.Events;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.MapBindings;
import hs.mediasystem.util.ScaledImageView;
import hs.mediasystem.util.SizeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.binding.LongBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MediaDetailPane extends DetailPane<Media> {
  protected final ObjectBinding<ImageHandle> posterHandle = MapBindings.select(content, "image");
  protected final AsyncImageProperty poster = new AsyncImageProperty();

  protected final StringProperty groupName = new SimpleStringProperty();
  protected final StringBinding title = MapBindings.selectString(content, "title");
  protected final StringBinding subtitle = MapBindings.selectString(content, "subtitle");
  protected final StringBinding releaseTime = MediaItemFormatter.releaseTimeBinding(content);
  protected final StringBinding plot = MapBindings.selectString(content, "description");
  protected final DoubleBinding rating = MapBindings.selectDouble(content, "rating");
  protected final IntegerBinding runtime = MapBindings.selectInteger(content, "runtime");
  protected final ObjectBinding<ObservableList<Casting>> castings = MapBindings.select(content, "castings");
  protected final ObjectBinding<ObservableList<Identifier>> identifiers = MapBindings.select(content, "identifiers");
  protected final StringBinding genres = new StringBinding() {
    final ObjectBinding<String[]> selectGenres = MapBindings.select(content, "genres");

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

  public static MediaDetailPane create(AreaLayout areaLayout, boolean interactive) {
    MediaDetailPane pane = new MediaDetailPane(areaLayout);

    pane.postConstruct(interactive);

    return pane;
  }

  protected MediaDetailPane(AreaLayout areaLayout) {
    super(areaLayout);

    poster.imageHandleProperty().bind(posterHandle);
  }

  protected void postConstruct(boolean interactive) {
    getStylesheets().add("collection/media-detail-pane.css");

    add("title-area", 1, new Label() {{
      getStyleClass().add("group-name");
      textProperty().bind(groupName);
      managedProperty().bind(groupName.isNotEqualTo(""));
      visibleProperty().bind(groupName.isNotEqualTo(""));
    }});

    add("title-area", 2, new Label() {{
      getStyleClass().add("title");
      textProperty().bind(title);
    }});

    add("title-area", 3, createSubtitleField());
    add("title-area", 4, createRating());
    add("title-area", 5, createGenresField());

    ScaledImageView imageView = new ScaledImageView() {{
      getStyleClass().add("poster-image");
      imageProperty().bind(poster);
      setPreserveRatio(true);
      setSmooth(true);
      setAlignment(Pos.TOP_CENTER);
    }};

    Timeline fadeInTimeline = new Timeline(
      new KeyFrame(Duration.ZERO, new KeyValue(imageView.opacityProperty(), 0)),
      new KeyFrame(Duration.seconds(0.3), new KeyValue(imageView.opacityProperty(), 1))
    );

    imageView.imageProperty().addListener(new ChangeListener<Image>() {
      @Override
      public void changed(ObservableValue<? extends Image> observable, Image oldValue, Image newValue) {
        fadeInTimeline.playFromStart();
      }
    });

    VBox.setVgrow(imageView, Priority.ALWAYS);

    add("title-image-area", 1, imageView);

    add("description-area", 6, createPlotBlock());
    add("description-area", 7, createMiscelaneousFieldsBlock(createReleaseDateBlock(), createRuntimeBlock()));

    CastingsRow castingsRow = createCastingsRow(interactive);
    Pane titledCastingsRow = createTitledBlock("CAST", castingsRow, castingsRow.empty.not());
    HBox.setHgrow(titledCastingsRow, Priority.ALWAYS);

    add("link-area", 1, titledCastingsRow);

    if(hasArea("action-area")) {
      final TabPane tabPane = new TabPane();

      tabPane.setSide(Side.BOTTOM);
      tabPane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

      add("action-area", 1, createTitledBlock("COPIES", tabPane, null));

      identifiers.addListener(new ChangeListener<ObservableList<Identifier>>() {
        @Override
        public void changed(ObservableValue<? extends ObservableList<Identifier>> observableValue, ObservableList<Identifier> old, ObservableList<Identifier> current) {
          generateMediaTabs(tabPane, current);
        }
      });

      generateMediaTabs(tabPane, identifiers.get());
    }
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

  protected Pane createMiscelaneousFieldsBlock(final Node... fields) {
    FlowPane flowPane = new FlowPane();

    flowPane.getStyleClass().add("fields");
    flowPane.getChildren().addAll(fields);

    return flowPane;
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

  protected CastingsRow createCastingsRow(boolean interactive) {
    CastingsRow castingsRow = new CastingsRow(Type.CAST, interactive);

    castingsRow.castings.bind(castings);
    castingsRow.onCastingSelected.set(new EventHandler<CastingSelectedEvent>() {
      @Override
      public void handle(CastingSelectedEvent event) {
        Events.dispatchEvent(onNavigate, new DetailNavigationEvent(event.getCasting().person.get()), event);
      }
    });

    return castingsRow;
  }

  private void generateMediaTabs(final TabPane tabPane, ObservableList<Identifier> current) {
    tabPane.getTabs().clear();

    if(current == null) {
      return;
    }

    int copyCount = 1;

    for(final Identifier identifier : current) {
      Tab tab = new Tab("" + copyCount++);

      tabPane.getTabs().add(tab);

      final StringBinding formattedName = new StringBinding() {
        private final StringBinding binding = MapBindings.selectString(identifier.mediaData, "uri");

        {
          bind(binding);
        }

        @Override
        protected String computeValue() {
          String uri = binding.get();

          if(uri == null) {
            return "";
          }

          int index = uri.lastIndexOf('/');

          if(index == -1) {
            index = uri.lastIndexOf('\\');
          }

          return uri.substring(index + 1);
        }
      };

      final StringBinding formattedPath = new StringBinding() {
        private final StringBinding binding = MapBindings.selectString(identifier.mediaData, "uri");

        {
          bind(binding);
        }

        @Override
        protected String computeValue() {
          String uri = binding.get();

          if(uri == null) {
            return "";
          }

          int index = uri.lastIndexOf('/');

          if(index == -1) {
            index = uri.lastIndexOf('\\');
          }

          return index == -1 ? "" : uri.substring(0, index);
        }
      };

      final StringBinding formattedFileLength = new StringBinding() {
        private final LongBinding binding = MapBindings.selectLong(identifier.mediaData, "fileLength");

        {
          bind(binding);
        }

        @Override
        protected String computeValue() {
          return SizeFormatter.BYTES_THREE_SIGNIFICANT.format(binding.get());
        }
      };

      final StringBinding formattedMatchType = new StringBinding() {
        {
          bind(identifier.matchType);
        }

        @Override
        protected String computeValue() {
          return identifier.matchType.get().name();
        }
      };

      tab.setContent(new VBox() {{
        getChildren().add(new Label() {{
          getStyleClass().add("field");
          textProperty().bind(formattedName);
        }});

        getChildren().add(createTitledBlock("PATH", new Label() {{
          getStyleClass().add("field");
          textProperty().bind(formattedPath);
        }}, null));

        getChildren().add(createTitledBlock("SIZE", new Label() {{
          getStyleClass().add("field");
          textProperty().bind(formattedFileLength);
        }}, null));

        getChildren().add(createMiscelaneousFieldsBlock(
          createTitledBlock("MATCH TYPE", new Label() {{
            getStyleClass().add("field");
            textProperty().bind(formattedMatchType);
          }}, null),

          createTitledBlock("MATCH ACCURACY", new Label() {{
            getStyleClass().add("field");
            textProperty().bind(new StringBinding() {
              {
                bind(identifier.matchAccuracy);
              }

              @Override
              protected String computeValue() {
                Float matchAccuracy = identifier.matchAccuracy.get();

                return matchAccuracy == null ? "" : String.format("%3.0f%%", matchAccuracy * 100);
              }
            });
          }}, null)
        ));

        getChildren().add(createTitledBlock("ACTIONS", new VBox() {{
          getChildren().add(new OptionCheckBox("Viewed") {{
            selectedProperty().bindBidirectional(identifier.mediaData.get().viewed);
          }});

          getChildren().add(new OptionButton("Reload Meta Data") {{
            setOnAction(new EventHandler<ActionEvent>() {
              @Override
              public void handle(ActionEvent event) {
                System.out.println("[INFO] Collection: 'Reload meta data' selected for: " + identifier);

//                mediaItem.reloadMetaData();
//
//                Media media = mediaItem.get(Media.class);
//
//                ImageCache.expunge(media.getBanner());
//                ImageCache.expunge(media.getImage());
//                ImageCache.expunge(media.getBackground());
              }
            });
          }});
        }}, null));
      }});
    }
  }
}
