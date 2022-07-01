import td.api.Exceptions.TDException;
import td.api.HttpCommunication.ResourceType;
import td.api.Logging.History;
import td.api.Reports.Report;
import td.api.TeamDynamix;

public class Main {
    public static void main(String[] args) {
        try {
            History history = new History(ResourceType.APPLICATION, "Ticket deleter");
            TeamDynamix td = new TeamDynamix(System.getenv("TD_API_BASE_URL"), System.getenv("USERNAME"), System.getenv("PASSWORD"), history);

            Report cancelledTickets = td.getReport(21177, true, "");

            HappyOrNot happyOrNot = new HappyOrNot();
            happyOrNot.readDataFromFile();

            TicketRecycler ticketRecycler = new TicketRecycler(happyOrNot, cancelledTickets, td);
            ticketRecycler.mapCancelledTicketsToDate();
            ticketRecycler.mapTickets();
            ticketRecycler.patchTickets();
        } catch (TDException e) {
            e.printStackTrace();
        }
    }
}
