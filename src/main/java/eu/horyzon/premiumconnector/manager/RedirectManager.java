package eu.horyzon.premiumconnector.manager;

import java.util.HashMap;
import java.util.Map;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.redirect.AuthRedirect;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

public class RedirectManager {
	private final PremiumConnector plugin;
	private final AuthRedirect authRedirect;
	private final Map<String, ServerInfo> pendingRedirections = new HashMap<>();

	public RedirectManager(PremiumConnector plugin, AuthRedirect authRedirect) {
		this.plugin = plugin;
		this.authRedirect = authRedirect;
	}

	public boolean isRedirectionWaiting(String username) {
		return pendingRedirections.containsKey(username.toLowerCase());
	}

	public void sendToAuth(ProxiedPlayer player, ServerInfo target) {
		pendingRedirections.put(player.getName().toLowerCase(), target);
		authRedirect.sendToAuth(player);
	}

	public void redirect(String username) {
		connect(plugin.getProxy().getPlayer(username), getServer(username));
	}

	public void redirect(ProxiedPlayer player) {
		connect(player, getServer(player.getName()));
	}

	private ServerInfo getServer(String username) {
		return pendingRedirections.remove(username.toLowerCase());
	}

	private void connect(ProxiedPlayer player, ServerInfo server) {
		if (server != null)
			((UserConnection) player).connect(server, null, true, ServerConnectEvent.Reason.PLUGIN);
	}
}