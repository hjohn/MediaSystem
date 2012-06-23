package hs.mediasystem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class Database {
  private static final Map<Class<? extends Fetcher<?, ?>>, Fetcher<?, ?>> FETCHERS = new HashMap<>();

  private final Provider<Connection> connectionProvider;

  @SuppressWarnings("unchecked")
  public static void registerFetcher(Fetcher<?, ?> fetcher) {
    FETCHERS.put((Class<? extends Fetcher<?, ?>>)fetcher.getClass(), fetcher);
  }

  public static <P, C> List<C> fetch(P parent, Class<?> cls) {
    @SuppressWarnings("unchecked")
    Fetcher<P, C> fetcher = (Fetcher<P, C>)FETCHERS.get(cls);

    if(fetcher == null) {
      throw new IllegalArgumentException("Fetcher not registered: " + cls);
    }

    return fetcher.fetch(parent);
  }

  @Inject
  public Database(Provider<Connection> connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  private static final ThreadLocal<Transaction> CURRENT_TRANSACTION = new ThreadLocal<>();

  public Transaction beginTransaction() throws DatabaseException {
    Transaction transaction = new Transaction(CURRENT_TRANSACTION.get());

    CURRENT_TRANSACTION.set(transaction);

    return transaction;
  }

  void endTransaction() {
    CURRENT_TRANSACTION.set(CURRENT_TRANSACTION.get().parent);
  }

  private static void setParameters(Map<String, Object> columns, PreparedStatement statement) throws SQLException {
    int parameterIndex = 1;

    for(String key : columns.keySet()) {
      Object value = columns.get(key);

      if(value instanceof Date) {
        statement.setTimestamp(parameterIndex++, new Timestamp(((Date)value).getTime()));
      }
      else if(value instanceof Enum) {
        statement.setObject(parameterIndex++, ((Enum<?>)value).name());
      }
      else {
        statement.setObject(parameterIndex++, value);
      }
    }
  }

  private static final Map<Class<?>, RecordMapper<?>> RECORD_MAPPERS = new HashMap<>();

  private static <T> RecordMapper<T> getRecordMapper(Class<T> cls) {
    @SuppressWarnings("unchecked")
    RecordMapper<T> recordMapper = (RecordMapper<T>)RECORD_MAPPERS.get(cls);

    if(recordMapper == null) {
      recordMapper = new AnnotatedRecordMapper<>(cls);

      RECORD_MAPPERS.put(cls, recordMapper);
    }

    return recordMapper;
  }

  public class Transaction implements AutoCloseable {
    private final Transaction parent;
    private final Connection connection;
    private final Savepoint savepoint;

    private int activeNestedTransactions;
    private boolean finished;

    Transaction(Transaction parent) throws DatabaseException {
      this.parent = parent;

      try {
        if(parent == null) {
          this.connection = connectionProvider.get();
          this.savepoint = null;

          connection.setAutoCommit(false);
        }
        else {
          this.connection = parent.getConnection();
          this.savepoint = connection.setSavepoint();

          parent.activeNestedTransactions++;
        }
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }

      assert this.connection != null;
      assert (this.parent != null && this.savepoint != null) || (this.parent == null && this.savepoint == null);
    }

    public Provider<Connection> getConnectionProvider() {
      return connectionProvider;
    }

    public Connection getConnection() {
      return connection;
    }

    private void ensureNotFinished() {
      if(finished) {
        throw new IllegalStateException("transaction already ended");
      }
    }

    public synchronized long getDatabaseSize() throws DatabaseException {
      ensureNotFinished();

      try(PreparedStatement statement = connection.prepareStatement("SELECT pg_database_size('mediasystem')");
          ResultSet rs = statement.executeQuery()) {
        if(rs.next()) {
          return rs.getLong(1);
        }

        throw new DatabaseException("unable to get database size");
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }
    }

    public Record selectUnique(String fields, String tableName, String whereCondition, Object... parameters) throws DatabaseException {
      List<Record> result = select(fields, tableName, whereCondition, parameters);

      return result.isEmpty() ? null : result.get(0);
    }

    public List<Record> select(String fields, String tableName, String whereCondition, Object... parameters) throws DatabaseException {
      ensureNotFinished();

      try(PreparedStatement statement = connection.prepareStatement("SELECT " + fields + " FROM " + tableName + (whereCondition == null ? "" : " WHERE " + whereCondition))) {
        int parameterIndex = 1;

        for(Object o : parameters) {
          statement.setObject(parameterIndex++, o);
        }

        try(ResultSet rs = statement.executeQuery()) {
          List<Record> records = new ArrayList<>();
          ResultSetMetaData metaData = rs.getMetaData();

          Map<String, Integer> fieldMapping = new HashMap<>();

          for(int i = 0; i < metaData.getColumnCount(); i++) {
            fieldMapping.put(metaData.getColumnName(i + 1), i);
          }

          while(rs.next()) {
            Object[] values = new Object[metaData.getColumnCount()];

            for(int i = 1; i <= metaData.getColumnCount(); i++) {
              values[i - 1] = rs.getObject(i);
            }

            records.add(new Record(values, fieldMapping));
          }

          return records;
        }
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }
    }

    public <T> T selectUnique(Class<T> cls, String whereCondition, Object... parameters) throws DatabaseException {
      List<T> result = select(cls, whereCondition, parameters);

      return result.isEmpty() ? null : result.get(0);
    }

    public synchronized <T> List<T> select(Class<T> cls, String whereCondition, Object... parameters) throws DatabaseException {
      ensureNotFinished();

      RecordMapper<T> recordMapper = getRecordMapper(cls);

      try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + recordMapper.getTableName() + (whereCondition == null ? "" : " WHERE " + whereCondition))) {
        int parameterIndex = 1;

        for(Object o : parameters) {
          statement.setObject(parameterIndex++, o);
        }

        try(ResultSet rs = statement.executeQuery()) {
          List<T> records = new ArrayList<>();
          ResultSetMetaData metaData = rs.getMetaData();

          while(rs.next()) {
            Map<String, Object> values = new HashMap<>();

            for(int i = 1; i <= metaData.getColumnCount(); i++) {
              String columnName = metaData.getColumnName(i);

              values.put(columnName, rs.getObject(i));
            }

            T record = cls.newInstance();
            recordMapper.applyValues(record, values);
            recordMapper.invokeAfterLoadStore(record, Database.this);

            records.add(record);
          }

          return records;
        }
        catch(IllegalAccessException | InstantiationException e) {
          throw new RuntimeException(e);
        }
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }
    }

    public synchronized <T> void merge(T obj) throws DatabaseException {
      @SuppressWarnings("unchecked")
      RecordMapper<T> recordMapper = (RecordMapper<T>)getRecordMapper(obj.getClass());

      Map<String, Object> ids = recordMapper.extractIds(obj);

      if(ids.isEmpty()) {
        insert(obj);
      }
      else {
        update(obj);
      }
    }

    public synchronized <T> void insert(T obj) throws DatabaseException {
      @SuppressWarnings("unchecked")
      RecordMapper<T> recordMapper = (RecordMapper<T>)getRecordMapper(obj.getClass());

      Map<String, Object> values = recordMapper.extractValues(obj);

      Map<String, Object> generatedKeys = insert(recordMapper.getTableName(), values);

      recordMapper.setGeneratedKeys(obj, generatedKeys);
      recordMapper.invokeAfterLoadStore(obj, Database.this);
    }

    public synchronized <T> void update(T obj) throws DatabaseException {
      @SuppressWarnings("unchecked")
      RecordMapper<T> recordMapper = (RecordMapper<T>)getRecordMapper(obj.getClass());

      Map<String, Object> ids = recordMapper.extractIds(obj);
      Map<String, Object> values = recordMapper.extractValues(obj);

      if(ids.isEmpty()) {
        throw new DatabaseException("Cannot update records that donot exist in the database: " + obj);
      }

      String whereCondition = "";
      Object[] parameters = new Object[ids.size()];
      int parameterIndex = 0;

      for(String id : ids.keySet()) {
        if(!whereCondition.isEmpty()) {
          whereCondition += ", ";
        }
        whereCondition += id + " = ?";
        parameters[parameterIndex++] = ids.get(id);
      }

      update(recordMapper.getTableName(), values, whereCondition, parameters);

      recordMapper.invokeAfterLoadStore(obj, Database.this);
    }

    public synchronized Map<String, Object> merge(String tableName, int id, Map<String, Object> parameters) throws DatabaseException {
      if(id == 0) {
        return insert(tableName, parameters);
      }

      update(tableName, id, parameters);

      return Collections.emptyMap();
    }

    public synchronized Map<String, Object> insert(String tableName, Map<String, Object> parameters) throws DatabaseException {
      ensureNotFinished();

      StringBuilder fields = new StringBuilder();
      StringBuilder values = new StringBuilder();
      Map<String, Object> generatedKeys = new HashMap<>();

      for(String key : parameters.keySet()) {
        if(fields.length() > 0) {
          fields.append(",");
          values.append(",");
        }

        fields.append(key);
        values.append("?");
      }

      System.out.println("[FINE] Database.Transaction.insert() - Inserting into '" + tableName + "': " + parameters);

      try(PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + " (" + fields.toString() + ") VALUES (" + values.toString() + ")", Statement.RETURN_GENERATED_KEYS)) {
        setParameters(parameters, statement);

        statement.execute();

        try(ResultSet rs = statement.getGeneratedKeys()) {
          if(rs.next()) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for(int i = 1; i <= columnCount; i++) {
              generatedKeys.put(metaData.getColumnName(i), rs.getObject(i));
            }
          }
        }
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }

      return generatedKeys;
    }

    public synchronized int update(String tableName, int id, Map<String, Object> parameters) throws DatabaseException {
      return update(tableName, parameters, "id = ?", id);
    }

    public synchronized int update(String tableName, Map<String, Object> values, String whereCondition, Object... parameters) throws DatabaseException {
      ensureNotFinished();

      StringBuilder set = new StringBuilder();

      for(String key : values.keySet()) {
        if(set.length() > 0) {
          set.append(",");
        }

        set.append(key);
        set.append("=?");
      }

      System.out.println("[FINE] Database.Transaction.update() - Updating '" + tableName + "' with condition: '" + whereCondition + "': " + Arrays.toString(parameters) + " and values: " + values);

      try(PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " SET " + set.toString() + " WHERE " + whereCondition)) {
        setParameters(values, statement);
        int parameterIndex = values.size() + 1;

        for(Object o : parameters) {
          statement.setObject(parameterIndex++, o);
        }

        return statement.executeUpdate();
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }
    }

    public synchronized int delete(String tableName, String whereCondition, Object... parameters) throws DatabaseException {
      ensureNotFinished();

      System.out.println("[FINE] Database.Transaction.delete() - Deleting from '" + tableName + "' with condition: '" + whereCondition + "': " + Arrays.toString(parameters));

      try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + whereCondition)) {
        int parameterIndex = 1;

        for(Object o : parameters) {
          statement.setObject(parameterIndex++, o);
        }

        return statement.executeUpdate();
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }
    }

    public synchronized int deleteChildren(String tableName, String parentTableName, long parentId) throws DatabaseException {
      ensureNotFinished();

      System.out.println("[FINE] Database.Transaction.deleteChildren() - Deleting '" + tableName + "' Children of '" + parentTableName + "' with id " + parentId);

      try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + parentTableName + "_id = ?")) {
        statement.setLong(1, parentId);

        return statement.executeUpdate();
      }
      catch(SQLException e) {
        throw new DatabaseException(e);
      }
    }

    private void finishTransaction(boolean commit) throws DatabaseException {
      ensureNotFinished();

      if(activeNestedTransactions != 0) {
        throw new IllegalStateException("attempt at rollback/commit while there are uncommitted nested transactions");
      }

      endTransaction();

      try {
        if(parent == null) {
          try {
            if(commit) {
              connection.commit();
            }
            else {
              connection.rollback();
            }
          }
          catch(SQLException e) {
            throw new DatabaseException(e);
          }
          finally {
            try {
              connection.close();
            }
            catch(SQLException e) {
              System.out.println("[FINE] Database.Transaction.finishTransaction() - exception while closing connection: " + e);
            }
          }
        }
        else {
          try {
            if(commit) {
              connection.releaseSavepoint(savepoint);
            }
            else {
              connection.rollback(savepoint);
            }
          }
          catch(SQLException e) {
            throw new DatabaseException(e);
          }
          finally {
            parent.activeNestedTransactions--;
          }
        }
      }
      finally {
        finished = true;
      }
    }

    public synchronized void commit() throws DatabaseException {
      finishTransaction(true);
    }

    public synchronized void rollback() throws DatabaseException {
      finishTransaction(false);
    }

    @Override
    public void close() {
      if(!finished) {
        rollback();
      }
    }
  }
}
