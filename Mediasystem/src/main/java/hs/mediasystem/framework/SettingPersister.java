package hs.mediasystem.framework;

import hs.mediasystem.dao.Setting;
import hs.mediasystem.dao.SettingsDao;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.persist.PersistTask;
import hs.mediasystem.persist.DatabasePersister;

import java.util.Date;

import javax.inject.Inject;

public class SettingPersister implements DatabasePersister<Setting> {
  private final SettingsDao settingsDao;
  private final PersistQueue queue = new PersistQueue(3000);

  @Inject
  public SettingPersister(SettingsDao settingsDao) {
    this.settingsDao = settingsDao;
  }

  @Override
  public void queueAsDirty(final Setting setting) {
    if(setting.getPersistLevel() != PersistLevel.SESSION) {
      queue.queueAsDirty(setting, new PersistTask() {
        @Override
        public void persist() {
          setting.setLastUpdated(new Date());
          settingsDao.storeSetting(setting);
        }
      });
    }
  }
}
