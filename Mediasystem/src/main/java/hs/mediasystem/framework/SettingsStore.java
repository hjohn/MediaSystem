package hs.mediasystem.framework;

import hs.mediasystem.dao.Setting;
import hs.mediasystem.dao.SettingsDao;
import hs.mediasystem.dao.Setting.PersistLevel;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.StringProperty;

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
}
