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
			event.setCancelled(plugin.isBlockServerSwitch() && plugin.getRedirectionRequests().containsKey(name.toLowerCase()));
			return;
		}

		PlayerSession playerSession = plugin.getPlayerSession().get(name);

		if (!playerSession.isPremium() && !playerSession.isBedrock()) {
			plugin.getRedirectionRequests().put(name.toLowerCase(), event.getTarget());
			plugin.getLogger().fine("Cracked player " + name + " was redirected on the cracked server " + plugin.getCrackedServer().getName());
			event.setTarget(plugin.getCrackedServer());
			return;
		}

		try {
			if (playerSession.isSecondAttempt()) {
				plugin.getSQLManager().update(playerSession);
				playerSession.setSecondAttempt(false);
				plugin.getLogger().fine("Player " + name + " confirmed as cracked with second attempt.");
			}
		} catch (SQLException exception) {
			exception.printStackTrace();
			plugin.getLogger().warning("SQL error on updating player" + name);
		}
	}
}