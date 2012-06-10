package hs.mediasystem.db;

import java.util.Map;

public interface RecordMapper<T> {
  Map<String, Object> extractValues(T object);
  Map<String, Object> extractIds(T object);

  void applyValues(T object, Map<String, Object> values);
  String getTableName();
  void setGeneratedKeys(T object, Map<String, Object> keys);
}
