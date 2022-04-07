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

    private Long intervall;
    private boolean isEnd = true;
    private TreeMap<Long, Integer> nextParkingTicket = new TreeMap<Long, Integer>();
    private long currentMilliseconds,
            timeSecondsFromSystemMilliseconds,
            timeMinutesFromSystemMilliseconds,
            calced,
            millisecondsAfter24Hours;
    private Context context;

    public CalculationParkingTicket(Context context) {
        this.context = context;
    }

    public TreeMap<Long, Integer> calculateNextParkingTicket(int hour, int minute, int hourEnd, int minuteEnd, long intervall, boolean isStopTimePicker, int durationParkingticket) {
        nextParkingTicket = new TreeMap<Long, Integer>();

        //get timePickerForEnd if checkbox is enabled
        if(isStopTimePicker) {
            hourEnd = hourEnd;
            minuteEnd = minuteEnd;
        }

        //current date + time in milliseconds
        currentMilliseconds = System.currentTimeMillis();

        //planned time in milliseconds
        long plannedTimeInMilliseconds = plannedTimeToMilliseconds(hour, minute);

        //get city for alternate booking
        SharedPreferences sh = this.context.getSharedPreferences(StaticFields.CITY, Context.MODE_PRIVATE);
        String city = sh.getString(StaticFields.CITY, StaticFields.DEFAULT_CITY);

        //which parking schema alternate/no alternate booking to use
        sh = this.context.getSharedPreferences(StaticFields.ALTERNATE_BOOKING, Context.MODE_PRIVATE);
        String alternateBooking = sh.getString(StaticFields.ALTERNATE_BOOKING, StaticFields.NO_ALTERNATE_BOOKING);

        //change intervall if alternate booking is in use
        //intervall will be overriden
        if(alternateBooking.equals(StaticFields.FIFTEEN_THIRTY) || alternateBooking.equals(StaticFields.THIRTY_FIFTEEN)) {
            intervall = 3600000;
        } else {
            //get intervall in Milliseconds
            intervall = 60000;
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
                }
                if (plannedTimeInMilliseconds >= millisecondsAfter24Hours)
                    isEnd = false;
            }
        } else {
            while (isEnd) {
                nextParkingTicket.put(plannedTimeInMilliseconds, durationParkingticket);
                if(intervall==0)
                    isEnd=false;
                else {
                    plannedTimeInMilliseconds+=intervall;
                }
                if(plannedTimeInMilliseconds>=millisecondsAfter24Hours)
                    isEnd=false;
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

    private long getCurrentMinutesTimeInMilliseconds(long currentMilliseconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm");
        Date date = new Date(currentMilliseconds);
        String minutes = simpleDateFormat.format(date);
        long minute = Long.valueOf(minutes);
        return minute * 60000;
    }

    private long getCurrentTimeInMilliseconds(long time) {
        long totalSeconds = time/1000;
        long currentSecond = totalSeconds % 60;
        long totalMinutes =  totalSeconds/60;
        long currentMinutes =totalMinutes%60 -30 ;
        long totalHour= totalMinutes/60;
        long currentHour =  totalHour % 24 - 6  ;
        return currentSecond*1000;
    }
}
