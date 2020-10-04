package eu.horyzon.premiumconnector.listeners;

import eu.horyzon.premiumconnector.PremiumConnector;
import ml.karmaconfigs.LockLogin.BungeeCord.API.PlayerAPI;
import ml.karmaconfigs.LockLogin.BungeeCord.API.Events.PlayerRegisterEvent;
import ml.karmaconfigs.LockLogin.BungeeCord.API.Events.PlayerVerifyEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class LockLoginListener implements Listener {
	private final PremiumConnector plugin;

	public LockLoginListener(PremiumConnector plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerRegister(PlayerRegisterEvent event) {
		String name = event.getPlayer().getName();
		plugin.redirect(name.toLowerCase());
		plugin.getLogger().fine("Plugin receive register event from LockLogin for player " + name + ".");
	}

	@EventHandler
	public void onPlayerVerify(PlayerVerifyEvent event) {
		String name = event.getPlayer().getName();
		plugin.redirect(name.toLowerCase());
		plugin.getLogger().fine("Plugin receive login event from LockLogin for player " + name + ".");
	}

	@EventHandler
	public void onPlayerLogin(PostLoginEvent event) {
		ProxiedPlayer player = event.getPlayer();

		if (player.getPendingConnection().isOnlineMode())
			new PlayerAPI(player).setLogged(true);
	}
}