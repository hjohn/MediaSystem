package hs.mediasystem.framework;

import hs.mediasystem.dao.Setting;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.dao.SettingsDao;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingsStore {
  private final SettingPersister settingPersister;

  private final Map<String, Setting> settings = new HashMap<>();

  @Inject
  public SettingsStore(SettingsDao settingsDao, SettingPersister settingPersister) {
    this.settingPersister = settingPersister;

    for(Setting setting : settingsDao.getAllSettings()) {
      setting.setPersister(settingPersister);
      settings.put(setting.getSystem() + "/" + setting.getKey(), setting);
    }

    System.out.println("[FINE] SettingsStore - Loaded " + settings.size() + " settings");
  }

  public void storeSetting(String system, PersistLevel level, String key, String value) {
    getValueProperty(system, level, key).set(value);
  }

  public String getSetting(String system, String key) {
    Setting setting = settings.get(system + "/" + key);

    return setting == null ? null : setting.getValue();
  }

  public StringProperty getValueProperty(String system, PersistLevel level, String key) {
    String settingsKey = system + "/" + key;

    Setting setting = settings.get(settingsKey);

    if(setting == null) {
      setting = new Setting();

      setting.setSystem(system);
      setting.setKey(key);
      setting.setPersister(settingPersister);

      settings.put(settingsKey, setting);
    }

    setting.setPersistLevel(level);

    return setting.valueProperty();
  }

  public BooleanProperty getBooleanProperty(String system, PersistLevel level, String key) {
    final StringProperty valueProperty = getValueProperty(system, level, key);
    final BooleanProperty booleanProperty = new SimpleBooleanProperty("TRUE".equals(valueProperty.get()));

    valueProperty.bindBidirectional(booleanProperty, new StringConverter<Boolean>() {
      @Override
      public Boolean fromString(String string) {
        return "TRUE".equals(string);
      }

      @Override
      public String toString(Boolean object) {
        return object ? "TRUE" : "FALSE";
      }
    });

    return booleanProperty;
  }

  public <T> ObservableList<T> getListProperty(String system, PersistLevel level, String key, final StringConverter<T> stringConverter) {
    final StringProperty valueProperty = getValueProperty(system, level, key);
    final ObservableList<T> list = FXCollections.observableArrayList();

    list.addListener(new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        StringBuilder builder = new StringBuilder();

        for(T t : list) {
          String s = stringConverter.toString(t);

          if(builder.length() > 0) {
            builder.append("||");
          }
          builder.append(s.replace("|", "|p"));
        }

        valueProperty.set(builder.toString());
      }
    });

    String encodedList = valueProperty.get();

    if(encodedList != null) {
      for(String item : encodedList.split("\\|\\|")) {
        list.add(stringConverter.fromString(item.replace("|p", "|")));
      }
    }

    return list;
  }
}
