package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.Setting.PersistLevel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class SettingsDao {
  private final Provider<Connection> connectionProvider;
  private final Database database;

  @Inject
  public SettingsDao(Database database, Provider<Connection> connectionProvider) {
    this.database = database;
    this.connectionProvider = connectionProvider;
  }

  public List<Setting> getAllSettings() {
    try(Connection connection = connectionProvider.get();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM settings")) {

      try(ResultSet rs = statement.executeQuery()) {
        List<Setting> settings = new ArrayList<>();

        while(rs.next()) {
          Setting setting = new Setting();

          setting.setId(rs.getInt("id"));
          setting.setSystem(rs.getString("system"));
          setting.setPersistLevel(PersistLevel.valueOf(rs.getString("persistlevel")));
          setting.setKey(rs.getString("key"));
          setting.setValue(rs.getString("value"));
          setting.setLastUpdated(rs.getDate("lastupdated"));

          settings.add(setting);
        }

        return settings;
      }
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void storeSetting(Setting setting) {
    try(Transaction transaction = database.beginTransaction()) {
      if(setting.getId() != 0) {
        transaction.delete("settings", "id = ?", setting.getId());
      }

      Map<String, Object> generatedKeys = transaction.insert("settings", createSettingFieldMap(setting));

      setting.setId((int)generatedKeys.get("id"));
      transaction.commit();
    }
    catch(SQLException e) {
      throw new RuntimeException("exception while storing: " + setting, e);
    }
  }

  private static Map<String, Object> createSettingFieldMap(Setting setting) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("system", setting.getSystem());
    columns.put("persistlevel", setting.getPersistLevel().name());
    columns.put("key", setting.getKey());
    columns.put("value", setting.getValue());
    columns.put("lastupdated", setting.getLastUpdated());

    return columns;
  }
}
