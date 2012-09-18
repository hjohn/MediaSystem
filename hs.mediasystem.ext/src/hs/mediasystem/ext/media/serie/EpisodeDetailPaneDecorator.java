package hs.mediasystem.ext.media.serie;

import hs.mediasystem.screens.selectmedia.DetailPane;
import hs.mediasystem.screens.selectmedia.MediaDetailPaneDecorator;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class EpisodeDetailPaneDecorator extends MediaDetailPaneDecorator {
  protected final StringBinding season = MapBindings.selectInteger(dataProperty(), "season").asString();
  protected final StringBinding episode = MapBindings.selectString(dataProperty(), "episodeRange");

  public EpisodeDetailPaneDecorator(DetailPane.DecoratablePane decoratablePane) {
    super(decoratablePane);

    groupName.bind(MapBindings.selectString(dataProperty(), "serie", "title"));
  }

  @Override
  public void decorate(boolean interactive) {
    super.decorate(interactive);

    decoratablePane.getStylesheets().add("select-media/episode-detail-pane.css");  // TODO move to bundle when possible

    decoratablePane.add("title-area", 10, createSeasonEpisodeBlock());
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
