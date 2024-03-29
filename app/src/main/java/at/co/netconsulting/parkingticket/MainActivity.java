package at.co.netconsulting.parkingticket;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
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

public class MainActivity extends BaseActivity {
    private PendingIntent pendingIntent;
    private Intent intent;
    private TimePicker startTimePicker, stopTimePicker;
    private int permissionWriteExternalStorage,
            permissionReadExternalStorage,
            permissionAccessWifiState,
            permissionAccessNetworkState,
            permissionInternet,
            permissionSendSMS,
            permissionBroadcastSMS,
            permissionReadSMS,
            permissionReceiveSMS,
            permissionAccessFineLocation,
            permissionAccessCoarseLocation,
            permissionAccessLocationExtraCommands,
            hourEnd,
            minuteEnd;
    private Spinner spinnerMinutes;
    private Spinner spinnerCity;
    private ParkscheinCollection parkscheinCollection;
    private String city;
    private String licensePlate;
    private String telephoneNumber;
    private long waitMinutesLong;
    private Integer durationParkingticket;
    private NumberPicker numberPicker;
    private Button stop;
    private CheckBox enableStopTimerCheckBox;
    private boolean isStopTimePicker, isVoiceMessageActivated, resultValue;
    private Toolbar toolbar;
    private TreeMap<Long, Integer> nextParkingTickets;
    private static MainActivity instance;
    private String showAlertDialog, alternateBooking, nextParkingTicket;
    private TextView textViewAlarmManagerOverview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        instance = this;

