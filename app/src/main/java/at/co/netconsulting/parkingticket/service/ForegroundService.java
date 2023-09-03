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

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.co.netconsulting.parkingticket.MainActivity;
import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.broadcastreceiver.SmsBroadcastReceiver;
import at.co.netconsulting.parkingticket.general.StaticFields;

public class ForegroundService extends Service {

    private int counter = 0;
    private static final int NOTIFICATION_ID = 1;
    private String NOTIFICATION_CHANNEL_ID = "com.netconsulting.parkingticket", msgBody;
    private Notification notification;
    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager manager;
    private int waitForXMinutes;
    private TextToSpeech textToSpeech;
    private IntentFilter filter;
    private final boolean[] isSmsReceived = new boolean[1];
    private Timer timer;
    private boolean isVoiceMessageParkingTicketExpired;
    private TextToSpeech ttobj;

    @Override
    public void onCreate() {
        super.onCreate();
        filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        filter.addAction("NO_SMS_RECEIVED");

        registerReceiver(receiver, filter);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getSystemService(ns);
        stopForeground(0);
        timer.cancel();
        nMgr.cancel(1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        waitForXMinutes = loadSharedPreferences(StaticFields.WAIT_MINUTES);
        loadSharedPreferences(StaticFields.VOICE_MESSAGE_PARKING_TICKET_EXPIRED);
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.notificationBuilder_title, counter))
                .setContentIntent(pendingIntent)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_launcher_background);

        notification = notificationBuilder.build();

        startForeground(NOTIFICATION_ID, notification);

        final int[] counter = {0};
        waitForXMinutes*=60;

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                manager.notify(NOTIFICATION_ID /* ID of notification */,
                        notificationBuilder.setContentTitle(getString(R.string.title_content, counter[0]++)).build());
                if(isSmsReceived[0] || counter[0]>=waitForXMinutes) {
                    if(!isSmsReceived[0]) {
                        //reset or cancel everything
                        isSmsReceived[0] = false;
                        counter[0]=0;
                        timer.cancel();
                        //send broadcast to notify no sms was received in due time
                        Intent i = new Intent(getApplicationContext(), SmsBroadcastReceiver.class);
                        i.putExtra(StaticFields.NO_PARKSCHEIN_RECEIVED, waitForXMinutes);
                        i.setAction(String.valueOf(R.string.no_sms_received));
                        sendBroadcast(i);
                        stopSelfResult(NOTIFICATION_ID);
                    } else {
                        // sms was received, start showing a new notification
                        // with the appropriate message
                        //stopSelfResult(NOTIFICATION_ID);
                        timer.cancel();
                        showNewNotification(intent);
                    }
                    timer.cancel();
                }
            }
        }, 0,1000);
        return START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    private void showNewNotification(Intent intent) {
        Timer newTimer = new Timer();
        final long[] counter = {0};

        if(intent.getAction() != null) {
            if(intent.getAction().equals(StaticFields.ACTION_START_FOREGROUND_SERVICE)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    int timeValue = bundle.getInt("DurationParkingTicket");
                    counter[0] = timeValue*60;
                    String endTime = analyseParkingTicket();
                    final long[] seconds = {calculateTimeDifference(endTime)};
                    newTimer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if(seconds[0]<=0) {
                                manager.notify(NOTIFICATION_ID /* ID of notification */,
                                        notificationBuilder.setContentTitle(getString(R.string.parkticket_expired)).build());
                                if(isVoiceMessageParkingTicketExpired) {
                                    ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                                        @Override
                                        public void onInit(int status) {
                                            ttobj.speak("Parkingticket expired", TextToSpeech.QUEUE_FLUSH, null);
                                        }
                                    });
                                }
                                newTimer.cancel();
                                manager.cancelAll();
                            } else {
                                manager.notify(NOTIFICATION_ID /* ID of notification */,
                                        notificationBuilder.setContentTitle(getString(R.string.parkticket_expires, seconds[0]--)).build());
                            }
                        }
                    }, 0, 1000);
                }
            }
        }
    }

    private long calculateTimeDifference(String endTime) {
        String[] splitTime = endTime.split(":");
        int hour = Integer.valueOf(splitTime[0]);
        int minute = Integer.valueOf(splitTime[1]);
        LocalTime one = LocalTime.of(hour,minute);

        String sdf = new SimpleDateFormat("HH:mm").format(new Date());
        String[] start = sdf.split(":");
        int startHour = Integer.parseInt(start[0]);
        int startMinute = Integer.parseInt(start[1]);
        LocalTime two = LocalTime.of(startHour,startMinute);

        return ChronoUnit.SECONDS.between(two, one);
    }

    private String analyseParkingTicket() {
        String pattern = "(\\d+):(\\d+)";
        String endtime = null;

        // Create a Pattern object
        Pattern r = Pattern.compile(pattern);

        // Now create matcher object.
        Matcher m = r.matcher(msgBody);

        if (m.find( )) {
            endtime = m.group(0);
        }
        return endtime;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ForegroundService: ", "Calling onDestroy method");
        unregisterReceiver(receiver);
        stopSelf();
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) getSystemService(ns);
        nMgr.cancel(1);
        stopForeground(0);
        if(timer!=null)
            timer.cancel();
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
            case "VOICE_MESSAGE_PARKING_TICKET_EXPIRED":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                isVoiceMessageParkingTicketExpired = sh.getBoolean(sharedPref, false);
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
                        msgBody = msgs[i].getMessageBody();
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