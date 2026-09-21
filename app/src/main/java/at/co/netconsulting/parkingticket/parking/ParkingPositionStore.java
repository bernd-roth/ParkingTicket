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
    private static final String TRACKING = "tracking";
    private static final String TRACKING_SESSIONS = "tracking_sessions";
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
                .putBoolean(TRACKING, false)
                .putInt(TRACKING_SESSIONS, 0)
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

    public static boolean isTracking(Context context) {
        return hasCar(context) && preferences(context).getBoolean(TRACKING, false);
    }

    /**
     * Starts a tracking session. The first session continues from the car position; every later
     * session is recorded as a separate segment so the pause between sessions is not drawn as a line.
     */
    public static synchronized void startTracking(Context context) {
        if (!hasCar(context) || isTracking(context)) return;
        SharedPreferences preferences = preferences(context);
        int sessions = preferences.getInt(TRACKING_SESSIONS, 0);
        SharedPreferences.Editor editor = preferences.edit()
                .putBoolean(TRACKING, true)
                .putInt(TRACKING_SESSIONS, sessions + 1);
        if (sessions > 0) {
            editor.putString(PATH, preferences.getString(PATH, "") + ParkingPathCodec.SEGMENT_SEPARATOR + '\n');
        }
        editor.apply();
    }

    public static synchronized void stopTracking(Context context) {
        preferences(context).edit().putBoolean(TRACKING, false).apply();
    }

    public static synchronized void appendLocation(Context context, Location location) {
        if (!isTracking(context) || (location.hasAccuracy() && location.getAccuracy() > 75f)) return;
        List<List<ParkingPathCodec.Point>> segments = new ArrayList<>(getPathSegments(context));
        if (segments.isEmpty()) segments.add(new ArrayList<>());
        List<ParkingPathCodec.Point> points = new ArrayList<>(segments.get(segments.size() - 1));
        if (!points.isEmpty()) {
            ParkingPathCodec.Point last = points.get(points.size() - 1);
            float[] distance = new float[1];
            Location.distanceBetween(last.latitude, last.longitude,
                    location.getLatitude(), location.getLongitude(), distance);
            if (distance[0] < MIN_POINT_DISTANCE_METERS) return;
        }
        points.add(new ParkingPathCodec.Point(location.getLatitude(), location.getLongitude(),
                location.getTime() > 0 ? location.getTime() : System.currentTimeMillis()));
        segments.set(segments.size() - 1, points);
        trimOldestPoints(segments);
        preferences(context).edit().putString(PATH, ParkingPathCodec.encodeSegments(segments)).apply();
    }

    private static void trimOldestPoints(List<List<ParkingPathCodec.Point>> segments) {
        int total = 0;
        for (List<ParkingPathCodec.Point> segment : segments) total += segment.size();
        while (total > MAX_PATH_POINTS && segments.size() > 0) {
            List<ParkingPathCodec.Point> oldest = segments.get(0);
            int remove = Math.min(total - MAX_PATH_POINTS, oldest.size());
            total -= remove;
            if (remove == oldest.size() && segments.size() > 1) {
                segments.remove(0);
            } else {
                segments.set(0, new ArrayList<>(oldest.subList(remove, oldest.size())));
            }
        }
    }

    /** All recorded points of all tracking sessions as one flat list. */
    public static List<ParkingPathCodec.Point> getPath(Context context) {
        return ParkingPathCodec.decode(preferences(context).getString(PATH, ""));
    }

    /** Recorded points grouped by tracking session. */
    public static List<List<ParkingPathCodec.Point>> getPathSegments(Context context) {
        return ParkingPathCodec.decodeSegments(preferences(context).getString(PATH, ""));
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
