package hs.mediasystem.ext.media.serie;

import hs.mediasystem.screens.AreaLayout;
import hs.mediasystem.screens.collection.detail.MediaDetailPane;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class EpisodeDetailPane extends MediaDetailPane {
  protected final StringBinding season = MapBindings.selectInteger(content, "season").asString();
  protected final StringBinding episode = MapBindings.selectString(content, "episodeRange");

  public static EpisodeDetailPane create(AreaLayout areaLayout, boolean interactive) {
    EpisodeDetailPane pane = new EpisodeDetailPane(areaLayout);

    pane.postConstruct(interactive);

    return pane;
  }

  protected EpisodeDetailPane(AreaLayout areaLayout) {
    super(areaLayout);

    groupName.bind(MapBindings.selectString(content, "serie", "title"));
  }

  @Override
  protected void postConstruct(boolean interactive) {
    super.postConstruct(interactive);

    getStylesheets().add(getClass().getResource("episode-detail-pane-decorator.css").toExternalForm());

    add("title-area", 10, createSeasonEpisodeBlock());
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
