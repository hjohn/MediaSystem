package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;

import java.util.Map;

public interface RecordMapper<T> {
  String getTableName();

  Map<String, Object> extractIds(T object);
  Map<String, Object> extractValues(T object);

  void applyValues(Transaction transaction, T object, Map<String, Object> values);
  void setGeneratedKey(T object, Object key);

  void invokeAfterLoadStore(T object, Database database) throws DatabaseException;
}
