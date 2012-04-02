package hs.mediasystem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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

  private Transaction activeTransaction;
  private boolean rollbackOnly;

  public synchronized Transaction beginTransaction() {
    if(activeTransaction != null) {
      return new Transaction(activeTransaction.getConnection());
    }

    @SuppressWarnings("resource")
    Connection connection = connectionProvider.get();

    try {
      connection.setAutoCommit(false);

      activeTransaction = new Transaction(connection);
      rollbackOnly = false;

      return activeTransaction;
    }
    catch(SQLException e) {
      try {
        connection.close();
      }
      catch(SQLException e2) {
        // ignore, original exception is more important
      }

      throw new RuntimeException(e);
    }
  }

  private synchronized void commit(Transaction transaction) throws SQLException {
    if(transaction.equals(activeTransaction)) {
      activeTransaction = null;

      try {
        if(!rollbackOnly) {
          transaction.getConnection().commit();
        }
        else {
          transaction.getConnection().rollback();
          throw new SQLException("transaction was rollback only");
        }
      }
      finally {
        transaction.getConnection().close();
      }
    }
  }

  private synchronized void rollback(Transaction transaction) throws SQLException {
    rollbackOnly = true;

    if(transaction.equals(activeTransaction)) {
      activeTransaction = null;

      try {
        transaction.getConnection().rollback();
      }
      finally {
        transaction.getConnection().close();
      }
    }
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
    private final Connection connection;

    private boolean finished;

    /**
     * Creates a Transaction object which uses the same connection for all operations.
     *
     * @param connection a Connection (with auto commit set to false)
     */
    Transaction(Connection connection) {
      this.connection = connection;
    }

    public Connection getConnection() {
      return connection;
    }

    private void ensureNotFinished() throws SQLException {
      if(finished) {
        throw new SQLException("transaction already ended");
      }
    }

    public Map<String, Object> insert(String tableName, Map<String, Object> parameters) throws SQLException {
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

      try(PreparedStatement statement = connection.prepareStatement("INSERT INTO " + tableName + " (" + fields.toString() + ") VALUES (" + values.toString() + ")", Statement.RETURN_GENERATED_KEYS)) {
        setParameters(parameters, statement);

        System.out.println("[FINE] Database.Transaction.insert() - Inserting into '" + tableName + "': " + parameters);

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

    public int update(String tableName, int id, Map<String, Object> parameters) throws SQLException {
      ensureNotFinished();

      StringBuilder set = new StringBuilder();

      for(String key : parameters.keySet()) {
        if(set.length() > 0) {
          set.append(",");
        }

        set.append(key);
        set.append("=?");
      }

      try(PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " SET " + set.toString() + " WHERE id = ?")) {
        System.out.println("[FINE] Database.Transaction.update() - Updating '" + tableName + "' with id: " + id);

        parameters.put("(irrelevant)", id);

        setParameters(parameters, statement);
        return statement.executeUpdate();
      }
    }

    public int deleteChildren(String tableName, String parentTableName, long parentId) throws SQLException {
      ensureNotFinished();

      try(PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + parentTableName + "_id = ?")) {
        statement.setLong(1, parentId);

        System.out.println("[FINE] Database.Transaction.deleteChildren() - Deleting '" + tableName + "' Children of '" + parentTableName + "' with id " + parentId);

        return statement.executeUpdate();
      }
    }

    public void commit() throws SQLException {
      Database.this.commit(this);
      finished = true;
    }

    public void rollback() throws SQLException {
      Database.this.rollback(this);
      finished = true;
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
