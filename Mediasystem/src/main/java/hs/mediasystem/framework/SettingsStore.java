package hs.mediasystem.framework;

import hs.mediasystem.db.Setting;
import hs.mediasystem.db.Setting.PersistLevel;
import hs.mediasystem.db.SettingsDao;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
      setting.setPersistTrigger(settingPersister);
      settings.put(setting.getSystem() + "/" + setting.getKey(), setting);
    }

    System.out.println("[FINE] SettingsStore - Loaded " + settings.size() + " settings");
  }

  public void storeSetting(String system, PersistLevel level, String key, String value) {
    String settingsKey = system + "/" + key;

    Setting setting = settings.get(settingsKey);

    if(setting == null) {
      setting = new Setting();

      setting.setSystem(system);
      setting.setKey(key);
      setting.setPersistTrigger(settingPersister);

      settings.put(settingsKey, setting);
    }

    setting.setPersistLevel(level);
    setting.setValue(value);
    setting.setLastUpdated(new Date());

    settingPersister.queueAsDirty(setting);
  }

  public String getSetting(String system, String key) {
    Setting setting = settings.get(system + "/" + key);

    return setting == null ? null : setting.getValue();
  }
}
