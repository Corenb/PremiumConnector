package eu.horyzon.premiumconnector.listeners;

import java.sql.SQLException;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.session.PlayerSession;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerConnectListener implements Listener {
	private PremiumConnector plugin;

	public ServerConnectListener(PremiumConnector plugin) {
		this.plugin = plugin;
	}

	@EventHandler()
	public void onServerConnect(ServerConnectEvent event) {
		// Skip if event was cancelled
		if (event.isCancelled())
			return;

		ProxiedPlayer player = event.getPlayer();
		String name = player.getName();

		// Skip if player is waiting for a redirection (means is connected to the auth server)
		if (event.getReason() != Reason.JOIN_PROXY) {
			event.setCancelled(plugin.isBlockServerSwitch() && plugin.getRedirectManager().isRedirectionWaiting(name));
			return;
		}

		PlayerSession playerSession = plugin.getPlayerSessionManager().getSession(name);

		if (playerSession.isPremium()) {
			String address = player.getPendingConnection().getSocketAddress().toString();
			String ip = address.substring(1, address.indexOf(':'));
			if (plugin.getSecondAttempts().remove(name + ip)) {
				try {
					plugin.getDataSource().update(playerSession);
				} catch (SQLException exception) {
					exception.printStackTrace();
					plugin.getLogger().warning("SQL error on updating player" + name);
				}

				plugin.getLogger().fine("Player " + name + " confirmed as premium player.");
			}
		} else if (!playerSession.isBedrock())
			plugin.getRedirectManager().sendToAuth(player, event.getTarget());
	}
}