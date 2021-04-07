package at.co.netconsulting.parkingticket.CalculateBookings;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CalculateBookings {
    //TreeMap is automatically sorted
    public static TreeMap<Long, Integer> pendingAlarms = new TreeMap<Long, Integer>();
    public static long millisecondsEndOfDay = 86340000; //23:59
    public int sizeOfTreeMap = 0;

    public static long convertTimeToMillisecondsIncludingSeconds(int hour, int minutes, int seconds){
        return (hour * 60 * 60 * 1000) + (minutes * 60000) + (seconds * 1000);
    }

    public static long convertTimeToMilliseconds(int hour, int minutes){
        return (hour * 60 * 60 * 1000) + (minutes * 60000);
    }
    public long addIntervall(long currentMillisecondsFromTime, long intervall){
        return currentMillisecondsFromTime + intervall;
    }
    public void putTimeToTreeMap(long currentMillisecondsFromTime, int minutesForParkingTicket, long intervall){
        boolean stopCalculating = true;
        long convertIntervall = intervall*60000;

        while(stopCalculating){
            if(isTimeAtTheEnd(currentMillisecondsFromTime, intervall)){
                if(intervall==0){
                    pendingAlarms.put(currentMillisecondsFromTime, minutesForParkingTicket);
                    stopCalculating=false;
                }else{
                    pendingAlarms.put(currentMillisecondsFromTime, minutesForParkingTicket);
                    currentMillisecondsFromTime = addIntervall(currentMillisecondsFromTime, convertIntervall);
                }
            }else
                stopCalculating = false;
        }
    }
    private boolean isTimeAtTheEnd(long currentMillisecondsFromTime, long intervall) {
        long result = currentMillisecondsFromTime + intervall;
        if(result <= millisecondsEndOfDay)
            return true;
        else
            return false;
    }
    public static TreeMap<Long,Integer> getTreeMap(){
        return pendingAlarms;
    }
    public void printTreeMap(){
        // Get a set of the entries
        Set<?> set = pendingAlarms.entrySet();
        // Get an iterator
        Iterator<?> i = set.iterator();
        // Display elements
        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            System.out.print(me.getKey() + ": ");
            System.out.println(me.getValue());
        }
    }
    public int sizeOfTreeMap(){
        return pendingAlarms.size();
    }

    public static long getCurrentCalendarHourMinuteSecondsInMilliseconds() {
        final Calendar c = Calendar.getInstance();
        int hour = 0;
        hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        minute = c.get(Calendar.MINUTE);
        int second = 0;
        second = c.get(Calendar.SECOND);
        return CalculateBookings.convertTimeToMillisecondsIncludingSeconds(hour, minute, second);
    }

    public static long getCurrentCalendarHourMinuteInMilliseconds() {
        final Calendar c = Calendar.getInstance();
        int hour = 0;
        hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        return CalculateBookings.convertTimeToMilliseconds(hour, minute);
    }
}
