package at.co.netconsulting.parkingticket;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TimePicker;
import at.co.netconsulting.parkingticket.CalculateBookings.CalculateBookings;
import at.co.netconsulting.parkingticket.general.BaseActivity;
import static at.co.netconsulting.parkingticket.statics.StaticVariables.*;

public class MainActivity extends BaseActivity {
    TimePicker clockStartTimer, clockEndTimer;
    CheckBox checkBoxEndTimer;
    CalculateBookings calculateBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        startTasks();
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

        calculateBookings = new CalculateBookings();
    }

    public void startTasks() {

    }

    public void button_minutes(View view) {
        Button whichControlWasClicked = (Button)view;
        int tpHour = clockStartTimer.getHour();
        int tpMinute = clockStartTimer.getMinute();
        long whenToStartMilliSeconds = CalculateBookings.convertTimeToMilliseconds(tpHour, tpMinute);

        switch(whichControlWasClicked.getText().toString()) {
            case BUTTON_15:
                calculateBookings.addIntervall(whenToStartMilliSeconds, Integer.parseInt(BUTTON_15));
                break;
            case BUTTON_30:
                calculateBookings.addIntervall(whenToStartMilliSeconds, Integer.parseInt(BUTTON_30));
                break;
            case BUTTON_60:
                calculateBookings.addIntervall(whenToStartMilliSeconds, Integer.parseInt(BUTTON_60));
                break;
            case BUTTON_90:
                calculateBookings.addIntervall(whenToStartMilliSeconds, Integer.parseInt(BUTTON_90));
                break;
            case BUTTON_120:
                calculateBookings.addIntervall(whenToStartMilliSeconds, Integer.parseInt(BUTTON_120));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + whichControlWasClicked.getText().toString());
        }
    }
}