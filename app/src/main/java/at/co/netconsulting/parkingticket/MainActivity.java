package at.co.netconsulting.parkingticket;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import at.co.netconsulting.parkingticket.CalculateBookings.CalculateBookings;
import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.service.IntentServiceManager;

import static at.co.netconsulting.parkingticket.statics.StaticVariables.*;

public class MainActivity extends BaseActivity {
    TimePicker clockStartTimer, clockEndTimer;
    CheckBox checkBoxEndTimer;
    CalculateBookings calculateBookings;
    Intent intentServiceManager;
    NumberPicker timerIntervall;
    String[] PERMISSIONS= new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission();
        initializeComponents();
    }

    public void initializeComponents() {
        clockStartTimer = (TimePicker) findViewById(R.id.clockStartTimer);
        clockStartTimer.setEnabled(true);
        clockStartTimer.setIs24HourView(true);

        clockEndTimer = (TimePicker) findViewById(R.id.clockEndTimer);
        clockEndTimer.setEnabled(true);
        clockEndTimer.setIs24HourView(true);

        checkBoxEndTimer = (CheckBox) findViewById(R.id.checkBoxEndTimer);
        checkBoxEndTimer.setActivated(false);

        timerIntervall = (NumberPicker) findViewById(R.id.timerIntervall);
        timerIntervall.setMinValue(0);
        timerIntervall.setMaxValue(1440);
        timerIntervall.setWrapSelectorWheel(true);
        timerIntervall.setValue(0);

        calculateBookings = new CalculateBookings();
    }

    public void startTasks() {
        intentServiceManager = new Intent(this, IntentServiceManager.class);
        IntentServiceManager.enqueueWork(this, intentServiceManager);
        startForegroundService(intentServiceManager);
    }

    //Permissions handling

    public void askPermission() {
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS, PERMISSION_ALL);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //Do your work.
                } else {
                    Toast.makeText(this, getResources().getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //
    public void control_handler(View view) {
        Button whichControlWasClicked = (Button)view;
        int tpHour = clockStartTimer.getHour();
        int tpMinute = clockStartTimer.getMinute();
        long whenToStartMilliSeconds = CalculateBookings.convertTimeToMilliseconds(tpHour, tpMinute);

        switch(whichControlWasClicked.getText().toString()) {
            case BUTTON_15:
                calculateBookings.putTimeToTreeMap(whenToStartMilliSeconds, Integer.valueOf(BUTTON_15), timerIntervall.getValue());
                break;
            case BUTTON_30:
                calculateBookings.putTimeToTreeMap(whenToStartMilliSeconds, Integer.valueOf(BUTTON_30), timerIntervall.getValue());
                break;
            case BUTTON_60:
                calculateBookings.putTimeToTreeMap(whenToStartMilliSeconds, Integer.valueOf(BUTTON_60), timerIntervall.getValue());
                break;
            case BUTTON_90:
                calculateBookings.putTimeToTreeMap(whenToStartMilliSeconds, Integer.valueOf(BUTTON_90), timerIntervall.getValue());
                break;
            case BUTTON_120:
                calculateBookings.putTimeToTreeMap(whenToStartMilliSeconds, Integer.valueOf(BUTTON_120), timerIntervall.getValue());
                break;
            case BUTTON_START_BOOKING:
                startTasks();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + whichControlWasClicked.getText().toString());
        }
    }
}