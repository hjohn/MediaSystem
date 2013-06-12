package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;

import java.util.List;
import java.util.Map;

public interface RecordMapper<T> {
  String getTableName();
  List<String> getColumnNames();

  Map<String, Object> extractIds(T object);
  Map<String, Object> extractValues(T object);

  void applyValues(Transaction transaction, Object object, Map<String, Object> values);
  void setGeneratedKey(T object, Object key);

  void invokeAfterLoadStore(Object object, Database database) throws DatabaseException;

  boolean isTransient(T object);
}
