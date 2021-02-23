package eu.horyzon.premiumconnector.redirect;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface AuthRedirect {

	public abstract void sendToAuth(ProxiedPlayer player);
}