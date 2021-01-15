package eu.horyzon.premiumconnector.config;

import eu.horyzon.premiumconnector.util.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.config.Configuration;

public enum Message {
	PREFIX("prefix", "&8[&6PremiumConnector&8]&r"), MYSQL_ERROR("mySQLError", "&cDatabase error!\r\n&cPlease retry later or contact an administrator"), NOT_PREMIUM_ERROR("notPremiumError", "&2You're not the owner of this account\r\n&2Please change your username to join our server!"), MOJANG_SERVER_ERROR("mojangServerError", "&cAuthentification error\r\n&cMojang's servers are currently down, please retry later!"), RATE_LIMIT("rateLimit", "&cAuthentification overload\r\n&cAuthentificate process is currently overload. Please retry in one minute"), CONSOLE_COMMAND("consoleCommand", "%prefix% &cPlease use /%cmd% [player]"), NO_PERMISSION("noPermission", "%prefix% &cYou don't have the permission to do this."), NO_PLAYER("noPlayer", "%prefix% &cPlayer %player% not found."), WARN_COMMAND("warnCommand", "%prefix% &c&lWARNING &6This command should &lonly &6be invoked if you are the owner of this paid minecraft account. Type &a/%cmd% &6command again to confirm."), SUCESS_COMMAND("sucessCommand", "%prefix% &6You are now &l%status%&6, you need to reconnect you to the server to apply changes."), FAIL_COMMAND("failCommand", "%prefix% &cYou are already a &6&l%status% &cuser."), SUCESS_COMMAND_OTHER("sucessCommandOther", "%prefix% &6Player %player% is now &l%status&6, he need to reconnect to the server to apply changes."), FAIL_COMMAND_OTHER("failCommandOther", "%prefix% &cPlayer %player% is already a &6&l%status% &cuser."), ERROR_COMMAND("errorCommand", "&c%prefix% Error on executing this command. Please try again later or contact an administrator."), CRACKED_STATUS("crackedStatus", "Cracked"), PREMIUM_STATUS("premiumStatus", "Premium"), NO_STATUS("noStatus", "Not defined");

	private final String key;
	private String message;

	Message(String key, String message) {
		this.key = key;
		this.message = message;
	}

	public static void setup(Configuration config) {
		for (Message message : values()) {
			if (!config.contains(message.key))
				config.set(message.key, message.message);

			message.set(Utils.parseColors(config.getString(message.getKey(), message.message)));
		}
	}

	public void set(String message) {
		this.message = message;
	}

	private String getKey() {
		return key;
	}

	public void sendMessage(CommandSender sender, String... placeholder) {
		sender.sendMessage(getTextComponent(placeholder));
	}

	public TextComponent getTextComponent(String... placeholder) {
		return new TextComponent(toString(placeholder));
	}

	@Override
	public String toString() {
		return message;
	}

	public String toString(String... placeholder) {
		int i = 0;
		String message = this.message;
		while (i + 1 <= placeholder.length)
			message = message.replaceAll(placeholder[i++], placeholder[i++]);

		return message.replaceAll("%prefix%", PREFIX.message);
	}
}