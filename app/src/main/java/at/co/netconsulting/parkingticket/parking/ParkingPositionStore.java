package at.co.netconsulting.parkingticket.parking;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public final class ParkingPositionStore {
    private static final String PREFERENCES = "parked_car";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String SAVED_AT = "saved_at";
    private static final String PATH = "path";
    private static final int MAX_PATH_POINTS = 2_000;
    private static final float MIN_POINT_DISTANCE_METERS = 4f;

    private ParkingPositionStore() {}

    public static void saveCar(Context context, Location location) {
        long timestamp = location.getTime() > 0 ? location.getTime() : System.currentTimeMillis();
        List<ParkingPathCodec.Point> initialPath = new ArrayList<>();
        initialPath.add(new ParkingPathCodec.Point(location.getLatitude(), location.getLongitude(), timestamp));
        preferences(context).edit()
                .putString(LATITUDE, Double.toString(location.getLatitude()))
                .putString(LONGITUDE, Double.toString(location.getLongitude()))
                .putLong(SAVED_AT, timestamp)
                .putString(PATH, ParkingPathCodec.encode(initialPath))
                .apply();
    }

    public static boolean hasCar(Context context) {
        return preferences(context).contains(LATITUDE) && preferences(context).contains(LONGITUDE);
    }

    public static ParkedCar getCar(Context context) {
        SharedPreferences preferences = preferences(context);
        try {
            return new ParkedCar(
                    Double.parseDouble(preferences.getString(LATITUDE, "")),
                    Double.parseDouble(preferences.getString(LONGITUDE, "")),
                    preferences.getLong(SAVED_AT, 0L));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static synchronized void appendLocation(Context context, Location location) {
        if (!hasCar(context) || (location.hasAccuracy() && location.getAccuracy() > 75f)) return;
        List<ParkingPathCodec.Point> points = new ArrayList<>(getPath(context));
        if (!points.isEmpty()) {
            ParkingPathCodec.Point last = points.get(points.size() - 1);
            float[] distance = new float[1];
            Location.distanceBetween(last.latitude, last.longitude,
                    location.getLatitude(), location.getLongitude(), distance);
            if (distance[0] < MIN_POINT_DISTANCE_METERS) return;
        }
        points.add(new ParkingPathCodec.Point(location.getLatitude(), location.getLongitude(),
                location.getTime() > 0 ? location.getTime() : System.currentTimeMillis()));
        if (points.size() > MAX_PATH_POINTS) {
            points = new ArrayList<>(points.subList(points.size() - MAX_PATH_POINTS, points.size()));
        }
        preferences(context).edit().putString(PATH, ParkingPathCodec.encode(points)).apply();
    }

    public static List<ParkingPathCodec.Point> getPath(Context context) {
        return ParkingPathCodec.decode(preferences(context).getString(PATH, ""));
    }

    public static void clear(Context context) {
        preferences(context).edit().clear().apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static final class ParkedCar {
        public final double latitude;
        public final double longitude;
        public final long savedAt;

        ParkedCar(double latitude, double longitude, long savedAt) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.savedAt = savedAt;
        }
    }
}
