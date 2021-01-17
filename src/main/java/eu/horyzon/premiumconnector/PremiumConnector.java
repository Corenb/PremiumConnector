package eu.horyzon.premiumconnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.github.games647.craftapi.resolver.MojangResolver;

import eu.horyzon.premiumconnector.command.CommandBase;
import eu.horyzon.premiumconnector.command.CommandType;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.listeners.LockLoginListener;
import eu.horyzon.premiumconnector.listeners.MessageChannelListener;
import eu.horyzon.premiumconnector.listeners.PreLoginListener;
import eu.horyzon.premiumconnector.listeners.ServerConnectListener;
import eu.horyzon.premiumconnector.session.PlayerSession;
import eu.horyzon.premiumconnector.sql.DataSource;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class PremiumConnector extends Plugin {
	private static PremiumConnector instance;
	private MojangResolver resolver;
	private DataSource source;
	private ServerInfo crackedServer;
	private boolean floodgate,
			secondAttempt,
			blockServerSwitch;
	private int timeCommand;
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
			getLogger().info("Debug level set to " + getLogger().getLevel());

			floodgate = getProxy().getPluginManager().getPlugin("floodgate") != null;
			crackedServer = getProxy().getServerInfo(config.getString("authServer"));
			if (crackedServer == null) {
				getLogger().warning("Please provide a correct cracked server name in the configuration file.");
				return;
			}

			secondAttempt = config.getBoolean("secondAttempt", true);
			blockServerSwitch = config.getBoolean("blockServerSwitch", true);
			timeCommand = config.getInt("timeToConfirm", 30);
			// Initialize MojangResolver
			resolver = new MojangResolver();

			// Setup Database
			Configuration configBackend = config.getSection("backend");
			try {
				source = new DataSource(this, configBackend.getString("driver"), configBackend.getString("host"), configBackend.getInt("port", 3306), configBackend.getString("user"), configBackend.getString("password"), configBackend.getString("database"), configBackend.getString("table"), configBackend.getBoolean("useSSL", true));
			} catch (RuntimeException | SQLException exception) {
				if (configBackend.getString("driver").contains("sqlite"))
					getLogger().warning("Bungeecord don't support SQLite per default.\r\nIf you want to use SQLite, you need to install the driver yourself\r\nEasy way : https://www.spigotmc.org/resources/sqlite-for-bungeecord.57191/\r\nHard way : https://gist.github.com/games647/d2a57abf90f707c0bd1107e432c580f3");
				else
					getLogger().warning("Please configure your database informations.");

				exception.printStackTrace();
				return;
			}

			Message.setup(loadConfiguration(getDataFolder(), "locales/message_" + config.getString("locale", "en") + ".yml"));

			// INITIATE COMMANDS
			for (CommandType command : CommandType.values())
				getProxy().getPluginManager().registerCommand(this, new CommandBase(this, command));

			getProxy().getPluginManager().registerListener(this, new PreLoginListener(this));
			getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));
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
		}
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

	public MojangResolver getResolver() {
		return resolver;
	}

	public ServerInfo getCrackedServer() {
		return crackedServer;
	}

	public boolean isFloodgate() {
		return floodgate;
	}

	public boolean isSecondAttempt() {
		return secondAttempt;
	}

	public boolean isBlockServerSwitch() {
		return blockServerSwitch;
	}

	public int getTimeCommand() {
		return timeCommand;
	}

	public Map<String, ServerInfo> getRedirectionRequests() {
		return pendingRedirections;
	}

	public Map<String, PlayerSession> getPlayerSession() {
		return playerSession;
	}

	public void redirect(String name) {
		getProxy().getPlayer(name).connect(pendingRedirections.remove(name));
	}
}