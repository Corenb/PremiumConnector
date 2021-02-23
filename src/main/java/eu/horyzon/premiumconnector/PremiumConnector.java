package eu.horyzon.premiumconnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.github.games647.craftapi.resolver.MojangResolver;

import eu.horyzon.premiumconnector.command.CommandBase;
import eu.horyzon.premiumconnector.command.CommandType;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.listeners.LockLoginListener;
import eu.horyzon.premiumconnector.listeners.LoginListener;
import eu.horyzon.premiumconnector.listeners.MessageChannelListener;
import eu.horyzon.premiumconnector.listeners.PreLoginListener;
import eu.horyzon.premiumconnector.listeners.ServerConnectListener;
import eu.horyzon.premiumconnector.manager.RedirectManager;
import eu.horyzon.premiumconnector.redirect.AuthRedirect;
import eu.horyzon.premiumconnector.redirect.MultiLobbyRedirect;
import eu.horyzon.premiumconnector.redirect.ServerRedirect;
import eu.horyzon.premiumconnector.session.PlayerSessionManager;
import eu.horyzon.premiumconnector.sql.DataSource;
import eu.horyzon.premiumconnector.sql.SQLManager;
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

			if (getProxy().getPluginManager().getPlugin("LockLogin") != null) {
				getProxy().getPluginManager().registerListener(this, new LockLoginListener(this));
				getLogger().info("LockLogin hook enabled.");
			} else {
				getProxy().getPluginManager().registerListener(this, new MessageChannelListener(this));
				getLogger().info("AuthMe hook enabled.");
			}
		} catch (IOException exception) {
			exception.printStackTrace();
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
		Configuration backendSection = config.getSection("backend");
		try {
			source = new DataSource(this, backendSection.getString("driver"), backendSection.getString("host"), backendSection.getInt("port", 3306), backendSection.getString("user"), backendSection.getString("password"), backendSection.getString("database"), backendSection.getString("table"), backendSection.getBoolean("useSSL", true));
		} catch (RuntimeException | SQLException exception) {
			if (backendSection.getString("driver").contains("sqlite"))
				getLogger().warning("You select SQLite as backend but Bungeecord don't support it per default.\r\nIf you want to use SQLite, you need to install the driver yourself\r\nEasy way : https://www.spigotmc.org/resources/sqlite-for-bungeecord.57191/\r\nHard way : https://gist.github.com/games647/d2a57abf90f707c0bd1107e432c580f3");
			else
				getLogger().warning("Please configure your database informations.");

			throw exception;
		}

		SQLManager = new SQLManager(this, source);
	}

	private void setupMessages() throws IOException {
		Configuration messageConfiguration;
		try {
			messageConfiguration = loadConfiguration(getDataFolder(), "locales/message_" + config.getString("locale", "en") + ".yml");
		} catch (NullPointerException exception) {
			messageConfiguration = loadConfiguration(getDataFolder(), "locales/message_en.yml");
		}

		Message.setup(messageConfiguration);
	}

	private void setupCommands() {
		for (CommandType command : CommandType.values())
			getProxy().getPluginManager().registerCommand(this, new CommandBase(this, command));
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