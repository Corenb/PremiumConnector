package eu.horyzon.premiumconnector.task;

import java.sql.SQLException;
import java.util.UUID;

import org.geysermc.floodgate.FloodgateAPI;

import com.github.games647.craftapi.resolver.RateLimitException;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.session.PlayerSession;
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

		PlayerSession playerSession;
		if ( (playerSession = plugin.getPlayerSession().get(name + ip)) != null) {
			plugin.getLogger().fine("Player " + name + " try to connect for the second attempt.");
			if (plugin.isSecondAttempt()) {
				playerSession.setPremium(false);
				plugin.getLogger().fine("Defining connection for player " + name + " to premium.");
				connection.setOnlineMode(playerSession.isPremium());
			} else {
				plugin.getLogger().fine("Event canceled for player " + name + " cause is using a premium username.");
				event.setCancelReason(Message.NOT_PREMIUM_ERROR.getTextComponent());
			}
		} else
			try {
				try {
					playerSession = PlayerSession.loadFromName(name);
					plugin.getLogger().fine("Data successfully loaded from SQL for player " + name);
				} catch (NullPointerException exception) {
					playerSession = new PlayerSession(connection);
					plugin.getLogger().fine(name + " is a new player. Data successfully created.");
					// Check if player is premium
					if (isBedrockPlayer(connection.getUniqueId()))
						plugin.getLogger().fine("Player " + name + " defined as cracked cause is Bedrock player.");
					else if(plugin.getResolver().findProfile(name).isPresent()) {
						plugin.getLogger().fine("Data successfully loaded from Resolver for player" + name);
						playerSession.setPremium(true);

						plugin.getPlayerSession().put(name + ip, playerSession);
						plugin.getLogger().fine("Player " + name + " defined as premium.");
					} else
						plugin.getLogger().fine("Player " + name + " defined as cracked cause no Mojang profile found.");
				}

				connection.setOnlineMode(playerSession.isPremium());
			} catch (RateLimitException exception) {
				exception.printStackTrace();
				plugin.getLogger().warning("Rate limit reached when trying to load " + name + " account.");
				event.setCancelReason(Message.RATE_LIMIT.getTextComponent());
			} catch (SQLException exception) {
				exception.printStackTrace();
				plugin.getLogger().warning("SQL exception when trying to load " + name + " account.");
				event.setCancelReason(Message.MYSQL_ERROR.getTextComponent());
			} catch (Exception exception) {
				exception.printStackTrace();
				plugin.getLogger().warning("Error with Mojang server when trying to load " + name + " account.");
				event.setCancelReason(Message.MOJANG_SERVER_ERROR.getTextComponent());
			}

		event.completeIntent(plugin);
	}

	private boolean isBedrockPlayer(UUID correctedUUID) {
		return plugin.isFloodgate() && FloodgateAPI.isBedrockPlayer(correctedUUID);
	}
}