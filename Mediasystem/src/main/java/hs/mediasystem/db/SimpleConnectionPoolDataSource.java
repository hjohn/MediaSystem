package hs.mediasystem.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.ConnectionPoolDataSource;
import javax.sql.PooledConnection;

public class SimpleConnectionPoolDataSource implements ConnectionPoolDataSource {
  private final String url;

  private String userName;
  private String password;
  private Properties properties;

  public SimpleConnectionPoolDataSource(String url) {
    this.url = url;
  }

  public SimpleConnectionPoolDataSource(String url, String userName, String password) {
    this.url = url;
    this.userName = userName;
    this.password = password;
  }

  public SimpleConnectionPoolDataSource(String url, Properties properties) {
    this.url = url;
    this.properties = properties;
  }

  private Connection getConnection() throws SQLException {
    if(properties != null) {
      return DriverManager.getConnection(url, properties);
    }
    else if(userName != null || password != null) {
      return DriverManager.getConnection(url, userName, password);
    }

    return DriverManager.getConnection(url);
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getLoginTimeout() throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    throw new UnsupportedOperationException();
  }

  @Override
  public PooledConnection getPooledConnection() throws SQLException {
    return new SimplePooledConnection(getConnection());
  }

  @Override
  public PooledConnection getPooledConnection(String user, String password) throws SQLException {
    throw new UnsupportedOperationException();
  }
}
