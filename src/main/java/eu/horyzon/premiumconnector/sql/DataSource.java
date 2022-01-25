package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.zaxxer.hikari.HikariDataSource;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.session.PlayerSession;
import net.md_5.bungee.config.Configuration;

public abstract class DataSource {
	protected static String SQL_CREATE = "CREATE TABLE IF NOT EXISTS %s (" + Columns.NAME.getName() + " VARCHAR(16) NOT NULL, " + Columns.PREMIUM.getName() + " BOOLEAN, PRIMARY KEY (" + Columns.NAME.getName() + "));", SQL_ALTER = "ALTER TABLE %s ADD COLUMN " + Columns.BEDROCK.getName() + " BOOLEAN NULL DEFAULT 0;", SQL_DELETE = "DELETE FROM %s WHERE " + Columns.NAME.getName() + "=?;", SQL_SELECT = "SELECT " + Columns.PREMIUM.getName() + " FROM %s WHERE " + Columns.NAME.getName() + "=?;", SQL_UPDATE = "REPLACE INTO %s(" + Columns.NAME.getName() + ", " + Columns.PREMIUM.getName() + ") VALUES(?, ?);";

	protected PremiumConnector plugin;
	protected String table;
	protected HikariDataSource hikariSource;

	public DataSource(PremiumConnector plugin, Configuration configBackend) throws SQLException {
		this.plugin = plugin;
		table = configBackend.getString("table");
		hikariSource = new HikariDataSource();

		if (plugin.hasGeyserSupport()) {
			SQL_SELECT = "SELECT " + Columns.PREMIUM.getName() + ", " + Columns.BEDROCK.getName() + " FROM %s WHERE " + Columns.NAME.getName() + "=?;";
			SQL_UPDATE = "REPLACE INTO %s(" + Columns.NAME.getName() + ", " + Columns.PREMIUM.getName() + ", " + Columns.BEDROCK.getName() + ") VALUES(?, ?, ?);";
		}

        try {
			configure(configBackend);
        } catch (RuntimeException exception) {
            if (exception instanceof IllegalArgumentException) {
                plugin.getLogger().warning("Invalid database arguments! Please check your configuration!\nIf this error persists, please report it to the developer!");
            }

            plugin.getLogger().warning("Can't use the Hikari Connection Pool! Please, report this error to the developer!");
            throw exception;
        }

        try {
            initDatabase();
			plugin.getLogger().info("SQL initialization success");
        } catch (SQLException exception) {
            plugin.getLogger().warning("Error during SQlite initialization. Please check your database informations.");
			throw exception;
        }
	}

	protected abstract void configure(Configuration configBackend) throws RuntimeException;

    protected void initDatabase() throws SQLException {
		try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
			connection.setAutoCommit(false);

			statement.addBatch(String.format(SQL_CREATE, table));
			if (plugin.hasGeyserSupport()) {
				DatabaseMetaData metaData = connection.getMetaData();

				if (isColumnMissing(metaData, Columns.BEDROCK.getName()))
					statement.addBatch(String.format(SQL_ALTER, table));
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

	protected Connection getConnection() throws SQLException {
		return hikariSource.getConnection();
	}

    public void closeConnection() {
        if (hikariSource != null && !hikariSource.isClosed())
			hikariSource.close();
    }

	public void update(PlayerSession playerSession) throws SQLException {
		String statement = String.format(SQL_UPDATE, table);

		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
			preparedStatement.setString(1, playerSession.getName());
			preparedStatement.setBoolean(2, playerSession.isPremium());

			if (plugin.hasGeyserSupport()) {
				preparedStatement.setBoolean(3, playerSession.isBedrock());
			}

			preparedStatement.executeUpdate();
		}
	}

	public void delete(PlayerSession playerSession) throws SQLException {
		String statement = String.format(SQL_DELETE, table);

		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
			preparedStatement.setString(1, playerSession.getName());

			preparedStatement.executeUpdate();
		}
	}

	public PlayerSession loadPlayerSessionFromConnection(String username) throws NullPointerException, SQLException {
		String statement = String.format(SQL_SELECT, table);

		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(statement)) {
			preparedStatement.setString(1, username);

			ResultSet result = preparedStatement.executeQuery();
			if (result.next()) {
				Boolean bedrock = false;
				if (plugin.hasGeyserSupport() && ! (bedrock = result.getBoolean(Columns.BEDROCK.getName())) && result.wasNull())
					bedrock = null;

				return new PlayerSession(username, result.getBoolean(Columns.PREMIUM.getName()), bedrock);
			}
		}

		throw new NullPointerException();
	}
}