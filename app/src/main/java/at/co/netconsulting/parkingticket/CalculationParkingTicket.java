package at.co.netconsulting.parkingticket;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import at.co.netconsulting.parkingticket.general.StaticFields;
import at.co.netconsulting.parkingticket.pojo.PairResult;

public class CalculationParkingTicket {

    private Long intervall;
    private boolean isEnd = true;
    private List<Long> nextParkingTicket = new ArrayList<>();
    private List<Long> nextVocieMessage = new ArrayList<>();
    private long lminute,
            currentMilliseconds,
            timeSecondsFromSystemMilliseconds,
            timeMinutesFromSystemMilliseconds,
            calced,
            millisecondsAfter24Hours;
    private PairResult pairResult;

    public List<Long> calculateNextParkingTicket(int hour, int minute, int hourEnd, int minuteEnd, long intervall, boolean isStopTimePicker) {
        nextParkingTicket = new ArrayList<>();
        nextVocieMessage = new ArrayList<>();

        //get timePickerForEnd if checkbox is enabled
        if(isStopTimePicker) {
            hourEnd = hourEnd;
            minuteEnd = minuteEnd;
        }

        //minute to milliSeconds
        lminute = minute*60000;

        //current system time in milliseconds since 1970
        currentMilliseconds = System.currentTimeMillis();
        Log.d("CURRENT_MILLIS_SECONDS: ", String.valueOf(currentMilliseconds));

        //get seconds from System.currentTimeInMilliseconds
        timeSecondsFromSystemMilliseconds = getCurrentTimeInMilliseconds(currentMilliseconds);
        Log.d("SECONDS_FROM_SYSTEM_TIME ", String.valueOf(timeSecondsFromSystemMilliseconds));

        //get currentTimeInMilliseconds in format hh:mm:00
        currentMilliseconds=currentMilliseconds-timeSecondsFromSystemMilliseconds;
        Log.d("CURRENT_MILLIS_HH_MM_00 ", String.valueOf(currentMilliseconds));

        //get minutes from System.currentTimeInMilliseconds
        timeMinutesFromSystemMilliseconds = getCurrentMinutesTimeInMilliseconds(currentMilliseconds);
        Log.d("MINUTES_FROM_SYSTEM_TIME ", String.valueOf(timeMinutesFromSystemMilliseconds));

        calced = (minute*60000)-timeMinutesFromSystemMilliseconds;

        //first plannedTime
        currentMilliseconds+=calced;
        Log.d("PLANNED_TIME ", String.valueOf(currentMilliseconds));

        //calculate planned SMS for the next 24h from now on
        millisecondsAfter24Hours=currentMilliseconds + StaticFields.MAX_ONE_DAY;
        Log.d("MILLIS_AFTER_24H ", String.valueOf(millisecondsAfter24Hours));

        //get intervall in Milliseconds
        intervall *= 60000;

        //here the calculation / creating the list starts now
        while(isEnd) {
            nextParkingTicket.add(currentMilliseconds);
            if(intervall==0)
                isEnd=false;
            else {
                currentMilliseconds+=intervall;
            }
            if(currentMilliseconds>=millisecondsAfter24Hours)
                isEnd=false;
        }
        return nextParkingTicket;
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
