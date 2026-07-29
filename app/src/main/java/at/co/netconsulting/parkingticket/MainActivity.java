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
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
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
import at.co.netconsulting.parkingticket.parking.ActiveParkingBookingsStore;
import at.co.netconsulting.parkingticket.parking.ParkingPositionStore;
import at.co.netconsulting.parkingticket.service.ForegroundService;
import at.co.netconsulting.parkingticket.ui.ActiveParkingBooking;
import at.co.netconsulting.parkingticket.service.ParkingLocationService;
import at.co.netconsulting.parkingticket.ui.MainScreenSetup;
import at.co.netconsulting.parkingticket.ui.MainScreenState;
import at.co.netconsulting.parkingticket.ui.ParkingBookingRequest;

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
        mainScreenState.updateCarPositionSaved(ParkingPositionStore.hasCar(this));
        ComposeView composeView = findViewById(R.id.compose_view);
        MainScreenSetup.init(composeView, this, mainScreenState);
        refreshActiveBookingsState();
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

        Intent bookingIntent = new Intent(getApplicationContext(), SmsBroadcastReceiver.class);
        bookingIntent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);
        bookingIntent.setAction(String.valueOf(R.string.intentAction));

        PendingIntent bookingPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), parkscheinCollection.getAlarmRequestCode(),
                bookingIntent, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        triggerAlarmManager(plannedTime, size, bookingPendingIntent, parkscheinCollection);
    }

    @SuppressLint("ScheduleExactAlarm")
    private void triggerAlarmManager(long plannedTime, int size,
                                     PendingIntent bookingPendingIntent,
                                     ParkscheinCollection collection) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, R.string.exact_alarm_permission_required, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            return;
        }

        if (size > 0) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, plannedTime, bookingPendingIntent);
            calcAndSaveNextParkingTicket(collection);
        } else {
            AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(
                    System.currentTimeMillis(), bookingPendingIntent);
            alarmManager.setAlarmClock(ac, bookingPendingIntent);
        }
    }

    private void calcAndSaveNextParkingTicket(ParkscheinCollection collection) {
        TreeMap<Long, Integer> textViewTreeMap = collection.getNextParkingTickets();
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
        if (parkscheinCollection != null) {
            triggerCancellationAlarmManager(parkscheinCollection);
        } else if (city != null && nextParkingTickets != null) {
            ParkscheinCollection collection = new ParkscheinCollection(
                    city, nextParkingTickets, licensePlate, telephoneNumber,
                    isCityStop(), StaticFields.REQUEST_CODE);
            triggerCancellationAlarmManager(collection);
        }
        parkscheinCollection = null;
    }

    private void triggerCancellationAlarmManager(ParkscheinCollection collection) {
        Intent cancellationIntent = new Intent(getApplicationContext(), SmsBroadcastReceiver.class);
        cancellationIntent.setAction(String.valueOf(R.string.intentAction));

        if (collection.isStop()) {
            cancellationIntent.putExtra(StaticFields.STOP_SMS, collection);
            sendBroadcast(cancellationIntent);
            return;
        }

        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        PendingIntent cancellationPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(), collection.getAlarmRequestCode(), cancellationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT |
                        PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(cancellationPendingIntent);
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

    public void startAlarmsFromCompose(List<ParkingBookingRequest> bookings) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, R.string.exact_alarm_permission_required, Toast.LENGTH_LONG).show();
            Intent permissionIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            permissionIntent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(permissionIntent);
            return;
        }

        long earliestBooking = Long.MAX_VALUE;
        int scheduled = 0;
        for (ParkingBookingRequest booking : bookings) {
            this.city = booking.getCity();
            CalculationParkingTicket calc =
                    new CalculationParkingTicket(getApplicationContext());
            TreeMap<Long, Integer> tickets = calc.calculateNextParkingTicket(
                    booking.getStartHour(), booking.getStartMinute(),
                    booking.getStopHour(), booking.getStopMinute(),
                    booking.getIntervalMinutes(), booking.getStopEnabled(),
                    Integer.parseInt(booking.getDuration()), booking.getCity());
            if (tickets.isEmpty()) {
                continue;
            }

            ParkscheinCollection collection = new ParkscheinCollection(
                    booking.getCity(), tickets, booking.getLicensePlate(),
                    telephoneNumber, isCityStop(), nextAlarmRequestCode());
            prepareAlarmManager(collection);
            ActiveParkingBookingsStore.addOrReplace(this, collection);
            earliestBooking = Math.min(earliestBooking, tickets.firstKey());
            scheduled++;
        }

        if (earliestBooking != Long.MAX_VALUE) {
            CalculationParkingTicket calc =
                    new CalculationParkingTicket(getApplicationContext());
            String next = calc.calculateMillisecondsToHoursMinutes(earliestBooking);
            saveSharedPreferencesAsString(next, StaticFields.NEXT_PARKINGTICKET);
            mainScreenState.updateNextParkingTicket(
                    getString(R.string.next_parking_ticket) + next);
        }
        refreshActiveBookingsState();
        Toast.makeText(this, scheduled + " parking booking(s) scheduled",
                Toast.LENGTH_LONG).show();
    }

    private int nextAlarmRequestCode() {
        SharedPreferences preferences =
                getSharedPreferences("ALARM_REQUEST_CODES", Context.MODE_PRIVATE);
        int requestCode = preferences.getInt("NEXT", 1000);
        int next = requestCode == Integer.MAX_VALUE ? 1000 : requestCode + 1;
        preferences.edit().putInt("NEXT", next).apply();
        return requestCode;
    }

    public String getDefaultLicensePlate() {
        return licensePlate == null ? StaticFields.DEFAULT_NUMBER_PLATE : licensePlate;
    }

    public void stopBookingFromCompose(int alarmRequestCode) {
        ParkscheinCollection collection =
                ActiveParkingBookingsStore.findByRequestCode(this, alarmRequestCode);
        if (collection == null) {
            refreshActiveBookingsState();
            Toast.makeText(this, "Parking booking is already stopped or completed",
                    Toast.LENGTH_LONG).show();
            return;
        }

        triggerCancellationAlarmManager(collection);
        ActiveParkingBookingsStore.removeByRequestCode(this, alarmRequestCode);
        refreshActiveBookingsState();

        if (ActiveParkingBookingsStore.load(this).isEmpty()) {
            cancelForegroundService();
        }
        Toast.makeText(this,
                "Parking booking stopped: " + collection.getLicensePlate(),
                Toast.LENGTH_LONG).show();
    }

    // Called from Compose UI
    public void stopAlarmFromCompose(String currentCity) {
        this.city = currentCity;
        triggerCancellationAlarmManager();
        refreshActiveBookingsState();
        if (ActiveParkingBookingsStore.load(this).isEmpty()) {
            cancelForegroundService();
        }
    }

    // Called from Compose UI
    public void navigateToSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    // Called from Compose UI
    public void navigateToParkingplaces() {
        startActivity(new Intent(this, Parkingplace.class));
    }

    @SuppressLint("MissingPermission")
    public void saveCarPosition() {
        boolean fineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!fineLocation && !coarseLocation) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    StaticFields.REQUEST_ID_MULTIPLE_PERMISSIONS);
            Toast.makeText(this, R.string.location_permission_required, Toast.LENGTH_LONG).show();
            return;
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider;
        if (fineLocation && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else {
            Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
            return;
        }

        locationManager.getCurrentLocation(provider, new CancellationSignal(),
                ContextCompat.getMainExecutor(this), location -> {
                    if (location == null) {
                        Toast.makeText(this, R.string.location_unavailable, Toast.LENGTH_LONG).show();
                        return;
                    }
                    ParkingPositionStore.saveCar(this, location);
                    mainScreenState.updateCarPositionSaved(true);
                    ContextCompat.startForegroundService(this, new Intent(this, ParkingLocationService.class));
                    Toast.makeText(this, R.string.car_position_saved, Toast.LENGTH_LONG).show();
                });
    }

    public void navigateToParkedCar() {
        if (!ParkingPositionStore.hasCar(this)) {
            Toast.makeText(this, R.string.no_parked_car, Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(this, ParkedCarActivity.class));
    }

    public void refreshActiveBookingsState() {
        List<ParkscheinCollection> activeCollections =
                ActiveParkingBookingsStore.load(this);
        final List<ActiveParkingBooking> summaries =
                toActiveBookingSummaries(activeCollections);
        long earliestBooking =
                ActiveParkingBookingsStore.earliestNextTicketTime(activeCollections);
        final String nextParkingTicketText;

        if (earliestBooking == Long.MAX_VALUE) {
            destroySharedPreference();
            nextParkingTicketText = getString(R.string.booked_parking_ticket);
        } else {
            CalculationParkingTicket calc =
                    new CalculationParkingTicket(getApplicationContext());
            String next = calc.calculateMillisecondsToHoursMinutes(earliestBooking);
            saveSharedPreferencesAsString(next, StaticFields.NEXT_PARKINGTICKET);
            nextParkingTicketText = getString(R.string.next_parking_ticket) + next;
        }

        if (mainScreenState == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainScreenState.updateActiveBookings(summaries);
                mainScreenState.updateNextParkingTicket(nextParkingTicketText);
            }
        });
    }

    private List<ActiveParkingBooking> toActiveBookingSummaries(
            List<ParkscheinCollection> collections
    ) {
        ArrayList<ActiveParkingBooking> summaries = new ArrayList<>();
        for (ParkscheinCollection collection : collections) {
            if (collection.getNextParkingTickets() != null
                    && !collection.getNextParkingTickets().isEmpty()) {
                summaries.add(new ActiveParkingBooking(
                        collection.getAlarmRequestCode(),
                        collection.getLicensePlate(),
                        collection.getCity(),
                        collection.getNextParkingTickets().firstKey(),
                        collection.getNextParkingTickets().size()
                ));
            }
        }
        summaries.sort((left, right) -> Long.compare(
                left.getNextTicketTimeMillis(), right.getNextTicketTimeMillis()
        ));
        return summaries;
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
        if (mainScreenState != null) {
            mainScreenState.updateCarPositionSaved(ParkingPositionStore.hasCar(this));
        }
        refreshActiveBookingsState();
    }
}
