package at.co.netconsulting.parkingticket;

import android.os.Bundle;

import androidx.compose.ui.platform.ComposeView;

import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.ui.SettingsScreenSetup;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ComposeView composeView = findViewById(R.id.compose_view);
        SettingsScreenSetup.init(composeView, this);
    }
}
