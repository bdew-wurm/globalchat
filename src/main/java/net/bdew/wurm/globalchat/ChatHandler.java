package net.bdew.wurm.globalchat;

import com.wurmonline.server.*;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.webinterface.WcKingdomChat;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatHandler {
    static Logger chatlogger = Logger.getLogger("Chat");

    public static void handleGlobalMessage(Communicator communicator, String message) {
        if (communicator.isInvulnerable()) {
            communicator.sendAlertServerMessage("You may not use global chat until you have moved and lost invulnerability.");
            return;
        }
        if (communicator.player.isMute()) {
            communicator.sendNormalServerMessage("You are muted.");
            return;
        }
        chatlogger.log(Level.INFO, "Global-" + "<" + communicator.player.getName() + "> " + message);

        sendToPlayers(communicator.player.getName(), message, communicator.player.getWurmId(),
                communicator.player.hasColoredChat() ? communicator.player.getCustomRedChat() : -1,
                communicator.player.hasColoredChat() ? communicator.player.getCustomGreenChat() : -1,
                communicator.player.hasColoredChat() ? communicator.player.getCustomBlueChat() : -1
        );
        sendToServers(
                communicator.player.getName(), message, communicator.player.getWurmId(),
                communicator.player.hasColoredChat() ? communicator.player.getCustomRedChat() : -1,
                communicator.player.hasColoredChat() ? communicator.player.getCustomGreenChat() : -1,
                communicator.player.hasColoredChat() ? communicator.player.getCustomBlueChat() : -1
        );

        if (Servers.localServer.LOGINSERVER)
            DiscordHandler.sendToDiscord("<" + communicator.player.getName() + "> " + message);

        communicator.player.chatted();
    }

    public static void sendMessage(final Creature sender, final long senderId, final String playerName, final String message, final boolean emote, final byte kingdom, final int r, final int g, final int b) {
        if (Servers.localServer.LOGINSERVER) DiscordHandler.sendToDiscord("<" + playerName + "> " + message);
        Message mess = new Message(sender, (byte) 16, "Global", "<" + playerName + "> " + message);
        mess.setSenderKingdom(kingdom);
        mess.setSenderId(senderId);
        mess.setColorR(r);
        mess.setColorG(g);
        mess.setColorB(b);
        chatlogger.log(Level.INFO, "Global-" + mess.getMessage());
        final Player[] playarr = Players.getInstance().getPlayers();
        for (Player player : playarr) {
            if (!player.getCommunicator().isInvulnerable() && !player.isIgnored(senderId)) {
                player.getCommunicator().sendMessage(mess);
            }
        }
    }

    public static void sendBanner(final Player player) {
        final Message mess = new Message(player, (byte) 16, "Global", "<System> Welcome to Otherlands Cluster. This is the Global channel, you can use it to communicate with players across all servers and kingdoms.", 250, 150, 250);
        player.getCommunicator().sendMessage(mess);
    }

    static void sendToPlayers(String author, String msg, long wurmId, int r, int g, int b) {
        Message mess = new Message(null, (byte) 16, "Global", "<" + author + "> " + msg);
        mess.setColorR(r);
        mess.setColorG(g);
        mess.setColorB(b);
        final Player[] playarr = Players.getInstance().getPlayers();
        for (Player player : playarr) {
            if (!player.getCommunicator().isInvulnerable() && !player.isIgnored(wurmId)) {
                player.getCommunicator().sendMessage(mess);
            }
        }
    }

    static void sendToServers(String author, String msg, long wurmId, int r, int g, int b) {
        final WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(), wurmId, author, msg, false, (byte) -1, r, g, b);
        if (!Servers.isThisLoginServer()) {
            wc.sendToLoginServer();
        } else {
            wc.sendFromLoginServer();
        }
    }

    public static void serverStarted() {
        if (Servers.localServer.LOGINSERVER) {
            GlobalChatMod.logInfo("Server started, connecting to discord");
            DiscordHandler.initJda();
            DiscordHandler.sendToDiscord("**Servers are starting up...**");
        }
    }

    public static void serverStopped() {
        if (Servers.localServer.LOGINSERVER) {
            GlobalChatMod.logInfo("Sending shutdown notice");
            DiscordHandler.sendToDiscord("**Servers are shutting down. Byeeee~**");
        }
    }

    public static void serverAvailable(ServerEntry ent, boolean available) {
        if (Servers.localServer.LOGINSERVER) {
            GlobalChatMod.logInfo(String.format("Notifying available change - %s %s", ent.getName(), available));
            DiscordHandler.sendToDiscord(String.format("**%s is %s**", ent.getName(), available ? "now online!" : "shutting down."));
        }
    }
}
