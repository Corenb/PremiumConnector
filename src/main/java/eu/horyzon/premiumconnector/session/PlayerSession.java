package eu.horyzon.premiumconnector.session;

import java.sql.SQLException;

import net.md_5.bungee.api.connection.PendingConnection;

public class PlayerSession {
	protected final String name;
	protected boolean premium;
	protected Boolean bedrock;

	public PlayerSession(String name, boolean premium, Boolean bedrock) {
		this.name = name;
		this.premium = premium;
		this.bedrock = bedrock;
	}

	public PlayerSession(PendingConnection connection, boolean bedrock) throws SQLException {
		this(connection.getName(), connection.isOnlineMode(), bedrock);
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

	public void setBedrock(boolean bedrock) {
		this.bedrock = bedrock;
	}

	public boolean isBedrock() {
		return hasEditionDefined() && bedrock;
	}

	public boolean hasEditionDefined() {
		return bedrock != null;
	}
}