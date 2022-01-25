package eu.horyzon.premiumconnector.session;

import java.util.HashMap;
import java.util.Map;

public class PlayerSessionManager {
	private final Map<String, PlayerSession> playerSession = new HashMap<>();

	public PlayerSession getSession(String username) {
		return playerSession.get(username);
	}

	public void addSession(PlayerSession session) {
		playerSession.put(session.getName(), session);
	}

	public void removeSession(String username) {
		playerSession.remove(username);
	}
}