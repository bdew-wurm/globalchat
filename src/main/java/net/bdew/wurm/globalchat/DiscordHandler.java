package net.bdew.wurm.globalchat;

import com.wurmonline.server.Servers;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DiscordHandler extends ListenerAdapter {
    static JDA jda;

    static ConcurrentLinkedQueue<String> sendQueue = new ConcurrentLinkedQueue<>();

    static void initJda() {
        if (!Servers.localServer.LOGINSERVER) return;

        if (jda != null) {
            jda.shutdown();
            jda = null;
        }

        try {
            jda = new JDABuilder(AccountType.BOT).setToken(GlobalChatMod.botToken).addEventListener(new DiscordHandler()).buildAsync();
        } catch (LoginException | RateLimitedException e) {
            GlobalChatMod.logException("Error connecting to discord", e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot() && event.getChannel().getName().equals(GlobalChatMod.channelName)) {
            ChatHandler.sendToPlayers("@" + event.getAuthor().getName(), event.getMessage().getContent(), -10L, -1, -1, -1);
            ChatHandler.sendToServers("@" + event.getAuthor().getName(), event.getMessage().getContent(), -10L, -1, -1, -1);
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
