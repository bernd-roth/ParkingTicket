package at.co.netconsulting.parkingticket;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.List;

import at.co.netconsulting.parkingticket.general.StaticFields;
import at.co.netconsulting.parkingticket.pojo.ParkscheinCollection;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private PendingIntent pendingIntent, pendingIntentVoiceMessage;
    private TextToSpeech textToSpeech;
    private Long nextParkingTicket, nextVoiceMessageLong;
    private int durationParkingticket;
    private List<Long> nextParkingTickets, nextVoiceMessage;
    private String licensePlate, telephoneNumber, city;
    private ParkscheinCollection parkscheinCollection;
    private int rowId;
    private boolean isStopSignal = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null && intent.getAction() == null) {
            ParkscheinCollection stop = (ParkscheinCollection) intent.getExtras().getSerializable(StaticFields.PARKSCHEIN_POJO);

            parkscheinCollection = (ParkscheinCollection) intent.getExtras().getSerializable(StaticFields.PARKSCHEIN_POJO);
            city = parkscheinCollection.getCity();
            durationParkingticket = parkscheinCollection.getDurationParkingticket();
            nextParkingTickets = parkscheinCollection.getNextParkingTickets();
            nextVoiceMessage = parkscheinCollection.getNextVoiceMessage();
            licensePlate = parkscheinCollection.getLicensePlate();
            telephoneNumber = parkscheinCollection.getTelephoneNumber();
            isStopSignal = stop.isStop();

            if (stop != null && isStopSignal) {
                sendSMSToCancel(context, city, licensePlate, telephoneNumber);
            } else {
                //TODO checkVoiceMessages needs to be implemented
                //checkVoiceMessages(context, intent);

                if (checkStopSignal()) {
                    sendSMSToCancel(context, city, licensePlate, telephoneNumber);
                    //remove next planned parkingticket from collection
                    parkscheinCollection.getNextParkingTickets().clear();
                } else {
                    if (parkscheinCollection.getNextParkingTickets().size() > 0) {
                        removeNextParkingTicketFromCollection();
                        setNextAlarmManager(context, intent);
                        sendSMS(context, city, durationParkingticket, licensePlate, telephoneNumber);
                        updateIntent(intent);
                    }
                }
            }
        } else if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            Log.d("SMS Received", "SMS was received");
        }
    }

    private void updateIntent(Intent intent) {
        //extra has to be removed from intent and added again
        //collection will not be updated without removing Extra
        intent.removeExtra(StaticFields.PARKSCHEIN_POJO);
        intent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);
    }

    private void removeNextParkingTicketFromCollection() {
        if(nextParkingTickets.size()>=2) {
            //remove upcoming parkingticket
            nextParkingTickets.remove(0);
            //get next planned parkingticket
            nextParkingTicket = nextParkingTickets.get(0);
        } else if(nextParkingTickets.size()>0){
            //get next planned parkingticket
            nextParkingTicket = nextParkingTickets.get(0);
            //remove upcoming parkingticket
            nextParkingTickets.remove(0);
        }
    }

    private void setNextAlarmManager(Context context, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextParkingTicket, pendingIntent);
    }

    private void checkVoiceMessages(Context context, Intent intent) {
        nextVoiceMessageLong = parkscheinCollection.getNextVoiceMessage().get(0);

        if (parkscheinCollection.getNextParkingTickets().size() > 0 && nextVoiceMessageLong > 0) {
            intent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);
            intent.putExtra("voiceMessage", 1);

            //get next planned parkingticket
            nextParkingTicket = nextParkingTickets.get(0);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextParkingTicket, pendingIntent);
        } else if (parkscheinCollection.getNextParkingTickets().size() > 0 && nextVoiceMessageLong == 0) {
            intent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);

            //get next planned parkingticket
            nextParkingTicket = nextParkingTickets.get(0);

            pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextParkingTicket, pendingIntent);
        }
//            if (nextVoiceMessage != null) {
//                //remove next planned voiceMesage
//                parkscheinCollection.getNextVoiceMessage().remove(0);
//                //get next planned vocieMessage
//                nextVoiceMessageLong = nextVoiceMessage.get(0);
//            }
//            if (intent.getExtras() != null) {
//                rowId = intent.getExtras().getInt("voiceMessage", 0);
//                if(rowId==1) {
//                    textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
//                        @Override
//                        public void onInit(int status) {
//                            if (status != TextToSpeech.ERROR) {
//                                // replace this Locale with whatever you want
//                                Locale localeToUse = new Locale("en","US");
//                                textToSpeech.setLanguage(localeToUse);
//                                textToSpeech.speak("Hi, Welcome to my app! 10 minutes have passed now! Its time to check the app now! Time has passed again, over and over again", TextToSpeech.QUEUE_FLUSH, null);
//                                //firstly, send SMS again
//                                //then start recalculating the parkingtickets
//                            }
//                        }
//                    });
//                }
//            }
    }

    private boolean checkStopSignal() {
        if((city.equals("Klagenfurt Zone 1")
                || city.equals("Klagenfurt Zone 2")
                || city.equals("Klosterneuburg Zone 1")
                || city.equals("Klosterneuburg Zone 2")
                || city.equals("Krems Zone 1")
                || city.equals("Krems Zone 2")
                || city.equals("Linz Zone 1")
                || city.equals("Linz Zone 2")
                || city.equals("Linz Zone 3")
                || city.equals("Pörtschach")
                || city.equals("Salzburg Zone 1")
                || city.equals("Schärding Zone 1")
                || city.equals("Steyr Zone 1")
                || city.equals("Steyr Zone 2")
                || city.equals("Steyr Zone 3")
                || city.equals("Steyr Zone 4")
                || city.equals("Steyr Zone 5")
                || city.equals("Velden Zone 1")
                || city.equals("Villach Zone 1")
                || city.equals("Zell am See") && parkscheinCollection.getNextParkingTickets().size()==1)) {
            return true;
        }
        return false;
    }

    private void sendSMSToCancel(Context context, String city, String licensePlate, String telephoneNumber) {
        SmsManager smsManager = context.getSystemService(SmsManager.class);
        smsManager.sendTextMessage(telephoneNumber, null, StaticFields.STOP_SMS, null, null);
    }

    private void sendSMS(Context context, String city, int durationParkingticket, String licensePlate, String telephoneNumber) {
        SmsManager smsManager = context.getSystemService(SmsManager.class);
        smsManager.sendTextMessage(telephoneNumber, null, durationParkingticket + " " + city + "*" + licensePlate, null, null);
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