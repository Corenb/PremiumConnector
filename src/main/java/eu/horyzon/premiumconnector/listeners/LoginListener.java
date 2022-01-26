package eu.horyzon.premiumconnector.listeners;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.UUID;

import com.github.games647.craftapi.UUIDAdapter;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

public class LoginListener implements Listener {
	private final PremiumConnector plugin;

	private static final String UUID_FIELD_NAME = "uniqueId";
	private static final MethodHandle uniqueIdSetter;

	static {
		MethodHandle setHandle = null;
		try {
			Lookup lookup = MethodHandles.lookup();

			Field uuidField = InitialHandler.class.getDeclaredField(UUID_FIELD_NAME);
			uuidField.setAccessible(true);
			setHandle = lookup.unreflectSetter(uuidField);
		} catch (ReflectiveOperationException reflectiveOperationException) {
			reflectiveOperationException.printStackTrace();
		}

		uniqueIdSetter = setHandle;
	}

	public LoginListener(PremiumConnector plugin) {
		this.plugin = plugin;
	}

	@EventHandler()
	public void onLogin(LoginEvent event) {
		if (event.isCancelled() || !event.getConnection().isOnlineMode())
			return;

		PendingConnection connection = event.getConnection();
		String username = connection.getName();
		try {
			UUID onlineUUID = connection.getUniqueId();
			UUID offlineUUID = UUIDAdapter.generateOfflineId(username);

			uniqueIdSetter.invokeExact((InitialHandler) connection, offlineUUID);

			plugin.getLogger().fine("New offline UUID" + offlineUUID + " set for player " + username + " instead of premium UUID " + onlineUUID);
		} catch (Exception exception) {
			exception.printStackTrace();
			plugin.getLogger().warning("Failed to set offline uuid of " + username);
		} catch (Throwable exception) {
			exception.printStackTrace();
		}
	}
}