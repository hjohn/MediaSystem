package hs.mediasystem.screens;

import hs.mediasystem.framework.SubtitleProvider;
import hs.subtitle.SubtitleDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;

public class SubtitleSelector {
  private final ObjectProperty<SubtitleProvider> subtitleProvider = new SimpleObjectProperty<>();
  public SubtitleProvider getSubtitleProvider() { return subtitleProvider.get(); }
  public void setSubtitleProvider(SubtitleProvider provider) { subtitleProvider.set(provider); }
  public ObjectProperty<SubtitleProvider> subtitleProviderProperty() { return subtitleProvider; }

  private final List<SubtitleProvider> subtitleProviders = new ArrayList<>();
  public List<SubtitleProvider> getSubtitleProviders() { return Collections.unmodifiableList(subtitleProviders); }

  private final ObservableList<SubtitleDescriptor> subtitles = FXCollections.observableArrayList();
  public ObservableList<SubtitleDescriptor> getSubtitles() { return subtitles; }

  private final ReadOnlyBooleanWrapper loaded = new ReadOnlyBooleanWrapper(false);
  public boolean isLoaded() { return loaded.get(); }
  public ReadOnlyBooleanProperty loadedProperty() { return loaded.getReadOnlyProperty(); }

  private final StringProperty statusText = new SimpleStringProperty();
  public ObservableValue<? extends String> statusTextProperty() { return statusText; }

  private final SubtitleQueryService subtitleQueryService = new SubtitleQueryService();

  public SubtitleSelector(List<SubtitleProvider> providers) {
    statusText.bind(subtitleQueryService.messageProperty());

    subtitleProviders.addAll(providers);
    subtitleProvider.set(providers.get(0));

    subtitleQueryService.stateProperty().addListener(new ChangeListener<Service.State>() {
      @Override
      public void changed(ObservableValue<? extends Service.State> observableValue, Service.State oldValue, Service.State newValue) {
        if(newValue == Service.State.SUCCEEDED) {
          subtitles.clear();
          subtitles.addAll(subtitleQueryService.getValue());

          System.out.println("[FINE] SubtitleSelector.SubtitleSelector(...).new ChangeListener() {...}.changed() - Succesfully loaded " + subtitleQueryService.getValue().size() + " subtitles");

          loaded.set(true);
        }
        else if(newValue == Service.State.FAILED) {
          System.out.println("[FINE] SubtitleSelector.SubtitleSelector(...).new ChangeListener() {...}.changed() - Exception search for subtitles: " + subtitleQueryService.getException());
          subtitleQueryService.getException().printStackTrace();
        }
      }
    });
  }

  public void query(Map<String, Object> criteria) {
    loaded.set(false);
    subtitles.clear();

    subtitleQueryService.setCriteria(criteria);
    subtitleQueryService.setSubtitleProvider(subtitleProvider.get());
    subtitleQueryService.restart();
  }
}
