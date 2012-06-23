package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.selectmedia.StandardDetailPane;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class EpisodeDetailPane extends StandardDetailPane {
  protected final StringBinding season = MapBindings.selectInteger(mediaNodeProperty(), "dataMap", Episode.class, "season").asString();
  protected final StringBinding episode = MapBindings.selectString(mediaNodeProperty(), "dataMap", Episode.class, "episodeRange");

  public EpisodeDetailPane() {
    getStylesheets().add("select-media/episode-detail-pane.css");  // TODO move to bundle when possible

    groupName.bind(MapBindings.selectString(mediaNodeProperty(), "dataMap", Episode.class, "serie", "dataMap", Media.class, "title"));
  }

  @Override
  protected Pane createInfoBlock() {
    return new VBox() {{
      getChildren().add(createRating());
      getChildren().add(createGenresField());
      getChildren().add(createSeasonEpisodeBlock());
      getChildren().add(createPlotBlock());
      getChildren().add(createMiscelaneousFieldsBlock());
    }};
  }

  protected Pane createSeasonEpisodeBlock() {
    final Label seasonLabel = new Label() {{
      getStyleClass().addAll("field", "season");
      setMaxWidth(10000);
      textProperty().bind(season);
    }};

    final Label episodeLabel = new Label() {{
      getStyleClass().addAll("field", "episode");
      setMaxWidth(10000);
      textProperty().bind(episode);
    }};

    return new FlowPane() {{
      getStyleClass().add("fields");
      getChildren().add(createTitledBlock("SEASON", seasonLabel, null));
      getChildren().add(createTitledBlock("EPISODE", episodeLabel, null));
    }};
  }
}
