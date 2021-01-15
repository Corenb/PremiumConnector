package eu.horyzon.premiumconnector.command;

import eu.horyzon.premiumconnector.config.Message;

public enum CommandType {
	PREMIUM("premium", "premiumconnector.command.premium", Message.PREMIUM_STATUS, "prem"), CRACKED("cracked", "premiumconnector.command.cracked", Message.CRACKED_STATUS, "crack"), RESET("reset", "premiumconnector.command.reset", Message.NO_STATUS, "rst");

	private final String commandName;
	private final String permission;
	private final Message status;
	private final String[] aliases;

	private CommandType(String commandName, String permission, Message status, String... aliases) {
		this.commandName = commandName;
		this.permission = permission;
		this.status = status;
		this.aliases = aliases;
	}

	public String getCommandName() {
		return commandName;
	}

	public String getPermission() {
		return permission;
	}

	public Message getStatus() {
		return status;
	}

	public String[] getAliases() {
		return aliases;
	}
}