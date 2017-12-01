package net.bdew.wurm.globalchat;

import com.wurmonline.server.creatures.Communicator;
import javassist.ClassPool;
import javassist.CtClass;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.*;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalChatMod implements WurmServerMod, Configurable, PreInitable, Initable, PlayerMessageListener, ServerStartedListener {
    private static final Logger logger = Logger.getLogger("GlobalChatMod");

    static String botToken;
    static String serverName;
    static String channelName;

    public static void logException(String msg, Throwable e) {
        if (logger != null)
            logger.log(Level.SEVERE, msg, e);
    }

    public static void logWarning(String msg) {
        if (logger != null)
            logger.log(Level.WARNING, msg);
    }

    public static void logInfo(String msg) {
        if (logger != null)
            logger.log(Level.INFO, msg);
    }

    @Override
    public void configure(Properties properties) {
        botToken = properties.getProperty("botToken");
        serverName = properties.getProperty("serverName");
        channelName = properties.getProperty("channelName");
    }

    @Override
    public void preInit() {
        try {
            ClassPool classPool = HookManager.getInstance().getClassPool();

            CtClass ctPlayers = classPool.getCtClass("com.wurmonline.server.Players");
            ctPlayers.getMethod("sendGlobalKingdomMessage", "(Lcom/wurmonline/server/creatures/Creature;JLjava/lang/String;Ljava/lang/String;ZBIII)V")
                    .insertBefore(" if (kingdom == (byte)-1) {net.bdew.wurm.globalchat.ChatHandler.sendMessage($$); return;}");

            ctPlayers.getMethod("sendStartKingdomChat", "(Lcom/wurmonline/server/players/Player;)V").setBody("return;");
            ctPlayers.getMethod("sendStartGlobalKingdomChat", "(Lcom/wurmonline/server/players/Player;)V").setBody("return;");

            CtClass ctLoginHandler = classPool.getCtClass("com.wurmonline.server.LoginHandler");
            ctLoginHandler.getMethod("sendLoggedInPeople", "(Lcom/wurmonline/server/players/Player;)V")
                    .insertBefore("net.bdew.wurm.globalchat.ChatHandler.sendBanner(player);");

            CtClass ctPlayer = classPool.getCtClass("com.wurmonline.server.players.Player");

            ctPlayer.getMethod("isKingdomChat", "()Z").setBody("return false;");
            ctPlayer.getMethod("isGlobalChat", "()Z").setBody("return false;");

            classPool.getCtClass("com.wurmonline.server.Server").getMethod("shutDown", "()V")
                    .insertBefore("net.bdew.wurm.globalchat.ChatHandler.serverStopped();");

            classPool.getCtClass("com.wurmonline.server.ServerEntry").getMethod("setAvailable", "(ZZIIII)V")
                    .insertBefore("if (this.isAvailable != $1) net.bdew.wurm.globalchat.ChatHandler.serverAvailable(this, $1);");

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated
    @Override
    public boolean onPlayerMessage(Communicator communicator, String message) {
        return false;
    }

    @Override
    public MessagePolicy onPlayerMessage(Communicator communicator, String message, String title) {
        if (communicator.player.getPower() >= 4 && message.startsWith("#discordreconnect")) {
            DiscordHandler.initJda();
            return MessagePolicy.DISCARD;
        } else if (title.equals("Global") && !message.startsWith("#") && !message.startsWith("/")) {
            ChatHandler.handleGlobalMessage(communicator, message);
            return MessagePolicy.DISCARD;
        } else {
            return MessagePolicy.PASS;
        }
    }

    @Override
    public void init() {
    }


    @Override
    public void onServerStarted() {
        ChatHandler.serverStarted();
    }
}