        if (checkAndRequestPermissions()) {
            initializeObjects();
            loadSharedPreferences(StaticFields.TELEPHONE_NUMBER);
            loadSharedPreferences(StaticFields.LICENSE_PLATE);
            loadSharedPreferences(StaticFields.WAIT_MINUTES);
            loadSharedPreferences(StaticFields.ALERT_DIALOG);
            loadSharedPreferences(StaticFields.ALTERNATE_BOOKING);
        } else {
            //Show error message and close app
        }
    }

    private boolean checkAndRequestPermissions() {
        permissionWriteExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionReadExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionAccessWifiState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE);
        permissionAccessNetworkState = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE);
        permissionInternet = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);
        permissionSendSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        permissionBroadcastSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.BROADCAST_SMS);
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
        if (permissionBroadcastSMS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.BROADCAST_SMS);
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
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), StaticFields.REQUEST_ID_MULTIPLE_PERMISSIONS);
            return true;
        }
        return false;
    }

    private void initializeObjects() {
        intent = new Intent(getApplicationContext(), SmsBroadcastReceiver.class);

        enableStopTimerCheckBox = findViewById(R.id.stopTimerCheckbox);
        enableStopTimerCheckBox.setEnabled(true);
        enableStopTimerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    saveSharedPreferences(isStopTimePicker = true, StaticFields.STOP_TIMER_CHECKBOX);
                }
            }
        });

        stop = findViewById(R.id.stopButton);
        stop.setVisibility(View.VISIBLE);

        startTimePicker = findViewById(R.id.startTimePicker);
        startTimePicker.setIs24HourView(true);

        stopTimePicker = findViewById(R.id.stopTimePicker);
        stopTimePicker.setIs24HourView(true);

        numberPicker = findViewById(R.id.numberpicker_main_picker);
        numberPicker.setMinValue(StaticFields.MIN_ONE_DAY_MINUTES);
        numberPicker.setMaxValue(StaticFields.MAX_ONE_DAY_MINUTES);
        numberPicker.setEnabled(true);
        numberPicker.setWrapSelectorWheel(true);

        spinnerMinutes = (Spinner) findViewById(R.id.minutes_spinner);
        spinnerMinutes.setEnabled(true);

        spinnerCity = findViewById(R.id.city_spinner);
        ArrayAdapter<CharSequence> adapterCity = ArrayAdapter.createFromResource(this, R.array.city, android.R.layout.simple_spinner_item);
        adapterCity.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapterCity);
        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSpinnerCity(parent, position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<CharSequence> adapterLicensePlate = ArrayAdapter.createFromResource(this, R.array.license_plate, android.R.layout.simple_spinner_item);
        adapterLicensePlate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> adapterTelephoneNumber = ArrayAdapter.createFromResource(this, R.array.telephoneNumber, android.R.layout.simple_spinner_item);
        adapterTelephoneNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        textViewAlarmManagerOverview = findViewById(R.id.textViewAlarmManagerOverview);
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
        //show AlertDialog about bookings
        if(showAlertDialog.equals(StaticFields.DIALOG_YES))
            showAlertDialog();
        if (isVoiceMessageActivated) {
            if (size > 0) {
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedTime, pendingIntent);
                calcAndSaveNextParkingTicket();
            } else {
                AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setAlarmClock(ac, pendingIntent);
            }
        } else {
            if (size > 0) {
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedTime, pendingIntent);
                calcAndSaveNextParkingTicket();
            } else {
                AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
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

        // setup the alert builder
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

        // add a button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resultValue = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });

        // create and show the alert dialog
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

    //When STOP is called manually
    @SuppressLint("ScheduleExactAlarm")
    public void triggerCancellationAlarmManager() {
        if(isCityStop()) {
            Spinner spinnerCity = (Spinner) findViewById(R.id.city_spinner);
            String city = spinnerCity.getSelectedItem().toString();
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

    private TreeMap<Long, Integer> calculateNextParkingTicket() {
        //get timePicker
        int hour = startTimePicker.getHour();
        int minute = startTimePicker.getMinute();

        //get timePickerForEnd if checkbox is enabled
        if(isStopTimePicker) {
            hourEnd = stopTimePicker.getHour();
            minuteEnd = stopTimePicker.getMinute();
        }

        long interval = Long.valueOf(numberPicker.getValue());

        CalculationParkingTicket calc = new CalculationParkingTicket(getApplicationContext());
        int durationMinutes = Integer.valueOf(spinnerMinutes.getSelectedItem().toString());
        TreeMap<Long, Integer> nextParkingTicket = calc.calculateNextParkingTicket(hour, minute, hourEnd, minuteEnd, interval, isStopTimePicker, durationMinutes, city);

        return nextParkingTicket;
    }

    private void selectedSpinnerCity(AdapterView<?> parent, int position) {
        String item = parent.getItemAtPosition(position).toString();
        city = item;
        if(!item.equals("Wien")) {
            deactivateEndTimePicker(false);
        }

        ArrayAdapter<CharSequence> adapterMinutes;

        switch (city) {
            case "Baden Z1 (Blaue Zone)":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.baden_z1_minutes, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                selectedSpinnerMinutes(R.array.baden_z1_minutes);
                break;
            case "Baden Z2 (Grüne Zone)":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.baden_z2_minutes, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                selectedSpinnerMinutes(R.array.baden_z2_minutes);
                break;
            case "Bruck an der Leitha":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.bruck_an_der_leitha_minutes, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Eisenstadt Zone A":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.eisenstadt_zona_a_minutes, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Eisenstadt Zone B":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.eisenstadt_zona_b_minutes, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Eisenstadt Zone C":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.eisenstadt_zona_c_minutes, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Gleisdorf":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.gleisdorf, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Gmunden":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.gmunden, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Graz Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Graz Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Graz Zone 3":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z3, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Graz Zone 5":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z5, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Graz Zone 15":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z15, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Hall in Tirol":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.hall_in_tirol, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "IIG Parkplatz Sillside":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.iig_parkplatz_sillside, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Innsbruck Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Innsbruck Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Innsbruck Zone 3":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_3, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Innsbruck Zone 4":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_4, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Innsbruck Zone 5":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_5, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Klagenfurt Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klagenfurt_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Klagenfurt Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klagenfurt_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Klosterneuburg Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klosterneuburg_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Klosterneuburg Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klosterneuburg_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Korneuburg":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.korneuburg, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Krems Zone 1 (Blaue Zone)":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.krems_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Krems Zone 2 (Grüne Zone)":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.krems_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Linz Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.linz_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Linz Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.linz_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Linz Zone 3":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.linz_zone_3, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Mödling":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.moedling, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Neusiedl am See Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.neusiedl_am_see_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Neusiedl am See Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.neusiedl_am_see_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Oberwart":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.oberwart, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Parktiger Flughafen Wien":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.parktiger_flughafen_wien, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Parktiger P + R Aspern":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.parktiger_p_r_aspern, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Parktiger P + R Heiligenstadt":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.parktiger_p_r_heiligenstadt, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Perchtoldsdorf Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.perchtoldsdorf_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Perchtoldsdorf Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.perchtoldsdorf_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Pörtschach":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.poertschach, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Ried Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klagenfurt_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Salzburg Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.salzburg_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Schwechat Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.schwechat_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Schärding Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.schaerding_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Spittal":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.spittal, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "St. Pölten Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.sankt_poelten_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "St. Pölten Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.sankt_poelten_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Steyr Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Steyr Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Steyr Zone 3":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_3, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Steyr Zone 4":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_4, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Steyr Zone 5":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_5, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Stockerau":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.stockerau, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Tulln":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.tulln, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Velden Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.velden_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Velden Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.velden_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Villach":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.villach, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Weiz Zone A":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.weiz_zone_a, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Weiz Zone B":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.weiz_zone_b, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Wels":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wels, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Wien":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wien, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                selectedSpinnerMinutes(R.array.wien);
                deactivateEndTimePicker(true);
                break;
            case "Wiener Neustadt Zone 1":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wiener_neustadt_zone_1, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Wiener Neustadt Zone 2":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wiener_neustadt_zone_2, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Wipark":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wipark, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
            case "Zell am See":
                spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
                adapterMinutes = ArrayAdapter.createFromResource(this, R.array.zell_am_see, android.R.layout.simple_spinner_item);
                adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCity.setAdapter(adapterMinutes);
                break;
        }
    }

    private void deactivateEndTimePicker(boolean deactivate) {
        enableStopTimerCheckBox.setEnabled(deactivate);
        stopTimePicker.setEnabled(deactivate);
    }

    private void selectedSpinnerMinutes(int baden_z1_minutes) {
        spinnerMinutes = (Spinner) findViewById(R.id.minutes_spinner);
        ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, baden_z1_minutes, android.R.layout.simple_spinner_item);
        adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinutes.setAdapter(adapterMinutes);
        spinnerMinutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                durationParkingticket = Integer.valueOf(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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

    public void updateTheTextView(final Map.Entry<Long, Integer> firstEntry) {
        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                long firstKey = firstEntry.getKey();

                CalculationParkingTicket calc = new CalculationParkingTicket(getApplicationContext());
                String hoursAndMinutes = calc.calculateMillisecondsToHoursMinutes(firstKey);

                textViewAlarmManagerOverview.setText(getString(R.string.next_parking_ticket) + hoursAndMinutes);
            }
        });
    }

    //--------------------onclicked methods--------------------//
    public void stopAlarm(View view) {
        triggerCancellationAlarmManager();
        destroySharedPreference();
        cancelForegroundService();
    }

    private void cancelForegroundService() {
        //Intent intent = new Intent(this, SmsBroadcastReceiver.class);
        Intent intentForegroundService = new Intent(this, ForegroundService.class);
        //intent.putExtra("stopForegroundService", "stopForegroundService");
        //intent.setAction("stopForegroundService");
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(
        //        this.getApplicationContext(), StaticFields.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT |
        //                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);
        //AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        getApplicationContext().stopService(intentForegroundService);
    }

    private void destroySharedPreference() {
        getApplicationContext().getSharedPreferences(StaticFields.NEXT_PARKINGTICKET, 0).edit().clear().commit();
    }

    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }

    public void startAlarm(View view) {
        TreeMap<Long, Integer> nextParkingTickets = calculateNextParkingTicket();

        if(!isCityStop())
            parkscheinCollection = new ParkscheinCollection(city, nextParkingTickets, licensePlate, telephoneNumber, false);
        else
            parkscheinCollection = new ParkscheinCollection(city, nextParkingTickets, licensePlate, telephoneNumber, true);

        prepareAlarmManager(parkscheinCollection);
    }

    public static MainActivity getInstance() {
        return instance;
    }

    public void cancelAlarmManagerFromForegroundService() {
        triggerCancellationAlarmManager();
    }

    private void saveSharedPreferences(boolean input, String sharedPref) {
        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPref,MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // Storing the key and its value as the data fetched from edittext
        myEdit.putBoolean(sharedPref, input);

        // Once the changes have been made,
        // we need to commit to apply those changes made,
        // otherwise, it will throw an error
        myEdit.commit();
    }

    private void saveSharedPreferencesAsString(String value, String sharedPref) {
        // Storing data into SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(sharedPref,MODE_PRIVATE);

        // Creating an Editor object to edit(write to the file)
        SharedPreferences.Editor myEdit = sharedPreferences.edit();

        // Storing the key and its value as the data fetched from edittext
        myEdit.putString(sharedPref, value);

        // Once the changes have been made,
        // we need to commit to apply those changes made,
        // otherwise, it will throw an error
        myEdit.commit();
    }

    //--------------------Activity overriden methods--------------------//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        destroySharedPreference();
    }

    @Override
    protected void onResume() {
        loadSharedPreferences("NEXT_PARKINGTICKET");
        loadSharedPreferences(StaticFields.TELEPHONE_NUMBER);
        loadSharedPreferences(StaticFields.LICENSE_PLATE);
        loadSharedPreferences(StaticFields.WAIT_MINUTES);
        loadSharedPreferences(StaticFields.ALERT_DIALOG);
        loadSharedPreferences(StaticFields.ALTERNATE_BOOKING);

        if(nextParkingTicket.startsWith(getString(R.string.your))) {
            textViewAlarmManagerOverview.setText(getString(R.string.booked_parking_ticket));
        } else {
            textViewAlarmManagerOverview.setText(getString(R.string.next_parking_ticket) + nextParkingTicket);
        }
        super.onResume();
    }
}