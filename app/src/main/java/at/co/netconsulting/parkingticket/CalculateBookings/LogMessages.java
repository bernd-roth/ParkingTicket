package at.co.netconsulting.parkingticket.CalculateBookings;

import java.util.ArrayList;

public class LogMessages {
    private static final ArrayList<String> logMessages = new ArrayList<String>();

    public void addLogMessages(String message){
        logMessages.add(message);
    }

    public ArrayList getLogMessages() {
        return logMessages;
    }
}
