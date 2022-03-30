package at.co.netconsulting.parkingticket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import java.util.List;

import at.co.netconsulting.parkingticket.general.StaticFields;
import at.co.netconsulting.parkingticket.pojo.ParkscheinCollection;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private PendingIntent pendingIntent;
    private TextToSpeech textToSpeech;
    private Long nextParkingTicket;
    private int durationParkingticket;
    private List<Long> nextParkingTickets;
    private String licensePlate, telephoneNumber, city;
    private ParkscheinCollection parkscheinCollection;

    @Override
    public void onReceive(Context context, Intent intent) {
        ParkscheinCollection stop = (ParkscheinCollection) intent.getExtras().getSerializable(StaticFields.STOP_SMS);

        if(stop == null) {
            parkscheinCollection = (ParkscheinCollection) intent.getExtras().getSerializable(StaticFields.PARKSCHEIN_POJO);
            city = parkscheinCollection.getCity();
            durationParkingticket = parkscheinCollection.getDurationParkingticket();
            nextParkingTickets = parkscheinCollection.getNextParkingTickets();
            licensePlate = parkscheinCollection.getLicensePlate();
            telephoneNumber = parkscheinCollection.getTelephoneNumber();
        } else if(stop.getCancel().equals(StaticFields.STOP_SMS)){
            parkscheinCollection = (ParkscheinCollection) intent.getExtras().getSerializable(StaticFields.STOP_SMS);
            city = parkscheinCollection.getCity();
            licensePlate = parkscheinCollection.getLicensePlate();
            telephoneNumber = parkscheinCollection.getTelephoneNumber();
        }

        if(stop != null && stop.getCancel().equals(StaticFields.STOP_SMS)) {
            sendSMSToCancel(context, city, licensePlate, telephoneNumber);
        } else if(parkscheinCollection.getNextParkingTickets().size()>0) {
            //remove next planned parkingticket from collection
            parkscheinCollection.getNextParkingTickets().remove(0);
            //get next planned parkingticket
            nextParkingTicket = nextParkingTickets.get(0);

            if(parkscheinCollection.getNextParkingTickets().size()==0) {
                intent.removeExtra(StaticFields.PARKSCHEIN_POJO);
            } else {
                sendSMS(context, city, durationParkingticket, licensePlate, telephoneNumber);

                //extra has to be removed from intent and added again
                //collection will not be updated without removing Extra
                intent.removeExtra(StaticFields.PARKSCHEIN_POJO);
                intent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);

                pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextParkingTicket, pendingIntent);
            }
        }
    }

    private void sendSMSToCancel(Context context, String city, String licensePlate, String telephoneNumber) {
        SmsManager smsManager = context.getSystemService(SmsManager.class);
        smsManager.sendTextMessage(telephoneNumber, null, StaticFields.STOP_SMS, null, null);
    }

    private void sendSMS(Context context, String city, int durationParkingticket, String licensePlate, String telephoneNumber) {
        SmsManager smsManager = context.getSystemService(SmsManager.class);
        smsManager.sendTextMessage(telephoneNumber, null, city, null, null);
    }
}

//TODO:
//        //collect all necessary information before triggering the alarm clock
//        //1. repeat the alarm
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        boolean isRepeating = prefs.getBoolean("REPEATING", false);
//        //2. telephone number
//        String telephoneNumber = prefs.getString("TELEPHONE-NUMBER", "06646606000");
//        //3. city
//        String city = prefs.getString("CITY", "Wien");
//        //4. when to remember that parking ticket is getting outdated
//        int outdatedParkingTicket = prefs.getInt("OUTDATED-PARKING-TICKET", 0);
//        //5. Vibrate
//        boolean isVibrating = prefs.getBoolean("VIBRATE", false);
//        if(isVibrating) {
//            VibratorManager vibrator = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
//            vibrator.getDefaultVibrator();
//
//            final int DELAY = 0, VIBRATE = 500, SLEEP = 0, REPEAT = -1;
//            long[] vibratePattern = {DELAY, VIBRATE, SLEEP};
//
//            vibrator.vibrate(CombinedVibration.createParallel(VibrationEffect.createWaveform(vibratePattern, REPEAT)));
//        }
//        //6. TextToSpeech
//        boolean isTextToSpeech = prefs.getBoolean("TEXT-TO-SPEECH", false);
//        if(isTextToSpeech) {
//            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
//                @Override
//                public void onInit(int status) {
//                    if (status != TextToSpeech.ERROR) {
//                        // replace this Locale with whatever you want
//                        Locale localeToUse = new Locale("en","US");
//                        textToSpeech.setLanguage(localeToUse);
//                        textToSpeech.speak("Hi, Welcome to my app! 10 minutes have passed now! Its time to check the app now! Time has passed again, over and over again", TextToSpeech.QUEUE_FLUSH, null);
//                    }
//                }
//            });
//        }