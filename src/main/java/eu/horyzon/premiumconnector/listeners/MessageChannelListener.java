package eu.horyzon.premiumconnector.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import eu.horyzon.premiumconnector.PremiumConnector;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageChannelListener implements Listener {
	private final PremiumConnector plugin;

	public MessageChannelListener(PremiumConnector plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPluginMessage(final PluginMessageEvent event) {
		if (event.isCancelled())
			return;

		// Check if the message is for a server (ignore client messages)
		if (!event.getTag().equals("BungeeCord"))
			return;

		// Check if a player is not trying to send us a fake message
		if (!(event.getSender() instanceof Server))
			return;

		// Read the plugin message
		final ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());

		// Accept only broadcasts
		if (!in.readUTF().equals("Forward"))
			return;

		// Let's check the subchannel
		if (!in.readUTF().equals("AuthMe.v2.Broadcast"))
			return;

		// Read data byte array
		final short dataLength = in.readShort();
		final byte[] dataBytes = new byte[dataLength];
		in.readFully(dataBytes);
		final ByteArrayDataInput dataIn = ByteStreams.newDataInput(dataBytes);

		// For now that's the only type of message the server is able to receive
		final String type = dataIn.readUTF();
		switch (type) {
		case "login":
			String name = dataIn.readUTF();
			ServerInfo server = plugin.getRedirectionRequests().remove(name);
			plugin.getProxy().getPlayer(name).connect(server);

			plugin.getLogger().fine("Plugin receive login message from Authme for player " + name
					+ " and redirect him on " + server.getName() + " server.");
			break;
		}
	}
}
