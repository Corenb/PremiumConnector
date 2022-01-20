package eu.horyzon.premiumconnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.github.games647.craftapi.resolver.MojangResolver;

import eu.horyzon.premiumconnector.command.CommandBase;
import eu.horyzon.premiumconnector.command.CommandType;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.listeners.LoginListener;
import eu.horyzon.premiumconnector.listeners.MessageChannelListener;
import eu.horyzon.premiumconnector.listeners.PreLoginListener;
import eu.horyzon.premiumconnector.listeners.ServerConnectListener;
import eu.horyzon.premiumconnector.session.PlayerSession;
import eu.horyzon.premiumconnector.sql.DataSource;
import eu.horyzon.premiumconnector.sql.MySQLDataSource;
import eu.horyzon.premiumconnector.sql.SQLManager;
import eu.horyzon.premiumconnector.sql.SQLiteDataSource;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class PremiumConnector extends Plugin {
	private static PremiumConnector instance;
	private MojangResolver resolver;
	private DataSource source;
	private SQLManager SQLManager;
	private ServerInfo crackedServer;
	private boolean secondAttempt,
			blockServerSwitch;
	private int timeCommand;

	private Set<String> geyserProxies = new HashSet<>(),
			secondAttempts = new HashSet<>();
	private Map<String, ServerInfo> pendingRedirections = new HashMap<>();
	private Map<String, PlayerSession> playerSession = new HashMap<>();

	@Override
	public void onEnable() {
		instance = this;

		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		try {
			Configuration config = loadConfiguration(getDataFolder(), "config.yml");

			getLogger().setLevel(Level.parse(config.getString("debug", "INFO")));
			getLogger().info("Debug level: " + getLogger().getLevel());

			if ( (crackedServer = getProxy().getServerInfo(config.getString("authServer"))) == null) {
				getLogger().warning("Please provide a correct cracked server name in the configuration file.");
				return;
			}

			secondAttempt = config.getBoolean("secondAttempt", true);
			blockServerSwitch = config.getBoolean("blockServerSwitch", true);
			timeCommand = config.getInt("timeToConfirm", 30);
			geyserProxies.addAll(config.getStringList("geyserProxy"));

			// Setup Database
			Configuration configBackend = config.getSection("backend");
			try {
				source = configBackend.getString("driver").contains("sqlite") ? new SQLiteDataSource(this, configBackend) : new MySQLDataSource(this, configBackend);
			} catch (Exception exception) {
				exception.printStackTrace();
				return;
			}

			// Initialize SQLManager
			SQLManager = new SQLManager(this, source);

			// Initialize MojangResolver
			resolver = new MojangResolver();

			Message.setup(loadConfiguration(getDataFolder(), "locales/message_" + config.getString("locale", "en") + ".yml"));

			// Initialize Commands
			for (CommandType command : CommandType.values())
				getProxy().getPluginManager().registerCommand(this, new CommandBase(this, command));

			getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));
			getProxy().getPluginManager().registerListener(this, new PreLoginListener(this));
			if (!config.getBoolean("fixUUID", true))
				getProxy().getPluginManager().registerListener(this, new LoginListener(this));

			getProxy().getPluginManager().registerListener(this, new MessageChannelListener(this));
			getLogger().info("AuthMe hook enabled.");
		} catch (IOException exception) {
			getLogger().warning("Error on loading configuration file...");
			exception.printStackTrace();
		}
	}

	@Override
	public void onDisable() {
		source.closeConnection();
	}

	private Configuration loadConfiguration(File directory, String fileName) throws IOException {
		File file = new File(directory, fileName);
		if (!file.exists()) {
			if (file.getParentFile() != null)
				file.getParentFile().mkdirs();

			try (InputStream in = getResourceAsStream(fileName)) {
				Files.copy(in, file.toPath());
			} catch (NullPointerException exception) {
				return loadConfiguration(directory, "message_en.yml");
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}

		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
	}

	public static PremiumConnector getInstance() {
		return instance;
	}

	public DataSource getDataSource() {
		return source;
	}

	public SQLManager getSQLManager() {
		return SQLManager;
	}

	public MojangResolver getResolver() {
		return resolver;
	}

	public ServerInfo getCrackedServer() {
		return crackedServer;
	}

	public boolean isFloodgate() {
		return !geyserProxies.isEmpty();
	}

	public boolean allowSecondAttempt() {
		return secondAttempt;
	}

	public boolean isBlockServerSwitch() {
		return blockServerSwitch;
	}

	public int getTimeCommand() {
		return timeCommand;
	}

	public Set<String> getSecondAttempts() {
		return secondAttempts;
	}

	public Map<String, ServerInfo> getRedirectionRequests() {
		return pendingRedirections;
	}

	public Map<String, PlayerSession> getPlayerSession() {
		return playerSession;
	}

	public void redirect(String name) {
		if (pendingRedirections.containsKey(name) && pendingRedirections.get(name) != null)
			((UserConnection) getProxy().getPlayer(name)).connect(pendingRedirections.remove(name), null, true, ServerConnectEvent.Reason.PLUGIN);
	}

	public boolean isFromGeyserProxy(String address) {
		return geyserProxies.stream().anyMatch((proxy) -> address.matches(proxy));
	}

	public boolean hasGeyserSupport() {
		return !geyserProxies.isEmpty();
	}
}