package hs.mediasystem.framework;

import hs.mediasystem.db.Setting.PersistLevel;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.util.StringConverter;

public class SettingUpdater<T> implements ChangeListener<T> {
  private final SettingsStore settingsStore;
  private final StringConverter<T> converter;

  private StringProperty valueProperty;

  public SettingUpdater(SettingsStore settingsStore, StringConverter<T> converter) {
    this.settingsStore = settingsStore;
    this.converter = converter;
  }

  public T getStoredValue() {
    return valueProperty == null ? null : converter.fromString(valueProperty.get());
  }

  public T getStoredValue(T defaultValue) {
    T storedValue = getStoredValue();

    return storedValue == null ? defaultValue : storedValue;
  }

  public void setBackingSetting(String system, PersistLevel level, String key) {
    valueProperty = settingsStore.getValueProperty(system, level, key);
  }

  @Override
  public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
    if(valueProperty != null) {
      valueProperty.set(converter.toString(newValue));
    }
  }
}