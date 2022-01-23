package eu.horyzon.premiumconnector.task;

import java.sql.SQLException;

import com.github.games647.craftapi.resolver.RateLimitException;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.session.PlayerSession;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;

public class PremiumCheck implements Runnable {
	private final PremiumConnector plugin;
	private final PreLoginEvent event;

	public PremiumCheck(PremiumConnector plugin, PreLoginEvent event) {
		this.plugin = plugin;
		this.event = event;
	}

	@Override
	public void run() {
		PendingConnection connection = event.getConnection();
		String name = connection.getName();
		String address = connection.getSocketAddress().toString();
		String ip = address.substring(1, address.indexOf(':'));

		plugin.getLogger().fine("Starting premium check for player " + connection.getName() + " logging with ip " + ip);

		try {
			PlayerSession playerSession;

			// Check if a PlayerSession is present in cached data
			if ( (playerSession = plugin.getPlayerSessionManager().getSession(name)) != null) {
				// Check if PlayerSession is defined as premium and is the second attempt connection
				if (playerSession.isPremium() && plugin.getSecondAttempts().remove(name + ip))
					// Check if the plugin allow second attempt
					if (plugin.allowSecondAttempt()) {
						playerSession.setPremium(false);
						plugin.getSQLManager().update(playerSession);

						plugin.getLogger().fine("Player " + name + " try to connect for the second attempt and has been defined as cracked.");
					} else {
						cancel(Message.NOT_PREMIUM_ERROR.getTextComponent());

						plugin.getLogger().fine("Event canceled for player " + name + " cause is using a premium username and second attempt isn't enabled in config file.");
						return;
					}
			} else {
				try {
					// Try to load SQL data
					playerSession = plugin.getSQLManager().loadPlayerSessionFromConnection(name);

					plugin.getLogger().fine("Data successfully loaded from SQL for player " + name + " as " + (playerSession.isPremium() ? "premium" : "cracked") + ".");

					if (plugin.hasGeyserSupport())
						if (!playerSession.hasEditionDefined()) {
							playerSession.setBedrock(plugin.isFromGeyserProxy(ip));
							plugin.getSQLManager().update(playerSession);

							plugin.getLogger().fine("Player " + name + " use " + (playerSession.isBedrock() ? "bedrock" : "java") + " edition.");
						} else if (playerSession.isBedrock() != plugin.isFromGeyserProxy(ip)) {
							plugin.getLogger().fine("Player " + name + " is connecting with a different version than his last visit.");

							cancel(Message.WRONG_EDITION.getTextComponent());
							return;
						}
				} catch (NullPointerException exception) {
					playerSession = new PlayerSession(connection, plugin.hasGeyserSupport() && plugin.isFromGeyserProxy(ip));

					plugin.getLogger().fine("Creating new PlayerSession from PendingConnection with data Name=" + playerSession.getName() + ", Premium=" + playerSession.isPremium() + (plugin.hasGeyserSupport() ? ", Bedrock=" + playerSession.isBedrock() : ""));

					// Check if player is premium through the Mojang API
					if (!playerSession.isBedrock() && plugin.getResolver().findProfile(name).isPresent()) {
						// Define player session as Premium
						playerSession.setPremium(true);

						plugin.getSecondAttempts().add(name + ip);

						plugin.getLogger().fine("Player " + name + " defined as premium by profile resolver.");
					} else
						plugin.getLogger().fine("Player " + name + " defined as cracked cause no Mojang profile found.");

					if (!plugin.allowSecondAttempt())
						plugin.getSQLManager().update(playerSession);
				}

				plugin.getPlayerSessionManager().addSession(playerSession);
			}

			// Define ProxiedPlayer online or offline
			connection.setOnlineMode(playerSession.isPremium());
			event.completeIntent(plugin);
		} catch (RateLimitException exception) {
			exception.printStackTrace();
			plugin.getLogger().warning("Rate limit reached when trying to load " + name + " account.");
			cancel(Message.RATE_LIMIT.getTextComponent());
		} catch (SQLException exception) {
			exception.printStackTrace();
			plugin.getLogger().warning("SQL exception when trying to load " + name + " account.");
			cancel(Message.MYSQL_ERROR.getTextComponent());
		} catch (Exception exception) {
			exception.printStackTrace();
			plugin.getLogger().warning("Error with Mojang server when trying to load " + name + " account.");
			cancel(Message.MOJANG_SERVER_ERROR.getTextComponent());
		}
	}

	private void cancel(TextComponent cancelReason) {
		event.setCancelReason(cancelReason);
		event.setCancelled(true);
		event.completeIntent(plugin);
	}
}