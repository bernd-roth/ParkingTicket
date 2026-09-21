package at.co.netconsulting.parkingticket.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import at.co.netconsulting.parkingticket.ParkedCarActivity;
import at.co.netconsulting.parkingticket.R;
import at.co.netconsulting.parkingticket.parking.ParkingPositionStore;

public class ParkingLocationService extends Service implements LocationListener {
    public static final String ACTION_STOP_TRACKING = "at.co.netconsulting.parkingticket.action.STOP_TRACKING";
    private static final String CHANNEL_ID = "parking_position_tracking";
    private static final int NOTIFICATION_ID = 2104;
    private LocationManager locationManager;
    private boolean updatesRegistered;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP_TRACKING.equals(intent.getAction())) {
            ParkingPositionStore.stopTracking(this);
        }
        if (!ParkingPositionStore.isTracking(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }
        startForeground(NOTIFICATION_ID, createNotification());
        startLocationUpdates();
        return START_STICKY;
    }

    private void startLocationUpdates() {
        if (updatesRegistered) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4_000L, 4f, this);
            updatesRegistered = true;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 8_000L, 8f, this);
            updatesRegistered = true;
        }
        if (!updatesRegistered) stopSelf();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        ParkingPositionStore.appendLocation(this, location);
    }

    @Override
    public void onDestroy() {
        if (locationManager != null) locationManager.removeUpdates(this);
        updatesRegistered = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                getString(R.string.parking_tracking_channel), NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private Notification createNotification() {
        Intent mapIntent = new Intent(this, ParkedCarActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Intent stopIntent = new Intent(this, ParkingLocationService.class).setAction(ACTION_STOP_TRACKING);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.parking_tracking_title))
                .setContentText(getString(R.string.parking_tracking_text))
                .setContentIntent(pendingIntent)
                .addAction(0, getString(R.string.stop_tracking), stopPendingIntent)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }
}
