package eu.horyzon.premiumconnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
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
import eu.horyzon.premiumconnector.manager.RedirectManager;
import eu.horyzon.premiumconnector.redirect.AuthRedirect;
import eu.horyzon.premiumconnector.redirect.MultiLobbyRedirect;
import eu.horyzon.premiumconnector.redirect.ServerRedirect;
import eu.horyzon.premiumconnector.session.PlayerSessionManager;
import eu.horyzon.premiumconnector.sql.Columns;
import eu.horyzon.premiumconnector.sql.DataSource;
import eu.horyzon.premiumconnector.sql.MySQLDataSource;
import eu.horyzon.premiumconnector.sql.SQLManager;
import eu.horyzon.premiumconnector.sql.SQLiteDataSource;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class PremiumConnector extends Plugin {
	private static PremiumConnector instance;

	private Configuration config;
	private MojangResolver resolver;
	private DataSource source;
	private SQLManager SQLManager;
	private boolean secondAttempt,
			blockServerSwitch;

	private Set<String> geyserProxies = new HashSet<>(),
			secondAttempts = new HashSet<>();

	private RedirectManager redirectManager;
	private PlayerSessionManager playerSessionManager;

	@Override
	public void onEnable() {
		instance = this;

		if (!getDataFolder().exists())
			getDataFolder().mkdir();

		try {
			setupConfiguration();
			setupDatabase();
			setupMessages();
			setupCommands();

			// Initialize AuthRedirect
			AuthRedirect authRedirect;
			MultiLobbyRedirect multiLobby = new MultiLobbyRedirect(this);
			if (multiLobby.isEnabled())
				authRedirect = multiLobby;
			else {
				ServerInfo crackedServer = getProxy().getServerInfo(config.getString("authServer"));
				if (crackedServer != null)
					authRedirect = new ServerRedirect(crackedServer);
				else
					throw new Exception("Please provide a correct cracked server name in the configuration file.");
			}

			redirectManager = new RedirectManager(this, authRedirect);
			playerSessionManager = new PlayerSessionManager();

			// Initialize MojangResolver
			resolver = new MojangResolver();

			getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));
			getProxy().getPluginManager().registerListener(this, new PreLoginListener(this));
			if (!config.getBoolean("fixUUID", true))
				getProxy().getPluginManager().registerListener(this, new LoginListener(this));

			getProxy().getPluginManager().registerListener(this, new MessageChannelListener(this));
			getLogger().info("AuthMe hook enabled.");
		} catch (IOException exception) {
			getLogger().warning("Error on loading configuration file...");
		} catch (Exception exception) {
			exception.printStackTrace();
			getLogger().warning("Error on loading plugin...");
		}
	}

	private void setupConfiguration() throws IOException {
		config = loadConfiguration(getDataFolder(), "config.yml");

		getLogger().setLevel(Level.parse(config.getString("debug", "INFO")));
		getLogger().info("Debug level set to " + getLogger().getLevel());

		secondAttempt = config.getBoolean("secondAttempt", true);
		blockServerSwitch = config.getBoolean("blockServerSwitch", true);
		geyserProxies.addAll(config.getStringList("geyserProxy"));
	}

	private void setupDatabase() throws Exception {
		Configuration configBackend = config.getSection("backend");

		Columns.setup(configBackend);
		try {
			source = configBackend.getString("driver").contains("sqlite") ? new SQLiteDataSource(this, configBackend) : new MySQLDataSource(this, configBackend);
		} catch (Exception exception) {
			throw exception;
		}

		SQLManager = new SQLManager(this, source);
	}

	private void setupMessages() throws IOException {
		Configuration configMessage;
		try {
			configMessage = loadConfiguration(getDataFolder(), "locales/message_" + config.getString("locale", "en") + ".yml");
		} catch (NullPointerException exception) {
			getLogger().warning("No default config \"locales/message_" + config.getString("locale", "en") + ".yml\" found.");
			configMessage = loadConfiguration(getDataFolder(), "locales/message_en.yml");
		}

		Message.setup(configMessage);
	}

	private void setupCommands() {
		for (CommandType command : CommandType.values())
			getProxy().getPluginManager().registerCommand(this, new CommandBase(this, command));
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
			}
		}

		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
	}

	public static PremiumConnector getInstance() {
		return instance;
	}

	public Configuration getConfig() {
		return config;
	}

	public DataSource getDataSource() {
		return source;
	}

	public SQLManager getSQLManager() {
		return SQLManager;
	}

	public RedirectManager getRedirectManager() {
		return redirectManager;
	}

	public PlayerSessionManager getPlayerSessionManager() {
		return playerSessionManager;
	}

	public MojangResolver getResolver() {
		return resolver;
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

	public Set<String> getSecondAttempts() {
		return secondAttempts;
	}

	public boolean isFromGeyserProxy(String address) {
		return geyserProxies.stream().anyMatch((proxy) -> address.matches(proxy));
	}

	public boolean hasGeyserSupport() {
		return !geyserProxies.isEmpty();
	}
}