package hs.mediasystem.framework;

import hs.mediasystem.db.Setting;
import hs.mediasystem.db.Setting.PersistLevel;
import hs.mediasystem.db.SettingsDao;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.persist.PersistTask;
import hs.mediasystem.persist.Persister;

import javax.inject.Inject;

public class SettingPersister implements Persister<Setting> {
  private final SettingsDao settingsDao;
  private final PersistQueue queue;

  @Inject
  public SettingPersister(SettingsDao settingsDao, PersistQueue queue) {
    this.settingsDao = settingsDao;
    this.queue = queue;
  }

  @Override
  public void queueAsDirty(final Setting setting) {
    if(setting.getPersistLevel() != PersistLevel.SESSION) {
      queue.queueAsDirty(setting, new PersistTask() {
        @Override
        public void persist() {
          settingsDao.storeSetting(setting);
        }
      });
    }
  }
}
