package hs.mediasystem.framework;

import hs.mediasystem.dao.Setting;
import hs.mediasystem.dao.SettingsDao;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.persist.PersistTask;
import hs.mediasystem.persist.Persister;

import java.util.Date;

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
          setting.setLastUpdated(new Date());
          settingsDao.storeSetting(setting);
        }
      });
    }
  }
}
