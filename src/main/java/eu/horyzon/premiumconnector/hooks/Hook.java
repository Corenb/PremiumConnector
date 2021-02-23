package eu.horyzon.premiumconnector.hooks;

import eu.horyzon.premiumconnector.PremiumConnector;

public abstract class Hook {
	protected final PremiumConnector plugin;
	protected final HookType type;
	protected boolean enabled = false;

	protected Hook(HookType type, PremiumConnector plugin) {
		this.type = type;
		this.plugin = plugin;

		if (! (enabled = ! (plugin.getProxy().getPluginManager().getPlugin(getName()) == null)))
			return;

		init();

		if (isEnabled())
			plugin.getLogger().info("Hook with " + getName() + " correctly loaded !");
		else
			plugin.getLogger().warning("Hook with " + getName() + " detected but not properly loaded !");
	}

	public String getName() {
		return type.getPluginName();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	protected void init() {}
}