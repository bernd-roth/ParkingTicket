package at.co.netconsulting.parkingticket;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.TimePicker;
import android.widget.Toolbar;

import at.co.netconsulting.parkingticket.general.BaseActivity;

public class MainActivity extends BaseActivity {
    TimePicker clockStartTimer, clockEndTimer;
    CheckBox checkBoxEndTimer;

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
    }

    public void startTasks() {

    }
}