package eu.horyzon.premiumconnector.listeners;

import java.sql.SQLException;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent.Reason;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ServerConnectListener implements Listener {
	private PremiumConnector plugin;

	public ServerConnectListener(PremiumConnector plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onServerConnect(ServerConnectEvent event) {
		if (event.isCancelled() || event.getReason() != Reason.JOIN_PROXY)
			return;

		ProxiedPlayer player = event.getPlayer();
		String name = player.getName();
		String host = player.getPendingConnection().getVirtualHost().getHostName();
		plugin.getLogger().fine("Player " + name + " is joining on host " + host);

		if (!player.getPendingConnection().isOnlineMode()) {
			plugin.getRedirectionRequests().put(name.toLowerCase(), event.getTarget());
			plugin.getLogger().fine("Cracked player " + name + " was redirected on the cracked server "
					+ plugin.getCrackedServer().getName());
			event.setTarget(plugin.getCrackedServer());
		}

		try {
			if (plugin.getPlayerSession().containsKey(name + player.getSocketAddress()))
				plugin.getPlayerSession().remove(name + player.getSocketAddress()).update();
		} catch (SQLException e) {
			e.printStackTrace();
			plugin.getLogger().warning("SQL error on updating player" + name);
		}
	}
}
