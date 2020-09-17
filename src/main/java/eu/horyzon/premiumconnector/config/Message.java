package eu.horyzon.premiumconnector.config;

import eu.horyzon.premiumconnector.util.Utils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public enum Message {
	PREFIX("prefix", "&8[&6PremiumConnector&8] &6"), MYSQL_ERROR("mySQLError", "&cDatabase error!\r\n&cPlease retry later or contact an administrator"), NOT_PREMIUM_ERROR("notPremiumError", "&2You're not the owner of this account\r\n&2Please change your username to join our server!"), MOJANG_SERVER_ERROR("mojangServerError", "&cAuthentification error\r\n&cMojang's servers are currently down, please retry later!"), RATE_LIMIT("rateLimit", "&cAuthentification overload\r\n&cAuthentificate process is currently overload. Please retry in one minute"), WARN_COMMAND("warnCommand", "%prefix% &c&lWARNING &6This command should &lonly &6be invoked if you are the owner of this paid minecraft account. Type &a/premium &6command again to confirm."), PREMIUM_COMMAND("premiumCommand", "%prefix% &6You are now &lPremium &6you need to reconnect you to the server to show correcly you skin."), ALREADY_PREMIUM("alreadyPremium", "%prefix% &cYou are already a &6&lPremium &cuser.");

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

	public void sendMessage(ProxiedPlayer player) {
		player.sendMessage(getTextComponent());
	}

	public TextComponent getTextComponent() {
		return new TextComponent(toString());
	}

	@Override
	public String toString() {
		return message.replaceAll("%prefix%", PREFIX.message);
	}
}