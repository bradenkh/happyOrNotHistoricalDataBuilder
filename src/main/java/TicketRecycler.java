import td.api.Exceptions.TDException;
import td.api.JsonPatchArray;
import td.api.Reports.Report;
import td.api.TeamDynamix;

import java.time.LocalDate;
import java.util.*;

public class TicketRecycler {
    HappyOrNot happyOrNot;
    Report cancelledTickets;
    Map<LocalDate, Vector<String>> cancelledTicketsByDate = new HashMap<LocalDate, Vector<String>>();
    Map<String, String> ticketMatches = new HashMap<String, String>();
    TeamDynamix td;

    public TicketRecycler(HappyOrNot happyOrNot, Report cancelledTickets, TeamDynamix td) {
        this.happyOrNot = happyOrNot;
        this.cancelledTickets = cancelledTickets;
        this.td = td;
    }

    public void mapTickets() {
        for (int i = 0; i < happyOrNot.entries.length; i++) {
            HappyOrNot.Entry entry = happyOrNot.entries[i];
            entry.setDate();
            String ticketId = getTicketIdFromDate(entry.date);
            ticketMatches.put(entry.ts, ticketId);
        }
    }

    private String getTicketIdFromDate(LocalDate date) {
        boolean goDown = false;
        while (!cancelledTicketsByDate.containsKey(date) || cancelledTicketsByDate.get(date).isEmpty()) {
            if (goDown) {
                date = date.minusDays(1);
            } else {
                date = date.plusDays(1);
            }
            if (date.isAfter(LocalDate.of(2022, 5, 22))) {
                goDown = true;
            }
        }
        String ticketId = cancelledTicketsByDate.get(date).get(0);
        cancelledTicketsByDate.get(date).remove(ticketId);
        return ticketId;
    }

    public void mapCancelledTicketsToDate() {
        for (Map<String, String> ticket : cancelledTickets.getDataRows()) {
            LocalDate createdDate = LocalDate.parse(ticket.get("CreatedDate").split("T")[0]);
            // add the ticket to the list if it's there
            if (cancelledTicketsByDate.containsKey(createdDate)) {
                cancelledTicketsByDate.get(createdDate).add(ticket.get("TicketID"));
            }
            // otherwise add key and new set
            else {
                Vector<String> temp = new Vector<>();
                temp.add(ticket.get("TicketID"));
                cancelledTicketsByDate.put(createdDate, temp);

            }
        }
    }

    public void patchTickets() {
        for (int i = 0; i < happyOrNot.entries.length; i++) {
            HappyOrNot.Entry entry = happyOrNot.entries[i];
            String ticketId = ticketMatches.get(entry.ts);
            if (entry.isFeedback()) {
                performPatch(ticketId, entry.getRating(), entry.text);
            }
            else {
                performPatch(ticketId, entry.getRating(), " ");
            }
        }
    }

    private void performPatch(String ticketId, int rating, String text) {
        // convert text to utf 8
        byte[] utf8Bytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        String utf8String = new String(utf8Bytes, java.nio.charset.StandardCharsets.UTF_8);
        System.out.println(text);
        JsonPatchArray patch = new JsonPatchArray();
        patch.addPatchOperation("add", "attributes/13542", rating);
        patch.addPatchOperation("replace", "Description", utf8String);
        patch.addPatchOperation("replace", "StatusID", "1495");
        patch.addPatchOperation("replace", "RequestorUID", "bc721af4-2ee1-e911-910b-005056ac5ec6");
        patch.addPatchOperation("replace", "Title", "HappyOrNot");
        try {
            System.out.println("Patching ticket " + ticketId);
            td.patchTicket(50, false, Integer.parseInt(ticketId), patch);
        } catch (TDException e) {
            throw new RuntimeException(e);
        }
    }

}
