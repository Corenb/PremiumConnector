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

import eu.horyzon.premiumconnector.command.PremiumCommand;
import eu.horyzon.premiumconnector.config.Message;
import eu.horyzon.premiumconnector.listeners.DisconnectListener;
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
	private boolean floodgate, secondAttempt;

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
			// Initialize MojangResolver
			resolver = new MojangResolver();

			// Setup Database
			Configuration configBackend = config.getSection("backend");
			try {
				source = new DataSource(this, configBackend.getString("driver"), configBackend.getString("host"),
						configBackend.getInt("port", 3306), configBackend.getString("user"),
						configBackend.getString("password"), configBackend.getString("database"),
						configBackend.getString("table"), configBackend.getBoolean("useSSL", true));
			} catch (SQLException exception) {
				exception.printStackTrace();
				getLogger().warning("Please configure your database informations.");
				return;
			}

			Message.setup(loadConfiguration(getDataFolder(), "message.yml"));

			getProxy().getPluginManager().registerCommand(this,
					new PremiumCommand(this, config.getInt("timeToConfirm", 30)));
			getProxy().getPluginManager().registerListener(this, new PreLoginListener(this));
			getProxy().getPluginManager().registerListener(this, new MessageChannelListener(this));
			getProxy().getPluginManager().registerListener(this, new ServerConnectListener(this));
		} catch (IOException exception) {
			exception.printStackTrace();
			getLogger().warning("Error on loading configuration file...");
		}
	}

	private Configuration loadConfiguration(File directory, String fileName) throws IOException {
		File file = new File(directory, fileName);
		if (!file.exists())
			try (InputStream in = getResourceAsStream(fileName)) {
				Files.copy(in, file.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}

		return ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
	}

	public static PremiumConnector getInstance() {
		return instance;
	}

	public DataSource getDataSource() {
		return source;
	}

	public Map<String, PlayerSession> getPlayerSession() {
		return playerSession;
	}

	public MojangResolver getResolver() {
		return resolver;
	}

	public ServerInfo getCrackedServer() {
		return crackedServer;
	}

	public boolean isSecondAttempt() {
		return secondAttempt;
	}

	public Map<String, ServerInfo> getRedirectionRequests() {
		return pendingRedirections;
	}

	public boolean isFloodgate() {
		return floodgate;
	}
}