package net.bdew.wurm.globalchat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LastBroadcastWriter {
    static class LastBroadcastMessage implements Serializable {
        public String time;
        public String from;
        public String message;

        public LastBroadcastMessage(String time, String from, String message) {
            this.time = time;
            this.from = from;
            this.message = message;
        }

        public LastBroadcastMessage() {
        }
    }

    private static List<LastBroadcastMessage> lastBroadcasts;
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private static final TypeReference<List<LastBroadcastMessage>> myTypeRef =
            new TypeReference<List<LastBroadcastMessage>>() {
            };

    private static File file;

    public static void init(String fn) {
        file = new File(fn);
        lastBroadcasts = new ArrayList<>();

        if (file.exists()) {
            try {
                lastBroadcasts = mapper.readValue(file, myTypeRef);
                GlobalChatMod.logInfo(String.format(String.format("Loaded %d last broadcasts from %s", lastBroadcasts.size(), fn)));
            } catch (IOException e) {
                GlobalChatMod.logException("Error loading last broadcasts", e);
            }
        }
    }

    public static void addBroadcast(String from, String message) {
        if (file != null) {
            GlobalChatMod.logInfo(String.format(String.format("Adding broadcast %s: %s", from, message)));
            lastBroadcasts.add(0, new LastBroadcastMessage(ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT), from, message));
            while (lastBroadcasts.size() > 20)
                lastBroadcasts.remove(20);
            try {
                mapper.writeValue(file, lastBroadcasts);
            } catch (IOException e) {
                GlobalChatMod.logException("Error writing last broadcasts", e);
            }
        }
    }
}
