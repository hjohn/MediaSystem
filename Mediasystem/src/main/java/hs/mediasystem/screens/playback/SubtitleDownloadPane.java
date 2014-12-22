package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.framework.SubtitleProvider;
import hs.mediasystem.framework.actions.StringConvertingCell;
import hs.mediasystem.util.StringConverter;
import hs.subtitle.SubtitleDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class SubtitleDownloadPane extends VBox {
  private static final KeyCombination ENTER = new KeyCodeCombination(KeyCode.ENTER);

  private static final Comparator<SubtitleProvider> SUBTITLE_PROVIDER_COMPARATOR = new Comparator<SubtitleProvider>() {
    @Override
    public int compare(SubtitleProvider o1, SubtitleProvider o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  private final Set<SubtitleProvider> subtitleProviders;
  private final Set<SubtitleCriteriaProvider> subtitleCriteriaProviders;

  public SubtitleDownloadPane(Media media, Set<SubtitleProvider> subtitleProviders, Set<SubtitleCriteriaProvider> subtitleCriteriaProviders, SubtitleDownloadService subtitleDownloadService) {
    this.subtitleProviders = subtitleProviders;
    this.subtitleCriteriaProviders = subtitleCriteriaProviders;

    getStyleClass().add("media-look");

    final SubtitleSelector subtitleSelector = new SubtitleSelector(findSubtitleProviders("movie"));
    final SubtitleCriteriaProvider subtitleCriteriaProvider = findSubtitleCriteriaProvider(media.getClass());

    subtitleSelector.query(subtitleCriteriaProvider.getCriteria(media));

    subtitleSelector.subtitleProviderProperty().addListener(new ChangeListener<SubtitleProvider>() {
      @Override
      public void changed(ObservableValue<? extends SubtitleProvider> observableValue, SubtitleProvider oldValue, SubtitleProvider newValue) {
        subtitleSelector.query(subtitleCriteriaProvider.getCriteria(media));
      }
    });

    ComboBox<SubtitleProvider> subtitleProviderComboBox = new ComboBox<>();
    ListView<SubtitleDescriptor> subtitleListView = new ListView<>();

    StringConverter<SubtitleProvider> subtitleProviderStringConverter = new StringConverter<SubtitleProvider>() {
      @Override
      public String toString(SubtitleProvider provider) {
        return provider.getName();
      }
    };

    StringConverter<SubtitleDescriptor> subtitleDescriptorStringConverter = new StringConverter<SubtitleDescriptor>() {
      @Override
      public String toString(SubtitleDescriptor descriptor) {
        return descriptor.getMatchType().name() + ": " + descriptor.getName() + " (" + descriptor.getLanguageName() + ") [" + descriptor.getType() + "]";
      }
    };

    // Bindings
    subtitleProviderComboBox.valueProperty().bindBidirectional(subtitleSelector.subtitleProviderProperty());
    subtitleProviderComboBox.setItems(FXCollections.observableList(subtitleSelector.getSubtitleProviders()));
    subtitleProviderComboBox.setCellFactory(listView -> new StringConvertingCell<>(subtitleProviderStringConverter));
    subtitleProviderComboBox.setButtonCell(new StringConvertingCell<>(subtitleProviderStringConverter));

    subtitleListView.setItems(subtitleSelector.getSubtitles());
    subtitleListView.setCellFactory(listView -> new StringConvertingCell<>(subtitleDescriptorStringConverter));
    subtitleListView.setOnKeyPressed(e -> {
      if(ENTER.match(e)) {
        SubtitleDescriptor subtitleDescriptor = subtitleListView.getSelectionModel().getSelectedItem();

        if(subtitleDescriptor != null) {
          subtitleDownloadService.setSubtitleDescriptor(subtitleDescriptor);
          subtitleDownloadService.restart();
        }
      }
    });
    subtitleListView.setMinWidth(750);

    // UI
    getChildren().add(new Label("Subtitle Download") {{
      getStyleClass().add("title");
      BorderPane.setAlignment(this, Pos.CENTER);
    }});

    VBox vbox = new VBox(subtitleProviderComboBox, subtitleListView);

    vbox.getStyleClass().add("input-fields");

    getChildren().add(vbox);
  }

  private List<SubtitleProvider> findSubtitleProviders(String mediaType) {
    List<SubtitleProvider> matchingSubtitleProviders = new ArrayList<>();

    for(SubtitleProvider provider : subtitleProviders) {
      if(provider.getMediaTypes().contains(mediaType)) {
        matchingSubtitleProviders.add(provider);
      }
    }

    Collections.sort(matchingSubtitleProviders, SUBTITLE_PROVIDER_COMPARATOR);

    return matchingSubtitleProviders;
  }

  private SubtitleCriteriaProvider findSubtitleCriteriaProvider(Class<?> cls) {
    for(SubtitleCriteriaProvider provider : subtitleCriteriaProviders) {
      if(provider.getMediaType().equals(cls)) {
        return provider;
      }
    }

    return null;
  }
}
