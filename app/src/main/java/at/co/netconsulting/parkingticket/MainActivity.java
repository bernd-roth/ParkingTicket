package at.co.netconsulting.parkingticket;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.compose.ui.platform.ComposeView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import at.co.netconsulting.parkingticket.broadcastreceiver.SmsBroadcastReceiver;
import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.general.StaticFields;
import at.co.netconsulting.parkingticket.pojo.ParkscheinCollection;
import at.co.netconsulting.parkingticket.service.ForegroundService;
import at.co.netconsulting.parkingticket.ui.MainScreenSetup;
import at.co.netconsulting.parkingticket.ui.MainScreenState;

public class MainActivity extends BaseActivity {
    private PendingIntent pendingIntent;
    private Intent intent;
    private int permissionWriteExternalStorage,
            permissionReadExternalStorage,
            permissionAccessWifiState,
            permissionAccessNetworkState,
            permissionInternet,
            permissionSendSMS,
            permissionReadSMS,
            permissionReceiveSMS,
            permissionAccessFineLocation,
            permissionAccessCoarseLocation,
            permissionAccessLocationExtraCommands,
            hourEnd,
            minuteEnd;
    private ParkscheinCollection parkscheinCollection;
    private String city;
    private String licensePlate;
    private String telephoneNumber;
    private long waitMinutesLong;
    private boolean isStopTimePicker, isVoiceMessageActivated, resultValue;
    private TreeMap<Long, Integer> nextParkingTickets;
    private static MainActivity instance;
    private String showAlertDialog, alternateBooking, nextParkingTicket;
    private MainScreenState mainScreenState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        checkAndRequestPermissions();
        loadSharedPreferences(StaticFields.TELEPHONE_NUMBER);
        loadSharedPreferences(StaticFields.LICENSE_PLATE);
        loadSharedPreferences(StaticFields.WAIT_MINUTES);
        loadSharedPreferences(StaticFields.ALERT_DIALOG);
        loadSharedPreferences(StaticFields.ALTERNATE_BOOKING);

        intent = new Intent(getApplicationContext(), SmsBroadcastReceiver.class);

