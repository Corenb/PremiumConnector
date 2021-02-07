package eu.horyzon.premiumconnector.listeners;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.task.PremiumCheck;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PreLoginListener implements Listener {
	private PremiumConnector plugin;

	public PreLoginListener(PremiumConnector plugin) {
		this.plugin = plugin;
	}

	@EventHandler()
	public void onPreLogin(PreLoginEvent event) {
		if (event.isCancelled())
			return;

		event.registerIntent(plugin);
		plugin.getProxy().getScheduler().runAsync(plugin, new PremiumCheck(plugin, event));
	}
}