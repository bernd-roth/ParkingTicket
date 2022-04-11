package at.co.netconsulting.parkingticket;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import at.co.netconsulting.parkingticket.general.StaticFields;

public class CalculationParkingTicket {

    private boolean isEnd = true;
    private TreeMap<Long, Integer> nextParkingTicket = new TreeMap<Long, Integer>();
    private long currentMilliseconds,
            millisecondsAfter24Hours,
            plannedEndTimeInMilliseconds;
    private Context context;

    public CalculationParkingTicket(Context context) {
        this.context = context;
    }

    public TreeMap<Long, Integer> calculateNextParkingTicket(int hour, int minute, int hourEnd, int minuteEnd, long intervall, boolean isStopTimePicker, int durationParkingticket, String city) {
        nextParkingTicket = new TreeMap<Long, Integer>();

        //get timePickerForEnd if checkbox is enabled
        if (isStopTimePicker) {
            //hourEnd + minuteEnd to milliSeconds
            plannedEndTimeInMilliseconds = plannedTimeToMilliseconds(hourEnd, minuteEnd);
        }

        //current date + time in milliseconds
        currentMilliseconds = System.currentTimeMillis();

        //planned time in milliseconds
        long plannedTimeInMilliseconds = plannedTimeToMilliseconds(hour, minute);

        //which parking schema alternate/no alternate booking to use
        SharedPreferences sh = this.context.getSharedPreferences(StaticFields.ALTERNATE_BOOKING, Context.MODE_PRIVATE);
        String alternateBooking = sh.getString(StaticFields.ALTERNATE_BOOKING, StaticFields.NO_ALTERNATE_BOOKING);

        //change intervall if alternate booking is in use
        //intervall will be overriden
        if (city.equals("Wien")) {
            if (alternateBooking.equals(StaticFields.FIFTEEN_THIRTY) || alternateBooking.equals(StaticFields.THIRTY_FIFTEEN)) {
                intervall = 3600000;
            } else {
                intervall *= 60000;
            }
        } else {
            intervall *= 60000;
        }

        boolean isFirstBooking = true;
        long nextMilliseconds15 = 0;
        long nextMilliseconds30 = 0;

        Calendar date = Calendar.getInstance();
        long timeInSecs = date.getTimeInMillis();
        millisecondsAfter24Hours = timeInSecs + 86400000;

        //here the calculation starts now
        if(city.equals("Wien")) {
            while (isEnd) {
                if (alternateBooking.equals(StaticFields.FIFTEEN_THIRTY) && isFirstBooking) {
                    nextMilliseconds15=plannedTimeInMilliseconds;
                    nextParkingTicket.put(nextMilliseconds15, Integer.valueOf("15"));
                    //book next parking ticket after 15 minutes have passed
                    nextMilliseconds30=plannedTimeInMilliseconds+900000;
                    nextParkingTicket.put(nextMilliseconds30, Integer.valueOf("30"));
                    isFirstBooking = false;
                    plannedTimeInMilliseconds+=900000;
                } else if (alternateBooking.equals(StaticFields.FIFTEEN_THIRTY)) {
                    nextMilliseconds15 += intervall;
                    nextParkingTicket.put(nextMilliseconds15, Integer.valueOf("15"));
                    nextMilliseconds30 += intervall;
                    nextParkingTicket.put(nextMilliseconds30, Integer.valueOf("30"));
                    isFirstBooking = false;
                    plannedTimeInMilliseconds+=intervall;
                } else if(alternateBooking.equals(StaticFields.THIRTY_FIFTEEN) && isFirstBooking) {
                    nextMilliseconds30=plannedTimeInMilliseconds;
                    nextParkingTicket.put(nextMilliseconds30, Integer.valueOf("30"));
                    //book next parking ticket after 15 minutes have passed
                    nextMilliseconds15=plannedTimeInMilliseconds+900000;
                    nextParkingTicket.put(nextMilliseconds15, Integer.valueOf("15"));
                    isFirstBooking = false;
                    plannedTimeInMilliseconds+=900000;
                } else if(alternateBooking.equals(StaticFields.THIRTY_FIFTEEN)) {
                    nextMilliseconds30 += intervall;
                    nextParkingTicket.put(nextMilliseconds30, Integer.valueOf("30"));
                    nextMilliseconds15 += intervall;
                    nextParkingTicket.put(nextMilliseconds15, Integer.valueOf("15"));
                    plannedTimeInMilliseconds+=intervall;
                } else if(alternateBooking.equals(StaticFields.NO_ALTERNATE_BOOKING) && intervall>0) {
                    nextParkingTicket.put(plannedTimeInMilliseconds, durationParkingticket);
                    plannedTimeInMilliseconds+=intervall;
                } else if(alternateBooking.equals(StaticFields.NO_ALTERNATE_BOOKING) && intervall==0){
                    //one shot call, planned time will be set to 99999999 so that it exceeds the
                    //24 hours threshold
                    nextParkingTicket.put(plannedTimeInMilliseconds, durationParkingticket);
                    plannedTimeInMilliseconds+=99999999;
                }
                //stop calculating further parking tickets when the following condition is met
                //1. Stop timer is enabled and planned time is greater than planned end of parking ticket
                //2. or planned next parking ticket, measured in time, is greater than parking ticket in 24 hours
                if (isStopTimePicker && (plannedTimeInMilliseconds > plannedEndTimeInMilliseconds
                ||  plannedTimeInMilliseconds >= millisecondsAfter24Hours)) {
                    isEnd = false;
                } else if(plannedTimeInMilliseconds >= millisecondsAfter24Hours){
                    isEnd = false;
                }
            }
        } else {
            //every parking ticket for any other city than Vienna is considered here
            plannedEndTimeInMilliseconds=StaticFields.MAX_ONE_DAY_MINUTES*60000;

            while (isEnd) {
                nextParkingTicket.put(plannedTimeInMilliseconds, durationParkingticket);
                //one shot calling
                if(intervall==0)
                    isEnd=false;
                //intervall booking
                else {
                    //calculation will end after 24 hours are reached
                    if(plannedTimeInMilliseconds > millisecondsAfter24Hours) {
                        isEnd = false;
                    } else {
                        plannedTimeInMilliseconds+=intervall;
                    }
                }
            }
        }
        return nextParkingTicket;
    }

    private long plannedTimeToMilliseconds(int hour, int minute) {
        Date date = null;
        //Specifying the pattern of input date and time
        long hourInMilliseconds = hour * 3600000;
        long minuteInMilliseconds = minute * 60000;

        String pattern = "dd-M-yyyy hh:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);

        Calendar cal = Calendar.getInstance();
        int dayOfMonthStr = cal.get(Calendar.DAY_OF_MONTH);
        String day = String.valueOf(dayOfMonthStr);

        int monthOfYearStr = cal.get(Calendar.MONTH);
        monthOfYearStr+=1;
        String month = String.valueOf(monthOfYearStr);

        int yearStr = cal.get(Calendar.YEAR);
        String year = String.valueOf(yearStr);

        String dateString = day + "-" + month + "-" + year + " " + hour + ":" + minute + ":00";
        try {
            date = sdf.parse(dateString);
            Log.d("Planned Time: ", String.valueOf(date.getTime()));
        } catch(ParseException e){
            e.printStackTrace();
        }
        return date.getTime();
    }
}