        mainScreenState = new MainScreenState();
        ComposeView composeView = findViewById(R.id.compose_view);
        MainScreenSetup.init(composeView, this, mainScreenState);
    }

    private boolean checkAndRequestPermissions() {
        permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionAccessWifiState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        permissionAccessNetworkState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        permissionInternet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        permissionSendSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        permissionReadSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS);
        permissionReceiveSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS);
        permissionAccessFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        permissionAccessCoarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionAccessLocationExtraCommands = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionWriteExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionReadExternalStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionAccessWifiState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (permissionAccessNetworkState != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if (permissionInternet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (permissionSendSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.SEND_SMS);
        }
        if (permissionReadSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_SMS);
        }
        if (permissionReceiveSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECEIVE_SMS);
        }
        if (permissionAccessFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionAccessCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionAccessLocationExtraCommands != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int permissionPostNotifications = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
            if (permissionPostNotifications != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), StaticFields.REQUEST_ID_MULTIPLE_PERMISSIONS);
            return true;
        }
        return false;
    }

    private void prepareAlarmManager(ParkscheinCollection parkscheinCollection) {
        long plannedTime = parkscheinCollection.getNextParkingTickets().firstKey();
        int size = parkscheinCollection.getNextParkingTickets().size();

        intent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);
        intent.setAction(String.valueOf(R.string.intentAction));

        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), StaticFields.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        triggerAlarmManager(plannedTime, size, isVoiceMessageActivated);
    }

    @SuppressLint("ScheduleExactAlarm")
    private void triggerAlarmManager(long plannedTime, int size, boolean isVoiceMessageActivated) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, R.string.exact_alarm_permission_required, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        if(showAlertDialog.equals(StaticFields.DIALOG_YES))
            showAlertDialog();
        if (isVoiceMessageActivated) {
            if (size > 0) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedTime, pendingIntent);
                calcAndSaveNextParkingTicket();
            } else {
                AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
                alarmManager.setAlarmClock(ac, pendingIntent);
            }
        } else {
            if (size > 0) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedTime, pendingIntent);
                calcAndSaveNextParkingTicket();
            } else {
                AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
                alarmManager.setAlarmClock(ac, pendingIntent);
            }
        }
    }

    private void calcAndSaveNextParkingTicket() {
        TreeMap<Long, Integer> textViewTreeMap = parkscheinCollection.getNextParkingTickets();
        CalculationParkingTicket calc = new CalculationParkingTicket(getApplicationContext());
        String nextParkingTicket = calc.calculateMillisecondsToHoursMinutes(textViewTreeMap.firstKey());
        saveSharedPreferencesAsString(nextParkingTicket, StaticFields.NEXT_PARKINGTICKET);
    }

    public boolean showAlertDialog() {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alertDialog);

        String message = new String();
        Date date;
        SimpleDateFormat formatter = new SimpleDateFormat("dd:MM:yyyy HH:mm", Locale.GERMANY);

        for (Map.Entry<Long, Integer> entry : parkscheinCollection.getNextParkingTickets().entrySet()) {
            date = new Date(entry.getKey());
            String result = formatter.format(date);

            message += "\nDuration: " + entry.getValue() + "-" + "Time: " + result;
            builder.setMessage(message);
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resultValue = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        try {
            Looper.loop();
        } catch (RuntimeException e) {}
        return resultValue;
    }

    private boolean isCityStop() {
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
                || city.equals("Villach")
                || city.equals("Zell am See")))
            return true;
        else
            return false;
    }

    @SuppressLint("ScheduleExactAlarm")
    public void triggerCancellationAlarmManager() {
        if(isCityStop()) {
            parkscheinCollection = new ParkscheinCollection(city, nextParkingTickets, licensePlate, telephoneNumber, true);

            intent.setAction(String.valueOf("AlarmManager"));
            intent.putExtra(StaticFields.STOP_SMS, parkscheinCollection);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), StaticFields.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setAlarmClock(ac, pendingIntent);
        } else {
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            intent.setAction(String.valueOf(R.string.intentAction));
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), StaticFields.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.cancel(pendingIntent);
        }
        parkscheinCollection = null;
    }

    // Called from Compose UI
    public void startAlarmFromCompose(int startHour, int startMinute, int stopHour, int stopMinute,
                                       int interval, String selectedCity, String duration, boolean stopTimerEnabled) {
        this.city = selectedCity;
        this.isStopTimePicker = stopTimerEnabled;
        this.hourEnd = stopHour;
        this.minuteEnd = stopMinute;

        CalculationParkingTicket calc = new CalculationParkingTicket(getApplicationContext());
        int durationMinutes = Integer.valueOf(duration);
        TreeMap<Long, Integer> nextParkingTickets = calc.calculateNextParkingTicket(
                startHour, startMinute, hourEnd, minuteEnd, interval, isStopTimePicker, durationMinutes, city);

        if(!isCityStop())
            parkscheinCollection = new ParkscheinCollection(city, nextParkingTickets, licensePlate, telephoneNumber, false);
        else
            parkscheinCollection = new ParkscheinCollection(city, nextParkingTickets, licensePlate, telephoneNumber, true);

        prepareAlarmManager(parkscheinCollection);
    }

    // Called from Compose UI
    public void stopAlarmFromCompose(String currentCity) {
        this.city = currentCity;
        triggerCancellationAlarmManager();
        destroySharedPreference();
        cancelForegroundService();
    }

    // Called from Compose UI
    public void navigateToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    // Called from Compose UI
    public void navigateToParkingplaces() {
        startActivity(new Intent(this, Parkingplace.class));
    }

    public void updateTheTextView(final Map.Entry<Long, Integer> firstEntry) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                long firstKey = firstEntry.getKey();

                CalculationParkingTicket calc = new CalculationParkingTicket(getApplicationContext());
                String hoursAndMinutes = calc.calculateMillisecondsToHoursMinutes(firstKey);

                mainScreenState.updateNextParkingTicket(getString(R.string.next_parking_ticket) + hoursAndMinutes);
            }
        });
    }

    private void cancelForegroundService() {
        Intent intentForegroundService = new Intent(this, ForegroundService.class);
        getApplicationContext().stopService(intentForegroundService);
    }

    private void destroySharedPreference() {
        getApplicationContext().getSharedPreferences(StaticFields.NEXT_PARKINGTICKET, 0).edit().clear().commit();
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void cancelAlarmManagerFromForegroundService() {
        triggerCancellationAlarmManager();
    }

    private void saveSharedPreferences(boolean input, String sharedPref) {
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPref,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putBoolean(sharedPref, input);
        myEdit.commit();
    }

    private void saveSharedPreferencesAsString(String value, String sharedPref) {
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPref,MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString(sharedPref, value);
        myEdit.commit();
    }

    private void loadSharedPreferences(String sharedPref) {
        SharedPreferences sh;

        switch(sharedPref) {
            case "TELEPHONE_NUMBER":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                telephoneNumber = sh.getString(sharedPref, "06646606000");
                break;
            case "LICENSE_PLATE":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                licensePlate = sh.getString(sharedPref, StaticFields.DEFAULT_NUMBER_PLATE);
                break;
            case "WAIT_MINUTES":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                waitMinutesLong = sh.getInt(sharedPref, 0);
                if(waitMinutesLong>0)
                    isVoiceMessageActivated=true;
                break;
            case "ALERT_DIALOG":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                showAlertDialog = sh.getString(sharedPref, StaticFields.DIALOG_NO);
                break;
            case "ALTERNATE_BOOKING":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                alternateBooking = sh.getString(sharedPref, StaticFields.ALTERNATE_BOOKING);
                break;
            case "NEXT_PARKINGTICKET":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                nextParkingTicket = sh.getString(sharedPref, "Your next booked\\nparking ticket\\nwill be shown here");
                break;
        }
    }

    //--------------------Activity overridden methods--------------------//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
        destroySharedPreference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSharedPreferences("NEXT_PARKINGTICKET");
        loadSharedPreferences(StaticFields.TELEPHONE_NUMBER);
        loadSharedPreferences(StaticFields.LICENSE_PLATE);
        loadSharedPreferences(StaticFields.WAIT_MINUTES);
        loadSharedPreferences(StaticFields.ALERT_DIALOG);
        loadSharedPreferences(StaticFields.ALTERNATE_BOOKING);

        if (mainScreenState != null && nextParkingTicket != null) {
            if (nextParkingTicket.startsWith(getString(R.string.your))) {
                mainScreenState.updateNextParkingTicket(getString(R.string.booked_parking_ticket));
            } else {
                mainScreenState.updateNextParkingTicket(getString(R.string.next_parking_ticket) + nextParkingTicket);
            }
        }
    }
}
