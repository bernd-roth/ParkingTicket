package at.co.netconsulting.parkingticket;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.general.StaticFields;
import at.co.netconsulting.parkingticket.pojo.ParkscheinCollection;

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
            hourEnd,
            minuteEnd;
    private Spinner spinnerMinutes;
    private Spinner spinnerCity;
    private Spinner spinnerLicensePlate;
    private Spinner spinnerTelephoneNumber;
    private ParkscheinCollection parkscheinCollection;
    private String city;
    private String licensePlate;
    private String telephoneNumber;
    private int waitMinutes;
    private long waitMinutesLong;
    private Integer durationParkingticket;
    private NumberPicker numberPicker;
    private Button stop;
    private CheckBox enableStopTimerCheckBox;
    private boolean isStopTimePicker, isVoiceMessageActivated;
    private Toolbar toolbar;
    private TreeMap<Long, Integer> nextParkingTickets;
    private List<Long> voiceMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set the toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);

        if (checkAndRequestPermissions()) {
            initializeObjects();
            loadSharedPreferences(StaticFields.CITY);
            loadSharedPreferences(StaticFields.TELEPHONE_NUMBER);
            loadSharedPreferences(StaticFields.LICENSE_PLATE);
            loadSharedPreferences(StaticFields.WAIT_MINUTES);
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
                if(isChecked) {
                    isStopTimePicker=true;
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

        spinnerLicensePlate = (Spinner) findViewById(R.id.license_plate_spinner);
        spinnerLicensePlate.setPrompt("License Plate");
        ArrayAdapter<CharSequence> adapterLicensePlate = ArrayAdapter.createFromResource(this, R.array.license_plate, android.R.layout.simple_spinner_item);
        adapterLicensePlate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLicensePlate.setAdapter(adapterLicensePlate);
        spinnerLicensePlate.setEnabled(true);
        spinnerLicensePlate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                licensePlate = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerTelephoneNumber = (Spinner) findViewById(R.id.telephone_spinner);
        spinnerTelephoneNumber.setPrompt("Telephone number");
        ArrayAdapter<CharSequence> adapterTelephoneNumber = ArrayAdapter.createFromResource(this, R.array.telephoneNumber, android.R.layout.simple_spinner_item);
        adapterTelephoneNumber.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTelephoneNumber.setAdapter(adapterTelephoneNumber);
        spinnerTelephoneNumber.setEnabled(true);
        spinnerTelephoneNumber.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                telephoneNumber = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void prepareAlarmManager(ParkscheinCollection parkscheinCollection) {
        long plannedTime = parkscheinCollection.getNextParkingTickets().firstKey();
        int size = parkscheinCollection.getNextParkingTickets().size();

        intent.putExtra(StaticFields.PARKSCHEIN_POJO, parkscheinCollection);
        intent.setAction("AlarmManager");

        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        triggerAlarmManager(plannedTime, size, isVoiceMessageActivated);
    }

    private void triggerAlarmManager(long plannedTime, int size, boolean isVoiceMessageActivated) {
        if(isVoiceMessageActivated) {
            if (size > 0) {
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedTime, pendingIntent);
            } else {
                AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setAlarmClock(ac, pendingIntent);
            }
        } else {
            if (size > 0) {
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, plannedTime, pendingIntent);
            } else {
                AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
                AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
                alarmManager.setAlarmClock(ac, pendingIntent);
            }
        }
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
                || city.equals("Villach Zone 1")
                || city.equals("Zell am See")))
            return true;
        else
            return false;
    }

    private void triggerCancellationAlarmManager() {
        if(isCityStop()) {
            parkscheinCollection = new ParkscheinCollection(city, nextParkingTickets, licensePlate, telephoneNumber, true);

            intent.putExtra(StaticFields.STOP_SMS, parkscheinCollection);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), StaticFields.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            AlarmManager.AlarmClockInfo ac = new AlarmManager.AlarmClockInfo(System.currentTimeMillis(), pendingIntent);
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setAlarmClock(ac, pendingIntent);
        } else {
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

            intent.setAction("AlarmManager");
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), StaticFields.requestCode, intent, PendingIntent.FLAG_NO_CREATE |
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

        long intervall = Long.valueOf(numberPicker.getValue());

        CalculationParkingTicket calc = new CalculationParkingTicket(getApplicationContext());
        int durationMinutes = Integer.valueOf(spinnerMinutes.getSelectedItem().toString());
        TreeMap<Long, Integer> nextParkingTicket = calc.calculateNextParkingTicket(hour, minute, hourEnd, minuteEnd, intervall, isStopTimePicker, durationMinutes);

        return nextParkingTicket;
    }

    private void selectedSpinnerCity(AdapterView<?> parent, int position) {
        String item = parent.getItemAtPosition(position).toString();
        city = item;
        if (item.equals("Baden Z1 (Blaue Zone)")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.baden_z1_minutes, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
            selectedSpinnerMinutes(R.array.baden_z1_minutes);
        } else if (item.equals("Baden Z2 (Grüne Zone)")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.baden_z2_minutes, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
            selectedSpinnerMinutes(R.array.baden_z2_minutes);
        } else if (item.equals("Bruck an der Leitha")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.bruck_an_der_leitha_minutes, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Eisenstadt Zone A")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.eisenstadt_zona_a_minutes, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Eisenstadt Zone B")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.eisenstadt_zona_b_minutes, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Eisenstadt Zone C")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.eisenstadt_zona_c_minutes, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Gleisdorf")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.gleisdorf, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Gmunden")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.gmunden, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Graz Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Graz Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Graz Zone 3")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z3, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Graz Zone 5")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z5, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Graz Zone 15")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.graz_z15, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Hall in Tirol")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.hall_in_tirol, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("IIG Parkplatz Sillside")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.iig_parkplatz_sillside, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Innsbruck Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Innsbruck Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Innsbruck Zone 3")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_3, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Innsbruck Zone 4")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_4, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Innsbruck Zone 5")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.innsbruck_zone_5, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Klagenfurt Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klagenfurt_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Klagenfurt Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klagenfurt_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Klosterneuburg Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klosterneuburg_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Klosterneuburg Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klosterneuburg_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Korneuburg")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.korneuburg, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Krems Zone 1 (Blaue Zone)")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.krems_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Krems Zone 2 (Grüne Zone)")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.krems_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Linz Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.linz_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Linz Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.linz_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Linz Zone 3")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.linz_zone_3, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Mödling")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.moedling, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Neusiedl am See Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.neusiedl_am_see_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Neusiedl am See Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.neusiedl_am_see_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Oberwart")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.oberwart, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Parktiger Flughafen Wien")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.parktiger_flughafen_wien, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Parktiger P + R Aspern")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.parktiger_p_r_aspern, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Parktiger P + R Heiligenstadt")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.parktiger_p_r_heiligenstadt, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Perchtoldsdorf Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.perchtoldsdorf_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Perchtoldsdorf Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.perchtoldsdorf_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Pörtschach")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.poertschach, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Ried Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.klagenfurt_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Salzburg Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.salzburg_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Schwechat Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.schwechat_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Schärding Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.schaerding_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Spittal")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.spittal, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("St. Pölten Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.sankt_poelten_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        }  else if (item.equals("St. Pölten Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.sankt_poelten_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        }  else if (item.equals("Steyr Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Steyr Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Steyr Zone 3")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_3, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Steyr Zone 4")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_4, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Steyr Zone 5")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.steyr_zone_5, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Stockerau")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.stockerau, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Tulln")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.tulln, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Velden Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.velden_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Velden Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.velden_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Villach")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.villach, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Weiz Zone A")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.weiz_zone_a, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Weiz Zone B")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.weiz_zone_b, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Wels")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wels, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Wien")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wien, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
            selectedSpinnerMinutes(R.array.wien);
        } else if (item.equals("Wiener Neustadt Zone 1")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wiener_neustadt_zone_1, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Wiener Neustadt Zone 2")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wiener_neustadt_zone_2, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Wipark")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.wipark, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        } else if (item.equals("Zell am See")) {
            spinnerCity = (Spinner) findViewById(R.id.minutes_spinner);
            ArrayAdapter<CharSequence> adapterMinutes = ArrayAdapter.createFromResource(this, R.array.zell_am_see, android.R.layout.simple_spinner_item);
            adapterMinutes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCity.setAdapter(adapterMinutes);
        }
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
        String input;
        SharedPreferences sh;

        switch(sharedPref) {
            case "CITY":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, "WIEN");
                searchForPositions(spinnerCity, input);
                break;
            case "TELEPHONE_NUMBER":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, "06646606000");
                searchForPositions(spinnerTelephoneNumber, input);
                break;
            case "LICENSE_PLATE":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                input = sh.getString(sharedPref, "W-XYZ");
                searchForPositions(spinnerLicensePlate, input);
                break;
            case "WAIT_MINUTES":
                sh = getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
                waitMinutesLong = sh.getInt(sharedPref, 0);
                break;
        }
    }

    private void searchForPositions(Spinner spinner, String sharedPref) {
        ArrayAdapter myAdap = (ArrayAdapter) spinner.getAdapter();
        int spinnerPosition = myAdap.getPosition(sharedPref);

        spinner.setSelection(spinnerPosition);
    }

    //--------------------onclicked methods--------------------//
    public void stopAlarm(View view) {
        triggerCancellationAlarmManager();
    }

    public void showMenu(MenuItem item) {
        onOptionsItemSelected(item);
    }

    public void startAlarm(View view) {
        TreeMap<Long, Integer> nextParkingTickets = calculateNextParkingTicket();

        parkscheinCollection = new ParkscheinCollection(city, nextParkingTickets, licensePlate, telephoneNumber, false);

        prepareAlarmManager(parkscheinCollection);
    }

    //--------------------Activity overriden methods--------------------//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}