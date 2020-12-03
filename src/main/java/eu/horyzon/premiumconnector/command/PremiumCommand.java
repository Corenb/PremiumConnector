package eu.horyzon.premiumconnector.command;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.session.PlayerSession;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PremiumCommand extends Command {
	private final int timeToConfirm;
	private final Map<UUID, Long> confirm = new HashMap<>();

	public PremiumCommand(PremiumConnector plugin, int timeToConfirm) {
		super("premium", "premiumconnector.command", "prem");
		this.timeToConfirm = timeToConfirm;
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		if (! (sender instanceof ProxiedPlayer)) {
			sender.sendMessage(new TextComponent(Message.PREFIX.toString() + ChatColor.RED + "You need to be a player to execute this command."));
			return;
		}

		ProxiedPlayer player = (ProxiedPlayer) sender;
		if (player.getPendingConnection().isOnlineMode()) {
			Message.ALREADY_PREMIUM.sendMessage(player);
			return;
		}

		if (canConfirm(confirm.remove(player.getUniqueId())))
			try {

				PlayerSession playerSession = new PlayerSession(player.getPendingConnection());
				playerSession.setPremium(true);
				playerSession.update();
				sender.sendMessage(Message.PREMIUM_COMMAND.getTextComponent());
				return;
			} catch (SQLException e) {
				e.printStackTrace();
			}

		sender.sendMessage(Message.WARN_COMMAND.getTextComponent());
		confirm.put(player.getUniqueId(), System.currentTimeMillis());
	}

	private boolean canConfirm(Long time) {
		return time == null ? false : System.currentTimeMillis() - time < timeToConfirm * 1000;
	}
}