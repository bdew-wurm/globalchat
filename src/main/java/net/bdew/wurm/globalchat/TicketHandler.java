package net.bdew.wurm.globalchat;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.server.support.Tickets;

public class TicketHandler {
    public static void updateTicket(Ticket newTicket) {
        if (!Servers.isThisLoginServer()) return;
        if (Tickets.getTicket(newTicket.getTicketId()) == null) {
            DiscordHandler.sendToDiscord(CustomChannel.TICKETS, String.format("**NEW TICKET %s**", newTicket.getTrelloName()));
            DiscordHandler.sendToDiscord(CustomChannel.TICKETS, newTicket.getDescription());
        }
    }

    public static void addTicketAction(Ticket ticket, TicketAction action) {
        if (!Servers.isThisLoginServer()) return;
        DiscordHandler.sendToDiscord(CustomChannel.TICKETS, String.format("**UPDATED TICKET %s**", ticket.getTrelloName()));
        DiscordHandler.sendToDiscord(CustomChannel.TICKETS, String.format("**%s**%s%s", action.getLine(null), action.getNote() != null && action.getNote().length() > 0 ? ": " : "", action.getNote()));
    }
}
