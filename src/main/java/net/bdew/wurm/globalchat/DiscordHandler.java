package net.bdew.wurm.globalchat;

import com.wurmonline.server.Servers;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DiscordHandler extends ListenerAdapter {
    static JDA jda;

    static ConcurrentLinkedQueue<String> sendQueue = new ConcurrentLinkedQueue<>();

    private static Map<String, String> emojis = new HashMap<>();

    static {
        emojis.put("\uD83D\uDE1B", ":P");
        emojis.put("\uD83D\uDE03", ":)");
        emojis.put("\uD83D\uDE04", ":D");
        emojis.put("\uD83D\uDE26", ":(");
        emojis.put("\uD83D\uDE22", ":`(");
        emojis.put("\uD83D\uDE17", ":*");
    }

    static void initJda() {
        if (!Servers.localServer.LOGINSERVER) return;

        if (jda != null) {
            jda.shutdown();
            jda = null;
        }

        try {
            jda = new JDABuilder(AccountType.BOT).setToken(GlobalChatMod.botToken).addEventListener(new DiscordHandler()).buildAsync();
        } catch (LoginException e) {
            GlobalChatMod.logException("Error connecting to discord", e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot() && event.getChannel().getName().equals(GlobalChatMod.channelName)) {
            String name = event.getMember().getNickname();
            if (name == null) name = event.getAuthor().getName();
            for (Message.Attachment att : event.getMessage().getAttachments()) {
                String url = att.getUrl();
                if (url != null) {
                    ChatHandler.sendToPlayersAndServers("@" + name, url, -10L, -1, -1, -1);
                }
            }
            String msg = event.getMessage().getContentDisplay().trim();
            for (Map.Entry<String, String> p : emojis.entrySet()) {
                msg = msg.replace(p.getKey(), p.getValue());
            }
            if (msg.length() > 0) {
                ChatHandler.sendToPlayersAndServers("@" + name, msg, -10L, -1, -1, -1);
            }
        }
    }

    public static void sendToDiscord(String msg) {
        try {
            if (jda == null) return;
            if (jda.getStatus() != JDA.Status.CONNECTED) {
                GlobalChatMod.logInfo("Discord not ready, queueing: " + msg);
                sendQueue.add(msg);
            } else {
                MessageBuilder builder = new MessageBuilder();
                builder.append(msg);
                jda.getGuildsByName(GlobalChatMod.serverName, true).get(0).getTextChannelsByName(GlobalChatMod.channelName, true).get(0).sendMessage(builder.build())
                        .queue(null, e -> GlobalChatMod.logException("Error sending to discord", e));
            }
        } catch (Exception e) {
            GlobalChatMod.logException("Error sending to discord", e);
        }
    }

    @Override
    public void onStatusChange(StatusChangeEvent event) {
        GlobalChatMod.logInfo(String.format("Discord status is now %s", event.getStatus()));
        try {
            if (event.getStatus() == JDA.Status.CONNECTED && !sendQueue.isEmpty()) {
                TextChannel chan = jda.getGuildsByName(GlobalChatMod.serverName, true).get(0).getTextChannelsByName(GlobalChatMod.channelName, true).get(0);
                while (!sendQueue.isEmpty()) {
                    String msg = sendQueue.poll();
                    GlobalChatMod.logInfo("Sending queued: " + msg);
                    MessageBuilder builder = new MessageBuilder();
                    builder.append(msg);
                    chan.sendMessage(builder.build()).queue(null, e -> GlobalChatMod.logException("Error sending to discord", e));
                }
            }
        } catch (Exception e) {
            GlobalChatMod.logException("Error sending to discord", e);
        }
    }
}
