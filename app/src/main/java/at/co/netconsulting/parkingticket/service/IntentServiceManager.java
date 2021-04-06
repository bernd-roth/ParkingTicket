package at.co.netconsulting.parkingticket.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.TreeMap;

import at.co.netconsulting.parkingticket.BroadcastReceiver.SMSBroadcastReceiver;
import at.co.netconsulting.parkingticket.CalculateBookings.CalculateBookings;
import at.co.netconsulting.parkingticket.MainActivity;
import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.statics.StaticVariables;

import static at.co.netconsulting.parkingticket.statics.StaticVariables.JOB_ID;
import static at.co.netconsulting.parkingticket.statics.StaticVariables.KEY;
import static at.co.netconsulting.parkingticket.statics.StaticVariables.VALUE;
import static java.lang.Thread.*;

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
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, StaticVariables.CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.message_notification_booking))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    StaticVariables.CHANNEL_ID,
                    getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        //check whether map is exactly 1 entry, if yes, book it immediately
        //we do not need to check whether the current clock has already passed the booking
        if(CalculateBookings.getTreeMap().size()==1) {
            prepareSendingSMS(CalculateBookings.getTreeMap().firstKey(), CalculateBookings.getTreeMap().get(CalculateBookings.getTreeMap().firstKey()));
        } else if(CalculateBookings.getTreeMap().size()>1){
            usersTreemap = CalculateBookings.getTreeMap();

            while (!usersTreemap.isEmpty()) {
                currentMillisecs = getCurrentCalendarHourMinuteInMilliseconds();
                firstKeyFromDictionary = usersTreemap.firstKey();
                firstDictionaryValue = usersTreemap.get(firstKeyFromDictionary);

                //If currentMillisecs are equal or greater than firstKeyFromDictionary or there is no 2 minutes gap at least, then it is better to do nothing and show user an alert message
                //we do not want to fiddle around with clock time
                if (currentMillisecs >= firstKeyFromDictionary || (firstKeyFromDictionary-currentMillisecs)<=120000) {
                    usersTreemap.clear();
                    final ResultReceiver receiver = intent.getParcelableExtra("receiver");
                    Bundle bundle = new Bundle();
                    bundle.putString("RESULT", String.valueOf(StaticVariables.BOOKING_ERROR));
                    receiver.send(StaticVariables.BOOKING_ERROR, bundle);
                } else {
                    //calculate for how long to sleep until next SMS must be sent
                    long howLongThreadToSleep = getCalculateSleepTime();
                    sleepThread(howLongThreadToSleep);

                    //waking up 30 seconds before sending an SMS
                    //now wait until we reach the point to send an SMS
                    while(getCurrentCalendarHourMinuteSecondsInMilliseconds()<firstKeyFromDictionary)
                        sleepThread(1000);

                    prepareSendingSMS(firstKeyFromDictionary, firstDictionaryValue);

                    //now after sending SMS, we poll the first entry of your dictionary
                    //and start with the next entry
                    usersTreemap.pollFirstEntry();
                }
            }
        }
    }

    private void sleepThread(long howLongThreadToSleep) {
        try {
            sleep(howLongThreadToSleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long getCalculateSleepTime() {
        //get current time
        long currentTime = getCurrentCalendarHourMinuteSecondsInMilliseconds();
        //get next dictionary entry
        firstKeyFromDictionary = usersTreemap.firstKey();
        firstDictionaryValue = usersTreemap.get(firstKeyFromDictionary);
        //calculate how long to sleep
        long whenToWakeUpFromSleep = firstKeyFromDictionary - currentTime-StaticVariables.WHEN_TO_WAKE_UP;
        return whenToWakeUpFromSleep;
    }

    private long getCurrentCalendarHourMinuteSecondsInMilliseconds() {
        final Calendar c = Calendar.getInstance();
        int hour = 0;
        hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = 0;
        minute = c.get(Calendar.MINUTE);
        int second = 0;
        second = c.get(Calendar.SECOND);
        return CalculateBookings.convertTimeToMillisecondsIncludingSeconds(hour, minute, second);
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
        return CalculateBookings.convertTimeToMilliseconds(hour, minute);
    }
}
