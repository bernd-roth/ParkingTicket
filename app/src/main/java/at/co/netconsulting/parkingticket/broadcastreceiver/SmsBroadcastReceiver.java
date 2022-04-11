package at.co.netconsulting.parkingticket.broadcastreceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;

import java.util.List;
import java.util.TreeMap;

import at.co.netconsulting.parkingticket.general.StaticFields;
import at.co.netconsulting.parkingticket.pojo.ParkscheinCollection;
import at.co.netconsulting.parkingticket.service.ForegroundService;

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private PendingIntent pendingIntent;
    private TextToSpeech textToSpeech;
    private Long nextParkingTicket, nextVoiceMessageLong;
    private int durationParkingticket;
    private TreeMap<Long, Integer> nextParkingTickets;
    private List<Long> nextVoiceMessage;
    private String licensePlate, telephoneNumber, city;
    private ParkscheinCollection parkscheinCollection;
    private boolean isStopSignal = false;
    private SharedPreferences sh;
    private int waitMinutes;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getExtras() != null && intent.getAction().equals("AlarmManager")) {
            //Receive SharedPreferences for voice message
            String sharedPref = "WAIT_MINUTES";
            waitMinutes = loadSharedPreferences(context, sharedPref);
            ParkscheinCollection stop = (ParkscheinCollection) intent.getExtras().getSerializable(StaticFields.PARKSCHEIN_POJO);

            parkscheinCollection = (ParkscheinCollection) intent.getExtras().getSerializable(StaticFields.PARKSCHEIN_POJO);
            city = parkscheinCollection.getCity();
            durationParkingticket = parkscheinCollection.getNextParkingTickets().firstEntry().getValue();
            nextParkingTickets = parkscheinCollection.getNextParkingTickets();
            licensePlate = parkscheinCollection.getLicensePlate();
            telephoneNumber = parkscheinCollection.getTelephoneNumber();
            isStopSignal = stop.isStop();

            if (stop != null && isStopSignal) {
                sendSMSToCancel(context, city, licensePlate, telephoneNumber, intent, parkscheinCollection);
            } else {
                //TODO checkVoiceMessages needs to be implemented
                //checkVoiceMessages(context, intent);

                if (checkStopSignal()) {
                    sendSMSToCancel(context, city, licensePlate, telephoneNumber, intent, parkscheinCollection);
                    //remove next planned parkingticket from collection
                    parkscheinCollection.getNextParkingTickets().clear();
                } else {
                    if(parkscheinCollection.getNextParkingTickets().size() == 1) {
                        sendSMS(context, city, durationParkingticket, licensePlate, telephoneNumber);
                        ParkscheinCollection reducedParkscheinCollection = removeNextParkingTicketFromCollection(parkscheinCollection);
                        updateIntent(intent, reducedParkscheinCollection);
                    } else if(parkscheinCollection.getNextParkingTickets().size() > 1) {
                        ParkscheinCollection reducedParkscheinCollection = removeNextParkingTicketFromCollection(parkscheinCollection);
                        updateIntent(intent, reducedParkscheinCollection);
                        setNextAlarmManager(context, intent, reducedParkscheinCollection);
                        sendSMS(context, city, durationParkingticket, licensePlate, telephoneNumber);
                        //start foregroundservice
                        if(waitMinutes>0) {
                            Intent intentForegroundService = new Intent(context, ForegroundService.class);
                            intent.setAction(StaticFields.ACTION_START_FOREGROUND_SERVICE);
                            context.startForegroundService(intentForegroundService);
                        }
                    }
                }
            }
        }
    }

    private int loadSharedPreferences(Context context, String sharedPref) {
        sh = context.getSharedPreferences("WAIT_MINUTES", Context.MODE_PRIVATE);
        waitMinutes = sh.getInt(sharedPref, 0);
        return waitMinutes;
    }

    private void updateIntent(Intent intent, ParkscheinCollection parkscheinCollection) {
        //extra has to be removed from intent and added again
        //collection will not be updated without removing Extra
        intent.removeExtra(StaticFields.PARKSCHEIN_POJO);
        intent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);
        intent.setAction("AlarmManager");
    }

    private ParkscheinCollection removeNextParkingTicketFromCollection(ParkscheinCollection parkscheinCollection) {
        if (parkscheinCollection.getNextParkingTickets().size() > 0) {
            //remove next parkingticket
            parkscheinCollection.getNextParkingTickets().pollFirstEntry();
            return parkscheinCollection;
        } else
            return parkscheinCollection;
    }

    private void setNextAlarmManager(Context context, Intent intent, ParkscheinCollection parkscheinCollection) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, parkscheinCollection.getNextParkingTickets().firstKey(), pendingIntent);
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

    private void sendSMSToCancel(Context context, String city, String licensePlate, String telephoneNumber, Intent intent, ParkscheinCollection parkscheinCollection) {
        SmsManager smsManager = context.getSystemService(SmsManager.class);
        smsManager.sendTextMessage(telephoneNumber, null, StaticFields.STOP_SMS, null, null);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), StaticFields.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        parkscheinCollection.getNextParkingTickets().clear();
    }

    private void sendSMS(Context context, String city, int durationParkingticket, String licensePlate, String telephoneNumber) {
        SmsManager smsManager = context.getSystemService(SmsManager.class);
        smsManager.sendTextMessage(telephoneNumber, null, durationParkingticket + " " + city + "*" + licensePlate, null, null);
    }
}