package eu.horyzon.premiumconnector.redirect;

import com.google.common.base.Preconditions;

import cz.gameteam.dakado.multilobby.MultiLobby;
import cz.gameteam.dakado.multilobby.api.MultiLobbyAPI;
import eu.horyzon.premiumconnector.PremiumConnector;
import eu.horyzon.premiumconnector.hooks.Hook;
import eu.horyzon.premiumconnector.hooks.HookType;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MultiLobbyRedirect extends Hook implements AuthRedirect {
	private final static HookType HOOK = HookType.MULTI_LOBBY;
	private MultiLobbyAPI multiLobby;
	private String group;

	public MultiLobbyRedirect(PremiumConnector plugin) {
		super(HOOK, plugin);
	}

	@Override
	protected void init() throws Exception {
		multiLobby = MultiLobby.getInstance();

		String group = plugin.getConfig().getString("authServer");
		Preconditions.checkArgument(true, "");
		if (group == null || multiLobby.getGroup(group) == null) {
			throw new NullPointerException("Can't find valid MultiLobby '" + group + "' group.");
		}
	}

	@Override
	public void sendToAuth(ProxiedPlayer player) {
		multiLobby.sendPlayerToGroup(player, group);
	}
}