package eu.horyzon.premiumconnector.session;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.sql.DataSource;
import net.md_5.bungee.api.connection.PendingConnection;

public class PlayerSession {
	private static String SQL_SELECT = "SELECT Premium FROM %s WHERE Name='%s';",
			SQL_UPDATE = "INSERT INTO %s(Name, Premium) VALUES('%s', %3$b) ON DUPLICATE KEY UPDATE Premium = %3$b;",
			SQL_DELETE = "DELETE FROM %s WHERE Name='%s';";

	protected String name;
	protected boolean premium;

	protected PlayerSession(String name, boolean premium) {
		this.name = name;
		this.premium = premium;
	}

	public PlayerSession(PendingConnection connection) throws SQLException {
		this(connection.getName(), connection.isOnlineMode());
		PremiumConnector.getInstance().getLogger().fine("Creating new PlayerSession from PendingConnection with data Name=" + name + ", Premium=" + premium);
	}

	public static PlayerSession loadFromName(String name) throws NullPointerException, SQLException {
		DataSource source = PremiumConnector.getInstance().getDataSource();
		try (Connection con = source.getConnection(); Statement createStmt = con.createStatement()) {
			ResultSet result = createStmt.executeQuery(String.format(SQL_SELECT, source.getTable(), name));
			if (result.next())
				return new PlayerSession(name, result.getBoolean("Premium"));
		}

		throw new NullPointerException();
	}

	public void update() throws SQLException {
		DataSource source = PremiumConnector.getInstance().getDataSource();
		try (Connection connection = source.getConnection(); Statement createStmt = connection.createStatement()) {
			String statement = String.format(SQL_UPDATE, source.getTable(), name, premium);
			PremiumConnector.getInstance().getLogger().fine("Executing SQL update '" + statement + "'");
			createStmt.executeUpdate(statement);
		}
	}

	public void delete() throws SQLException {
		DataSource source = PremiumConnector.getInstance().getDataSource();
		try (Connection connection = source.getConnection(); Statement createStmt = connection.createStatement()) {
			String statement = String.format(SQL_DELETE, source.getTable(), name);
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