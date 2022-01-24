package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.config.Configuration;

public class SQLiteDataSource extends DataSource {
	private static String SQL_ALTER = "ALTER TABLE %s ADD COLUMN %s BOOLEAN NULL DEFAULT 0;";

	public SQLiteDataSource(PremiumConnector plugin, Configuration configBackend) throws ClassNotFoundException, SQLException {
        super(plugin, configBackend);
    }

    protected void configure(Configuration configBackend) throws RuntimeException {
		String driverClass = configBackend.getString("driver");

		hikariSource.setPoolName("PremiumConnectorSQLitePool");
		hikariSource.setDriverClassName(driverClass);
		hikariSource.setJdbcUrl("jdbc:sqlite://" + plugin.getDataFolder().getAbsolutePath().toString() + "/" + configBackend.getString("database").replace("{pluginDir}", plugin.getDataFolder().getAbsolutePath().toString()) + ".db");
		hikariSource.setConnectionTestQuery("SELECT 1");
		hikariSource.setMaxLifetime(60000); // 60 Sec
		hikariSource.setMaximumPoolSize(50); // 50 Connections (including idle connections)
    }

	protected void connect() throws Exception {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Failed to load SQLite JDBC class. You select SQLite as backend but Bungeecord don't support it per default.\r\nIf you want to use SQLite, you need to install the driver by yourself :\r\nEasy way : https://www.spigotmc.org/resources/sqlite-for-bungeecord.57191/\r\nHard way : https://gist.github.com/games647/d2a57abf90f707c0bd1107e432c580f3", exception);
        }

        plugin.getLogger().info("SQLite driver loaded");
	}

    protected void initDatabase(boolean floodgate) throws SQLException {
		try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
			connection.setAutoCommit(false);

			statement.addBatch(String.format(SQL_CREATE, table));
			if (floodgate) {
				DatabaseMetaData md = connection.getMetaData();

				if (isColumnMissing(md, "Bedrock"))
					statement.addBatch(String.format(SQL_ALTER, table, Columns.NAME.getName()));
			}

			statement.executeBatch();
			connection.commit();
		}
	}

    private boolean isColumnMissing(DatabaseMetaData metaData, String columnName) throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(null, null, table, columnName)) {
            return !resultSet.next();
        }
    }
}