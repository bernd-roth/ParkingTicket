package at.co.netconsulting.parkingticket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.compose.ui.platform.ComposeView;
import androidx.core.content.ContextCompat;

import at.co.netconsulting.parkingticket.general.BaseActivity;
import at.co.netconsulting.parkingticket.parking.ParkingPositionStore;
import at.co.netconsulting.parkingticket.service.ParkingLocationService;
import at.co.netconsulting.parkingticket.ui.ParkedCarScreenSetup;

public class ParkedCarActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ParkingPositionStore.hasCar(this)) {
            Toast.makeText(this, R.string.no_parked_car, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        ComposeView composeView = findViewById(R.id.compose_view);
        ParkedCarScreenSetup.init(composeView, this);
    }

    public void startTracking() {
        ParkingPositionStore.startTracking(this);
        ContextCompat.startForegroundService(this, new Intent(this, ParkingLocationService.class));
        Toast.makeText(this, R.string.tracking_started, Toast.LENGTH_SHORT).show();
    }

    public void stopTracking() {
        ParkingPositionStore.stopTracking(this);
        stopService(new Intent(this, ParkingLocationService.class));
        Toast.makeText(this, R.string.tracking_stopped, Toast.LENGTH_SHORT).show();
    }

    public void clearParkedCar() {
        ParkingPositionStore.clear(this);
        stopService(new Intent(this, ParkingLocationService.class));
        finish();
    }
}
