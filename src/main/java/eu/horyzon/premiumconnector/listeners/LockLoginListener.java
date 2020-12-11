package eu.horyzon.premiumconnector.listeners;

import org.jetbrains.annotations.NotNull;

import eu.horyzon.premiumconnector.PremiumConnector;
import ml.karmaconfigs.lockloginmodules.bungee.Module;
import ml.karmaconfigs.lockloginmodules.bungee.ModuleLoader;
import ml.karmaconfigs.lockloginmodules.shared.NoJarException;
import ml.karmaconfigs.lockloginmodules.shared.NoPluginException;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.PlayerAPI;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerRegisterEvent;
import ml.karmaconfigs.lockloginsystem.bungeecord.api.events.PlayerVerifyEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class LockLoginListener extends Module implements Listener {
	private final PremiumConnector plugin;

	public LockLoginListener(PremiumConnector plugin) {
		this.plugin = plugin;
		Module module = new LockLoginListener(this);

		/*
		 * ModuleLoader package depends on what platform are you in, if you are in bungee, use ml.karmaconfigs.lockloginmodules.bungee but if you are in spigot, use
		 * ml.karmaconfigs.lockloginmodules.spigot
		 */
		ModuleLoader loader = new ModuleLoader(module);

		//Check if the module is already loaded
		try {
			loader.inject();
		} catch (IOException | NoJarException | NoPluginException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public @NotNull String author() {
		return "LLIcocoman_Hrz";
	}

	@Override
	public @NotNull String author_url() {
		return "https://www.spigotmc.org/members/llicocoman.26529/#resources";
	}

	@Override
	public @NotNull String description() {
		return "This is the module used to access LockLogin API";
	}

	@Override
	public @NotNull String name() {
		return "PremiumConnector module";
	}

	@Override
	public @NotNull Plugin owner() {
		return plugin;
	}

	@Override
	public @NotNull String version() {
		return "1.0.0";
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
			new PlayerAPI(this, player).setLogged(true);
	}
}