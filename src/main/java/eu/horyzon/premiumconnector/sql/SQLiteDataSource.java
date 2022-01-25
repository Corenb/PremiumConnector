package eu.horyzon.premiumconnector.sql;

import java.sql.SQLException;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.config.Configuration;

public class SQLiteDataSource extends DataSource {

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
}