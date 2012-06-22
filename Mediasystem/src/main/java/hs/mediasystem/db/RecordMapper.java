package hs.mediasystem.db;

import java.sql.SQLException;
import java.util.Map;

public interface RecordMapper<T> {
  String getTableName();

  Map<String, Object> extractIds(T object);
  Map<String, Object> extractValues(T object);

  void applyValues(T object, Map<String, Object> values) throws SQLException;
  void setGeneratedKeys(T object, Map<String, Object> keys);

  void invokeAfterLoadStore(T object, Database database) throws SQLException;
}
