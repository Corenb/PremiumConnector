package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.config.Configuration;

public class SQLiteDataSource extends DataSource {
	private static String SQL_ALTER = "ALTER TABLE %s ADD COLUMN Bedrock BOOLEAN NULL DEFAULT 0;";

	public SQLiteDataSource(PremiumConnector plugin, Configuration configBackend) throws ClassNotFoundException, SQLException {
        super(plugin, configBackend);
    }

    protected HikariConfig configure(Configuration configBackend) {
		HikariConfig config = new HikariConfig();

		config.setPoolName("PremiumConnectorSQLitePool");
		config.setDriverClassName(configBackend.getString("driver"));
		config.setJdbcUrl("jdbc:sqlite://" + plugin.getDataFolder().getAbsolutePath().toString() + "/" + configBackend.getString("database").replace("{pluginDir}", plugin.getDataFolder().getAbsolutePath().toString()) + ".db");
		config.setConnectionTestQuery("SELECT 1");
		config.setMaxLifetime(60000); // 60 Sec
		config.setMaximumPoolSize(50); // 50 Connections (including idle connections)

		return config;
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
				statement.addBatch(String.format(SQL_ALTER, table));
			}

			statement.executeBatch();
			connection.commit();
		}
	}
}