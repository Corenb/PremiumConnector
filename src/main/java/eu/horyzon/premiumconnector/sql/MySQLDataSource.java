package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.config.Configuration;

public class MySQLDataSource extends DataSource {
	protected static String SQL_ALTER = "ALTER TABLE %s ADD IF NOT EXISTS Bedrock BOOLEAN;";

	public MySQLDataSource(PremiumConnector plugin, Configuration configBackend) throws SQLException {
		super(plugin, configBackend);
	}

	@Override
	protected void configure(Configuration configBackend) throws RuntimeException {
		String driverClass = configBackend.getString("driver");
		String jdbcUrl = "jdbc:" + (driverClass.contains("mariadb") ? "mariadb" : "mysql") + "://" + configBackend.getString("host") + ':' + configBackend.getInt("port", 3306) + '/' + configBackend.getString("database");

		hikariSource.setDriverClassName(driverClass);
		hikariSource.setJdbcUrl(jdbcUrl);
		hikariSource.setUsername(configBackend.getString("user"));
		hikariSource.setPassword(configBackend.getString("password"));
		hikariSource.addDataSourceProperty("useSSL", configBackend.getBoolean("useSSL", true));
		hikariSource.addDataSourceProperty("cachePrepStmts", "true");
		hikariSource.addDataSourceProperty("prepStmtCacheSize", "250");
		hikariSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
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