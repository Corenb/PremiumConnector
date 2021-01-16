package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.horyzon.premiumconnector.PremiumConnector;

public class DataSource {
	private static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS %s (Name VARCHAR(15) NOT NULL, Premium BOOLEAN, PRIMARY KEY (Name));";

	private String table;
	private HikariDataSource hikari;

	public DataSource(PremiumConnector plugin, String driver, String host, int port, String user, String password, String database, String table, boolean useSSL) throws SQLException {
		this.table = table;

		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driver);

		String jdbcUrl = "jdbc:";
		if (config.getDriverClassName().contains("sqlite")) {
			database = database.replace("{pluginDir}", plugin.getDataFolder().getAbsolutePath().toString());

			jdbcUrl += "sqlite://" + plugin.getDataFolder().getAbsolutePath().toString() + "/" + database;
			config.setMaximumPoolSize(1);
		} else {
			jdbcUrl += (config.getDriverClassName().contains("mariadb") ? "mariadb" : "mysql") + "://" + host + ':' + port + '/' + database;
			plugin.getLogger().fine("Configuring jdbc url to " + jdbcUrl);

			config.setUsername(user);
			config.setPassword(password);
			config.addDataSourceProperty("useSSL", useSSL);
			config.addDataSourceProperty("cachePrepStmts", "true");
			config.addDataSourceProperty("prepStmtCacheSize", "250");
			config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		}

		config.setJdbcUrl(jdbcUrl);

		hikari = new HikariDataSource(config);

		initDatabase();
	}

	public Connection getConnection() throws SQLException {
		return hikari.getConnection();
	}

	private void initDatabase() throws SQLException {
		try (Connection con = getConnection(); Statement createStmt = con.createStatement()) {
			createStmt.executeUpdate(String.format(SQL_CREATE, table));
		}
	}

	public String getTable() {
		return table;
	}
}