package hs.mediasystem.db;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Provider;

public class DatabaseUpdater {
  private final Provider<Connection> connectionProvider;

  @Inject
  public DatabaseUpdater(Provider<Connection> connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  public void updateDatabase() {
    int version = getDatabaseVersion();

    try {
      for(;;) {
        version++;

        try(InputStream sqlStream = getClass().getClassLoader().getResourceAsStream("hs/mediasystem/db/db-v" + version + ".sql")) {
          if(sqlStream == null) {
            version--;
            break;
          }

          System.out.println("[INFO] Updating database to version " + version);

          applyUpdateScript(version, sqlStream);
        }
      }

      System.out.println("[INFO] Database up to date at version " + version);
    }
    catch(IOException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void applyUpdateScript(int version, InputStream sqlStream) throws SQLException, IOException {
    try(Connection connection = connectionProvider.get()) {
      try {
        connection.setAutoCommit(false);

        try(LineNumberReader reader = new LineNumberReader(new InputStreamReader(sqlStream))) {

          statementExecuteLoop:
          for(;;) {
            String sqlStatement = "";

            while(!sqlStatement.endsWith(";")) {
              String line = reader.readLine();

              if(line == null) {
                if(!sqlStatement.trim().isEmpty()) {
                  throw new RuntimeException("unexpected EOF in db-v" + version + ".sql: " + sqlStatement);
                }

                break statementExecuteLoop;
              }

              sqlStatement += line;
            }

            try(PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
              System.out.println("[FINE] " + sqlStatement);
              statement.execute();
            }
          }
        }

        try(PreparedStatement statement = connection.prepareStatement("UPDATE dbinfo SET value = '" + version + "' WHERE name = 'version'")) {
          if(statement.executeUpdate() != 1) {
            throw new RuntimeException("unable to update version information to " + version);
          }
        }

        connection.commit();
      }
      finally {
        connection.rollback();
      }
    }
  }

  private int getDatabaseVersion() {
    int version = 0;

    try(Connection connection = connectionProvider.get();
        PreparedStatement statement = connection.prepareStatement("SELECT value FROM dbinfo WHERE name = 'version'")) {

      try(ResultSet rs = statement.executeQuery()) {
        if(rs.next()) {
          version = Integer.parseInt(rs.getString("value"));
        }
      }
    }
    catch(SQLException e) {
      throw new RuntimeException("unable to get version information from the database", e);
    }

    return version;
  }
}
