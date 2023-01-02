package at.co.netconsulting.parkingticket.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import at.co.netconsulting.parkingticket.MainActivity;
import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.broadcastreceiver.SmsBroadcastReceiver;
import at.co.netconsulting.parkingticket.general.StaticFields;

public class ForegroundService extends Service {

    private int counter = 0;
    private static final int NOTIFICATION_ID = 1;
    private String NOTIFICATION_CHANNEL_ID = "com.netconsulting.parkingticket";
    private Notification notification;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager manager;
    private int waitForXMinutes;
    private TextToSpeech textToSpeech;
    private IntentFilter filter;
    private final boolean[] isSmsReceived = new boolean[1];

    @Override
    public void onCreate() {
        super.onCreate();
        filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("NO_SMS_RECEIVED");

        registerReceiver(receiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        waitForXMinutes = loadSharedPreferences(StaticFields.WAIT_MINUTES);
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notificationBuilder_title, counter))
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_launcher_background);

        notification = notificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);

        final int[] counter = {0};
        waitForXMinutes*=60;

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                manager.notify(NOTIFICATION_ID /* ID of notification */,
                        notificationBuilder.setContentTitle(getString(R.string.title_content, counter[0]++)).build());
                if(isSmsReceived[0] || counter[0]>waitForXMinutes) {
                    if(!isSmsReceived[0]) {
                        isSmsReceived[0] = false;
                        Intent i = new Intent(getApplicationContext(), SmsBroadcastReceiver.class);
                        i.putExtra(StaticFields.NO_PARKSCHEIN_RECEIVED, waitForXMinutes);
                        i.setAction(String.valueOf(R.string.no_sms_received));
                        sendBroadcast(i);
                        stopSelfResult(NOTIFICATION_ID);
                    } else {
                        stopSelfResult(NOTIFICATION_ID);
                    }
                    timer.cancel();
                }
            }
        }, 0,1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private int loadSharedPreferences(String sharedPref) {
        SharedPreferences sh;

        switch (sharedPref) {
            case "WAIT_MINUTES":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                waitForXMinutes = sh.getInt(sharedPref, 0);
                break;
        }
        return waitForXMinutes;
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String msg_from;
            if (bundle != null) {
                //If SMS was retrieved
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        String msgBody = msgs[i].getMessageBody();
                        if (!msgBody.contains("Zuletzt gebuchter Parkschein")) {
                            isSmsReceived[0]=false;
                        } else {
                            isSmsReceived[0]=true;
                        }
                    }
                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }// else {
                //If no SMS was retrieved
                //voiceMessage();
            //}
        }
    };
}