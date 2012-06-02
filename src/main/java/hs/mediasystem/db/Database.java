package hs.mediasystem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Arrays;
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

  private static final ThreadLocal<Transaction> currentTransaction = new ThreadLocal<>();

  public synchronized Transaction beginTransaction() throws SQLException {
    Transaction transaction = new Transaction(currentTransaction.get());

    currentTransaction.set(transaction);

    return transaction;
  }

  synchronized void endTransaction() {
    currentTransaction.set(currentTransaction.get().parent);
  }

  private static void setParameters(Map<String, Object> columns, PreparedStatement statement) throws SQLException {
    int parameterIndex = 1;

    for(String key : columns.keySet()) {
      Object value = columns.get(key);

      if(value instanceof Date) {
        statement.setTimestamp(parameterIndex++, new Timestamp(((Date)value).getTime()));
      }
      else {
        statement.setObject(parameterIndex++, value);
      }
    }
  }

  public class Transaction implements AutoCloseable {
    private final Transaction parent;
    private final Connection connection;
    private final Savepoint savepoint;

    private int activeNestedTransactions;
    private boolean finished;

    Transaction(Transaction parent) throws SQLException {
      this.parent = parent;

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

      assert this.connection != null;
      assert (this.parent != null && this.savepoint != null) || (this.parent == null && this.savepoint == null);
    }

    public Connection getConnection() {
      return connection;
    }

    private void ensureNotFinished() {
      if(finished) {
        throw new IllegalStateException("transaction already ended");
      }
    }

    public synchronized Map<String, Object> insert(String tableName, Map<String, Object> parameters) throws SQLException {
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

      return generatedKeys;
    }

    public synchronized int update(String tableName, int id, Map<String, Object> parameters) throws SQLException {
      ensureNotFinished();

      StringBuilder set = new StringBuilder();

      for(String key : parameters.keySet()) {
        if(set.length() > 0) {
          set.append(",");
        }

        set.append(key);
        set.append("=?");
      }

      System.out.println("[FINE] Database.Transaction.update() - Updating id " + id + " in '" + tableName + "': " + parameters);

      try(PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " SET " + set.toString() + " WHERE id = ?")) {
        setParameters(parameters, statement);

        statement.setLong(parameters.size() + 1, id);

        return statement.executeUpdate();
      }
    }

    public synchronized int delete(String tableName, String whereCondition, Object... parameters) throws SQLException {
      ensureNotFinished();

      System.out.println("[FINE] Database.Transaction.delete() - Deleting from '" + tableName + "' with condition: '" + whereCondition + "': " + Arrays.toString(parameters));

      try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + whereCondition)) {
        int parameterIndex = 1;

        for(Object o : parameters) {
          statement.setObject(parameterIndex++, o);
        }

        return statement.executeUpdate();
      }
    }

    public synchronized int deleteChildren(String tableName, String parentTableName, long parentId) throws SQLException {
      ensureNotFinished();

      System.out.println("[FINE] Database.Transaction.deleteChildren() - Deleting '" + tableName + "' Children of '" + parentTableName + "' with id " + parentId);

      try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + parentTableName + "_id = ?")) {
        statement.setLong(1, parentId);

        return statement.executeUpdate();
      }
    }

    private void finishTransaction(boolean commit) throws SQLException {
      ensureNotFinished();

      if(activeNestedTransactions != 0) {
        throw new IllegalStateException("attempt at rollback/commit while there are uncommitted nested transactions");
      }

      endTransaction();

      if(parent == null) {
        try {
          if(commit) {
            connection.commit();
          }
          else {
            connection.rollback();
          }
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
        if(commit) {
          connection.releaseSavepoint(savepoint);
        }
        else {
          connection.rollback(savepoint);
        }

        parent.activeNestedTransactions--;
      }

      finished = true;
    }

    public synchronized void commit() throws SQLException {
      finishTransaction(true);
    }

    public synchronized void rollback() throws SQLException {
      finishTransaction(false);
    }

    @Override
    public void close() {
      try {
        if(!finished) {
          rollback();
        }
      }
      catch(SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
