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
	private final Map<UUID, Long> confirm = new HashMap<>();

	public CommandBase(PremiumConnector plugin, CommandType command) {
		super(command.getCommandName(), command.getPermission(), command.getAliases());
		this.plugin = plugin;
		this.command = command;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		try {
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
		} catch (SQLException exception) {
			Message.ERROR_COMMAND.sendMessage(sender);
			exception.printStackTrace();
		}
	}

	protected void update(CommandSender sender, String playerName, CommandType command) throws SQLException {
		try {
			PlayerSession playerSession = plugin.getSQLManager().loadPlayerSessionFromConnection(playerName);
			if (command == CommandType.PREMIUM && !playerSession.isPremium() || command == CommandType.CRACKED && playerSession.isPremium()) {
				playerSession.setPremium(command == CommandType.PREMIUM);
				plugin.getSQLManager().update(playerSession);
			} else if (command == CommandType.RESET)
				plugin.getSQLManager().delete(playerSession);
			else {
				(sender.getName().equals(playerName) ? Message.FAIL_COMMAND : Message.FAIL_COMMAND_OTHER).sendMessage(sender, "%player%", playerName, "%status%", command.getStatus().toString());
				return;
			}

			(sender.getName().equals(playerName) ? Message.SUCESS_COMMAND : Message.SUCESS_COMMAND_OTHER).sendMessage(sender, "%player%", playerName, "%status%", command.getStatus().toString());
		} catch (NullPointerException exception) {
			Message.NO_PLAYER.sendMessage(sender, "%player%", playerName);
		} catch (SQLException exception) {
			Message.ERROR_COMMAND.sendMessage(sender);
			exception.printStackTrace();
		}
	}

	private boolean canConfirm(Long time) {
		return time == null ? false : System.currentTimeMillis() - time < plugin.getTimeCommand() * 1000;
	}
}