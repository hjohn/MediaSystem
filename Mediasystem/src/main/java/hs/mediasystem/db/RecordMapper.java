package hs.mediasystem.db;

import java.sql.SQLException;
import java.util.Map;

public interface RecordMapper<T> {
  Map<String, Object> extractValues(T object);
  Map<String, Object> extractIds(T object);

  void applyValues(T object, Map<String, Object> values) throws SQLException;
  String getTableName();
  void setGeneratedKeys(T object, Map<String, Object> keys);
}
