package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.config.Configuration;

public class MySQLDataSource extends DataSource {
	protected static String SQL_ALTER = "ALTER TABLE %s ADD IF NOT EXISTS Bedrock BOOLEAN;";

	public MySQLDataSource(PremiumConnector plugin, Configuration configBackend) throws SQLException {
		super(plugin, configBackend);
	}

	@Override
	protected HikariConfig configure(Configuration configBackend) {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(configBackend.getString("driver"));

		String jdbcUrl = "jdbc:" + (config.getDriverClassName().contains("mariadb") ? "mariadb" : "mysql") + "://" + configBackend.getString("host") + ':' + configBackend.getInt("port", 3306) + '/' + configBackend.getString("database");
		plugin.getLogger().fine("Configuring jdbc url to " + jdbcUrl);

		config.setJdbcUrl(jdbcUrl);
		config.setUsername(configBackend.getString("user"));
		config.setPassword(configBackend.getString("password"));
		config.addDataSourceProperty("useSSL", configBackend.getBoolean("useSSL", true));
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

		return config;
	}

	protected void initDatabase(boolean bedrockSupport) throws SQLException {
		try (Connection connection = getConnection(); Statement createStmt = connection.createStatement()) {
			connection.setAutoCommit(false);

			createStmt.addBatch(String.format(SQL_CREATE, table));
			if (bedrockSupport)
				createStmt.addBatch(String.format(SQL_ALTER, table));

			createStmt.executeBatch();
		}
	}
}