package at.co.netconsulting.parkingticket;

import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.compose.ui.platform.ComposeView;

import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.ui.ParkingplaceScreenSetup;

public class Parkingplace extends BaseActivity {

    private boolean gpsEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parkingpplaces);

        gpsEnabled = checkGPSEnabled();
        if (!gpsEnabled) {
            Toast.makeText(getApplicationContext(), R.string.gps_location, Toast.LENGTH_LONG).show();
        }

        ComposeView composeView = findViewById(R.id.compose_view);
        ParkingplaceScreenSetup.init(composeView, this);
    }

    private boolean checkGPSEnabled() {
        final LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public boolean isGPSEnabled() {
        return gpsEnabled;
    }
}
