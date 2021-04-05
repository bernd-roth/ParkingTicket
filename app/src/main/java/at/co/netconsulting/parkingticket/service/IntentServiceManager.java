package at.co.netconsulting.parkingticket.service;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import java.util.Calendar;
import java.util.TreeMap;

import at.co.netconsulting.parkingticket.BroadcastReceiver.SMSBroadcastReceiver;
import at.co.netconsulting.parkingticket.CalculateBookings.CalculateBookings;
import at.co.netconsulting.parkingticket.MainActivity;

import static at.co.netconsulting.parkingticket.statics.StaticVariables.JOB_ID;
import static at.co.netconsulting.parkingticket.statics.StaticVariables.KEY;
import static at.co.netconsulting.parkingticket.statics.StaticVariables.VALUE;

public class IntentServiceManager extends JobIntentService {

    TreeMap<Long, Integer> usersTreemap;
    Long firstKeyFromDictionary;
    long currentMillisecs;
    int firstDictionaryValue;

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, IntentServiceManager.class, JOB_ID, intent);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //check whether map is exactly 1 entry, if yes, book it immediately
        if(CalculateBookings.getTreeMap().size()==1) {
            prepareSendingSMS(CalculateBookings.getTreeMap().firstKey(), CalculateBookings.getTreeMap().get(CalculateBookings.getTreeMap().firstKey()));
        } else if(CalculateBookings.getTreeMap().size()>1){
            usersTreemap = CalculateBookings.getTreeMap();
            firstKeyFromDictionary = usersTreemap.firstKey();
            firstDictionaryValue = usersTreemap.get(firstKeyFromDictionary);

            while (!usersTreemap.isEmpty()) {
                currentMillisecs = getCurrentCalendarHourMinuteInMilliseconds();

                //If currentMillisecs are equal firstKeyFromDictionary, then it is better to book it one minute later
                //so that no interference will occur
                //Furthermore, we will clear the Treemap and build a new one from scratch
                if (currentMillisecs >= firstKeyFromDictionary) {
                    buildDictionryFromScratch(currentMillisecs);
                    prepareSendingSMS(firstKeyFromDictionary, firstDictionaryValue);
                }
            }
        }
    }

    private void buildDictionryFromScratch(long currentMillisecs) {
        //1. we have to find out which sequence the user has chosen (15/30 minutes or 30/15 minutes)
        //if 15/30 minutes, than we have to rearrange the minutes
        //take the user´s dictionary and guess what the user`s intention is
        TreeMap<Long, Integer> cloneMap = (TreeMap<Long, Integer>) usersTreemap.clone();
        Long firstClonedTreemapKey = cloneMap.firstKey();
        Integer firstClonedTreemapValue = cloneMap.get(firstClonedTreemapKey);

        if(firstClonedTreemapValue == 15)
            //add 1 minute so that we have no interference
            firstClonedTreemapKey=currentMillisecs+60000;

        //Delete first entry
        cloneMap.pollFirstEntry();
        //Get second entry
        Long secondClonedTreemapKey = cloneMap.firstKey();
        Integer secondClonedTreemapValue = cloneMap.get(secondClonedTreemapKey);
        //If 30 minutes were booked, then we do not need to change anything
        //If 15 minutes were booked again, we have to adapt the time, too
        if(secondClonedTreemapKey==15) {
            long differenceToNextBooking = secondClonedTreemapKey-firstClonedTreemapKey;
            differenceToNextBooking=differenceToNextBooking+60000*2;
            secondClonedTreemapKey=differenceToNextBooking;
        }
    }

    private void prepareSendingSMS(Long firstKeyFromDictionary, int firstValueFromDictionary) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(SMSBroadcastReceiver.ACTION_SMS_BROADCAST_RECEIVER);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(KEY, this.firstKeyFromDictionary.toString());
        broadcastIntent.putExtra(VALUE, firstValueFromDictionary);
        sendBroadcast(broadcastIntent);
    }

    private long getCurrentCalendarHourMinuteInMilliseconds() {
        final Calendar c = Calendar.getInstance();
        int hour = 0;
        hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        minute = c.get(Calendar.MINUTE);
        return CalculateBookings.convertTimeToMilliseconds(hour, minute);
    }
}
