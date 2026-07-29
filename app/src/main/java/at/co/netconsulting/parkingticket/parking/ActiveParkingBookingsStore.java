package at.co.netconsulting.parkingticket.parking;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import at.co.netconsulting.parkingticket.pojo.ParkscheinCollection;

public final class ActiveParkingBookingsStore {
    private static final String PREFS = "ACTIVE_PARKING_BOOKINGS";
    private static final String KEY_COLLECTIONS = "COLLECTIONS";

    private ActiveParkingBookingsStore() {
    }

    public static synchronized List<ParkscheinCollection> load(Context context) {
        SharedPreferences preferences = preferences(context);
        String encoded = preferences.getString(KEY_COLLECTIONS, "");
        if (encoded == null || encoded.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            byte[] data = Base64.decode(encoded, Base64.DEFAULT);
            ObjectInputStream input =
                    new ObjectInputStream(new ByteArrayInputStream(data));
            Object object = input.readObject();
            input.close();

            ArrayList<ParkscheinCollection> result = new ArrayList<>();
            if (object instanceof List<?>) {
                for (Object item : (List<?>) object) {
                    if (item instanceof ParkscheinCollection) {
                        ParkscheinCollection collection = (ParkscheinCollection) item;
                        if (hasTickets(collection)) {
                            result.add(collection);
                        }
                    }
                }
            }
            return result;
        } catch (Exception e) {
            preferences.edit().remove(KEY_COLLECTIONS).apply();
            return new ArrayList<>();
        }
    }

    public static synchronized void addOrReplace(
            Context context,
            ParkscheinCollection collection
    ) {
        if (!hasTickets(collection)) {
            return;
        }
        List<ParkscheinCollection> collections = load(context);
        replaceOrAppend(collections, collection);
        save(context, collections);
    }

    public static synchronized ParkscheinCollection findByRequestCode(
            Context context,
            int requestCode
    ) {
        for (ParkscheinCollection collection : load(context)) {
            if (collection.getAlarmRequestCode() == requestCode) {
                return collection;
            }
        }
        return null;
    }

    public static synchronized void updateOrRemove(
            Context context,
            ParkscheinCollection collection
    ) {
        List<ParkscheinCollection> collections = load(context);
        removeByRequestCode(collections, collection.getAlarmRequestCode());
        if (hasTickets(collection)) {
            collections.add(collection);
        }
        save(context, collections);
    }

    public static synchronized void removeByRequestCode(Context context, int requestCode) {
        List<ParkscheinCollection> collections = load(context);
        removeByRequestCode(collections, requestCode);
        save(context, collections);
    }

    public static long earliestNextTicketTime(List<ParkscheinCollection> collections) {
        long earliest = Long.MAX_VALUE;
        for (ParkscheinCollection collection : collections) {
            if (hasTickets(collection)) {
                earliest = Math.min(
                        earliest,
                        collection.getNextParkingTickets().firstKey()
                );
            }
        }
        return earliest;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static void save(Context context, List<ParkscheinCollection> collections) {
        ArrayList<ParkscheinCollection> validCollections = new ArrayList<>();
        for (ParkscheinCollection collection : collections) {
            if (hasTickets(collection)) {
                validCollections.add(collection);
            }
        }

        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(bytes);
            output.writeObject(validCollections);
            output.close();

            preferences(context).edit()
                    .putString(
                            KEY_COLLECTIONS,
                            Base64.encodeToString(bytes.toByteArray(), Base64.NO_WRAP)
                    )
                    .apply();
        } catch (Exception e) {
            preferences(context).edit().remove(KEY_COLLECTIONS).apply();
        }
    }

    private static void replaceOrAppend(
            List<ParkscheinCollection> collections,
            ParkscheinCollection collection
    ) {
        removeByRequestCode(collections, collection.getAlarmRequestCode());
        collections.add(collection);
    }

    private static void removeByRequestCode(
            List<ParkscheinCollection> collections,
            int requestCode
    ) {
        for (int i = collections.size() - 1; i >= 0; i--) {
            if (collections.get(i).getAlarmRequestCode() == requestCode) {
                collections.remove(i);
            }
        }
    }

    private static boolean hasTickets(ParkscheinCollection collection) {
        return collection != null
                && collection.getNextParkingTickets() != null
                && !collection.getNextParkingTickets().isEmpty();
    }
}
