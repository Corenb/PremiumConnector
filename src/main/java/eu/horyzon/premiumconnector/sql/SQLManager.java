package eu.horyzon.premiumconnector.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.session.PlayerSession;

public class SQLManager {
	private final PremiumConnector plugin;
	private final DataSource source;
	private final String SQLSelect,
			SQLUpdate,
			SQLDelete;

	public SQLManager(PremiumConnector plugin, DataSource source) {
		this.plugin = plugin;
		this.source = source;

		SQLSelect = "SELECT Premium" + (plugin.hasGeyserSupport() ? ", Bedrock" : "") + " FROM %s WHERE Name='%s';";
		SQLUpdate = "INSERT INTO %s(Name, Premium" + (plugin.hasGeyserSupport() ? ", Bedrock" : "") + ") VALUES('%s', %3$b" + (plugin.hasGeyserSupport() ? ", %4$b" : "") + ") ON DUPLICATE KEY UPDATE Premium = %3$b" + (plugin.hasGeyserSupport() ? ", Bedrock = %4$b" : "") + ";";
		SQLDelete = "DELETE FROM %s WHERE Name='%s';";
	}

	public PlayerSession loadPlayerSessionFromConnection(String username) throws NullPointerException, SQLException {
		try (Connection connection = source.getConnection(); Statement statement = connection.createStatement()) {
			ResultSet result = statement.executeQuery(String.format(SQLSelect, source.getTable(), username));

			if (result.next()) {
				Boolean bedrock = false;
				if (plugin.hasGeyserSupport() && ! (bedrock = result.getBoolean("Bedrock")) && result.wasNull())
					bedrock = null;

				return new PlayerSession(username, result.getBoolean("Premium"), bedrock);
			}
		}

		throw new NullPointerException();
	}

	public void update(PlayerSession playerSession) throws SQLException {
		try (Connection connection = source.getConnection()) {
			String statement = String.format(SQLUpdate, source.getTable(), playerSession.getName(), playerSession.isPremium(), playerSession.isBedrock());

			plugin.getLogger().fine("Executing SQL update '" + statement + "'");
			connection.prepareStatement(statement).executeUpdate();
		}
	}

	public void delete(PlayerSession playerSession) throws SQLException {
		try (Connection connection = source.getConnection(); Statement createStmt = connection.createStatement()) {
			String statement = String.format(SQLDelete, source.getTable(), playerSession.getName());

			plugin.getLogger().fine("Executing SQL update '" + statement + "'");
			createStmt.executeUpdate(statement);
		}
	}
}