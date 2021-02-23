package eu.horyzon.premiumconnector.redirect;

import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerRedirect implements AuthRedirect {
	private final ServerInfo crackedServer;

	public ServerRedirect(ServerInfo server) {
		crackedServer = server;
	}

	@Override
	public void sendToAuth(ProxiedPlayer player) {
		player.connect(crackedServer);
	}
}