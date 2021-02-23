package eu.horyzon.premiumconnector.hooks;

public enum HookType {
	MULTI_LOBBY("MultiLobby");

	private final String pluginName;

	HookType(String hookName) {
		this.pluginName = hookName;
	}

	public String getPluginName() {
		return pluginName;
	}
}