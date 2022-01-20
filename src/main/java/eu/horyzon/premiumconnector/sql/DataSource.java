package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.config.Configuration;

public abstract class DataSource {
	protected static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS %s (Name VARCHAR(16) NOT NULL, Premium BOOLEAN, PRIMARY KEY (Name));";

	protected PremiumConnector plugin;
	protected String table;
	protected HikariDataSource hikariSource;

	public DataSource(PremiumConnector plugin, Configuration configBackend) throws SQLException {
		this.plugin = plugin;
		hikariSource = new HikariDataSource(configure(configBackend));

		table = configBackend.getString("table");

        try {
            initDatabase(plugin.hasGeyserSupport());
			plugin.getLogger().info("SQL initialization success");
        } catch (Exception exception) {
            plugin.getLogger().warning("Error during SQlite initialization. Please check your database informations.");
			throw exception;
        }
	}

	protected abstract HikariConfig configure(Configuration configBackend);

	protected abstract void initDatabase(boolean bedrockSupport) throws SQLException;

	public Connection getConnection() throws SQLException {
		return hikariSource.getConnection();
	}

    public void closeConnection() {
        if (hikariSource != null && !hikariSource.isClosed())
			hikariSource.close();
    }

	public String getTable() {
		return table;
	}
}