package net.bdew.wurm.globalchat;

public enum CustomChannel {
    GLOBAL("Global", (byte) -1, true, false),
    HELP("Help", (byte) -2, true, false),
    INFO("Info", (byte) -3, false, false),
    TICKETS("*", (byte) -3, false, true),
    BROADCAST("*", (byte) -4, false, false);

    public final String ingameName;
    public String discordName;
    public final byte kingdom;
    public final boolean canPlayersSend, discordOnly;

    CustomChannel(String ingameName, byte kingdom, boolean canPlayersSend, boolean discordOnly) {
        this.ingameName = ingameName;
        this.kingdom = kingdom;
        this.canPlayersSend = canPlayersSend;
        this.discordOnly = discordOnly;
    }

    public static CustomChannel findByIngameName(String name) {
        for (CustomChannel ch: values())
            if (name.equals(ch.ingameName))
                return ch;
        return null;
    }

    public static CustomChannel findByDiscordName(String name) {
        for (CustomChannel ch: values())
            if (ch.discordName != null && name.toLowerCase().equals(ch.discordName.toLowerCase()))
                return ch;
        return null;
    }

    public static CustomChannel findByKingdom(byte kingdom) {
        for (CustomChannel ch: values())
            if (kingdom == ch.kingdom)
                return ch;
        return null;
    }
}
