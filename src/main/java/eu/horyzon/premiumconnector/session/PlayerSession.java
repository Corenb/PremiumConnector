package eu.horyzon.premiumconnector.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.github.games647.craftapi.model.Profile;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.sql.DataSource;
import net.md_5.bungee.api.connection.PendingConnection;

public class PlayerSession {
	private static String SQL_SELECT = "SELECT Premium FROM %s WHERE Name='%s';",
			SQL_UPDATE = "INSERT INTO %s VALUES('%s', %3$b) ON DUPLICATE KEY UPDATE Premium = %3$b;";

	protected String name;
	protected boolean premium = false;
	protected boolean online = false;

	protected PlayerSession(String name, boolean premium) {
		this.name = name;
		this.premium = premium;
	}

	public PlayerSession(Profile profile) {
		this(profile.getName(), true);
		PremiumConnector.getInstance().getLogger()
				.fine("Creating new PlayerSession from Profile with data name=" + name + ", premium=" + true);
	}

	public PlayerSession(PendingConnection connection) throws SQLException {
		this(connection.getName(), connection.isOnlineMode());
		PremiumConnector.getInstance().getLogger()
				.fine("Creating new PlayerSession from PendingConnection with data name=" + name + ", premium="
						+ connection.isOnlineMode());
	}

	public PlayerSession(PendingConnection connection, boolean premium) throws SQLException {
		this(connection.getName(), premium);
		PremiumConnector.getInstance().getLogger().fine(
				"Creating new PlayerSession from PendingConnection with data name=" + name + ", premium=" + premium);
	}

	public static PlayerSession loadFromName(String name) throws NullPointerException, SQLException {
		DataSource source = PremiumConnector.getInstance().getDataSource();
		try (Connection con = source.getConnection(); Statement createStmt = con.createStatement()) {
			ResultSet result = createStmt.executeQuery(String.format(SQL_SELECT, source.getTable(), name));
			if (result.next())
				return new PlayerSession(name, result.getBoolean("premium"));
		}

		throw new NullPointerException();
	}

	public void update() throws SQLException {
		DataSource source = PremiumConnector.getInstance().getDataSource();
		try (Connection con = source.getConnection(); Statement createStmt = con.createStatement()) {
			String statement = String.format(SQL_UPDATE, source.getTable(), name, premium);
			PremiumConnector.getInstance().getLogger().fine("Executing SQL update '" + statement + "'");
			createStmt.executeUpdate(statement);
		}
	}

	public String getName() {
		return name;
	}

	public void setPremium(boolean premium) {
		this.premium = premium;
	}

	public boolean isPremium() {
		return premium;
	}
}