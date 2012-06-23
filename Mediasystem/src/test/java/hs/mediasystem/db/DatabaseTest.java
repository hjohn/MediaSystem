package hs.mediasystem.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hs.mediasystem.db.Database.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DatabaseTest {
  private static final Date DATE = new Date();

  private Provider<Connection> connectionProvider;
  private Database database;

  @Mock private Connection connection;
  @Mock private Savepoint savepoint;
  @Mock private PreparedStatement statement;
  @Mock private ResultSet resultSet;
  @Mock private ResultSetMetaData resultSetMetaData;

  @Before
  public void before() throws SQLException {
    MockitoAnnotations.initMocks(this);

    when(connection.setSavepoint()).thenReturn(savepoint);
    when(connection.prepareStatement(Matchers.anyString())).thenReturn(statement);
    when(connection.prepareStatement(Matchers.anyString(), Matchers.anyInt())).thenReturn(statement);

    when(statement.getGeneratedKeys()).thenReturn(resultSet);

    when(resultSet.next()).thenReturn(true).thenReturn(false);
    when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
    when(resultSet.getObject(1)).thenReturn(1001);

    when(resultSetMetaData.getColumnCount()).thenReturn(1);
    when(resultSetMetaData.getColumnName(1)).thenReturn("id");

    connectionProvider = new Provider<Connection>() {
      @Override
      public Connection get() {
        return connection;
      }
    };

    database = new Database(connectionProvider);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldAutoRollbackTransaction() throws SQLException {
    try(Transaction transaction = database.beginTransaction()) {
      throw new IllegalArgumentException();
    }
    finally {
      verify(connection).rollback();
      verify(connection, never()).commit();
    }
  }

  @Test
  public void shouldCommitTransaction() throws SQLException {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.commit();
    }

    verify(connection).commit();
    verify(connection, never()).rollback();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowCommitAfterRollback() {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.rollback();
      transaction.commit();
    }
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowRollbackAfterCommit() {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.commit();
      transaction.rollback();
    }
  }

  @Test
  public void shouldAllowNestedTransaction() throws SQLException {
    try(Transaction transaction = database.beginTransaction()) {
      try(Transaction nestedTransaction = database.beginTransaction()) {
        nestedTransaction.commit();
      }

      verify(connection, never()).rollback();
      verify(connection, never()).commit();

      transaction.commit();
    }

    verify(connection).commit();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowUncommitedNestedTransactions() {
    try(Transaction transaction = database.beginTransaction()) {
      try(Transaction nestedTransaction = database.beginTransaction()) {
        transaction.commit();
      }
    }
  }

  @Test
  public void shouldInsertRowAndReturnedGeneratedKey() throws SQLException {
    try(Transaction transaction = database.beginTransaction()) {
      Map<String, Object> keys = transaction.insert("items", new LinkedHashMap<String, Object>() {{
        put("childcount", 2);
        put("creationtime", DATE);
      }});

      assertTrue(keys.containsKey("id"));
      assertEquals(1001, keys.get("id"));
    }

    verify(connection).prepareStatement("INSERT INTO items (childcount,creationtime) VALUES (?,?)", Statement.RETURN_GENERATED_KEYS);
    verify(statement).setObject(1, 2);
    verify(statement).setTimestamp(2, new Timestamp(DATE.getTime()));
  }

  @Test
  public void shouldUpdateRow() throws SQLException {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.update("items", 15, new LinkedHashMap<String, Object>() {{
        put("childcount", 2);
        put("changetime", DATE);
      }});
    }

    verify(connection).prepareStatement("UPDATE items SET childcount=?,changetime=? WHERE id = ?");
    verify(statement).setObject(1, 2);
    verify(statement).setTimestamp(2, new Timestamp(DATE.getTime()));
    verify(statement).setObject(3, 15);
  }

  @Test
  public void shouldDeleteChildren() throws SQLException {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.deleteChildren("castings", "items", 15);
    }

    verify(connection).prepareStatement("DELETE FROM castings WHERE items_id = ?");
    verify(statement).setLong(1, 15);
  }
}
