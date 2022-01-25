package eu.horyzon.premiumconnector.command;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.session.PlayerSession;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CommandBase extends Command {
	private final PremiumConnector plugin;
	private final CommandType command;
	private final int cooldown;
	private final Map<UUID, Long> confirm = new HashMap<>();

	public CommandBase(PremiumConnector plugin, CommandType command) {
		super(command.getCommandName(), command.getPermission(), command.getAliases());
		this.plugin = plugin;
		this.command = command;
		cooldown = plugin.getConfig().getInt("timeToConfirm", 30);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (args.length > 0) {
			if (!sender.hasPermission("premiumconnector.admin"))
				Message.NO_PERMISSION.sendMessage(sender);
			else
				update(sender, args[0], command);
		} else if (sender instanceof ProxiedPlayer) {
			UUID playerUUID = ((ProxiedPlayer) sender).getUniqueId();
			if (command != CommandType.PREMIUM || canConfirm(confirm.remove(playerUUID)))
				update(sender, sender.getName(), command);
			else {
				Message.WARN_COMMAND.sendMessage(sender, "%cmd%", command.getCommandName(), "%status%", command.getStatus().toString());
				confirm.put(playerUUID, System.currentTimeMillis());
			}
		} else
			Message.CONSOLE_COMMAND.sendMessage(sender, "%cmd%", command.getCommandName());
	}

	protected void update(CommandSender sender, String playerName, CommandType command) {
		try {
			PlayerSession playerSession = plugin.getPlayerSessionManager().getSession(playerName);
			if (playerSession == null)
				playerSession = plugin.getDataSource().loadPlayerSessionFromConnection(playerName);

			if ((playerSession.isPremium() && command == CommandType.PREMIUM) || (!playerSession.isPremium() && command == CommandType.CRACKED))
				(sender.getName().equals(playerName) ? Message.ALREADY_DEFINED : Message.ALREADY_DEFINED_OTHER).sendMessage(sender, "%player%", playerName, "%status%", command.getStatus().toString());
			else {
				if (command == CommandType.RESET) {
					plugin.getPlayerSessionManager().removeSession(playerName);
					plugin.getDataSource().delete(playerSession);
				} else {
					playerSession.setPremium(command == CommandType.PREMIUM);
					plugin.getDataSource().update(playerSession);
				}

				(sender.getName().equals(playerName) ? Message.SUCCESS_COMMAND : Message.SUCCESS_COMMAND_OTHER).sendMessage(sender, "%player%", playerName, "%status%", command.getStatus().toString());
			}
		} catch (NullPointerException exception) {
			Message.NO_PLAYER.sendMessage(sender, "%player%", playerName);
		} catch (SQLException exception) {
			Message.ERROR_COMMAND.sendMessage(sender);
			exception.printStackTrace();
		}
	}

	private boolean canConfirm(Long time) {
		return time == null ? false : System.currentTimeMillis() - time < cooldown * 1000;
	}
}