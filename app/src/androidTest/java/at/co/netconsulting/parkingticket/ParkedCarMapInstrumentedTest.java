package at.co.netconsulting.parkingticket;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.location.Location;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import at.co.netconsulting.parkingticket.parking.ParkingPathCodec;
import at.co.netconsulting.parkingticket.parking.ParkingPositionStore;

/** Runs on a phone/emulator and opens the real OpenStreetMap screen with a known Vienna trail. */
@RunWith(AndroidJUnit4.class)
public class ParkedCarMapInstrumentedTest {
    private static final double CAR_LATITUDE = 48.208174;
    private static final double CAR_LONGITUDE = 16.373819;
    private Context context;

    @Before
    public void seedParkedCarAndCoveredPath() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ParkingPositionStore.clear(context);
        ParkingPositionStore.saveCar(context, location(CAR_LATITUDE, CAR_LONGITUDE, 1_000L));
        ParkingPositionStore.startTracking(context);
        ParkingPositionStore.appendLocation(context, location(48.208520, 16.374180, 2_000L));
        ParkingPositionStore.appendLocation(context, location(48.208890, 16.374620, 3_000L));
        ParkingPositionStore.appendLocation(context, location(48.209260, 16.375110, 4_000L));
        ParkingPositionStore.appendLocation(context, location(48.209640, 16.375570, 5_000L));
        ParkingPositionStore.stopTracking(context);
        // Ignored: tracking is off.
        ParkingPositionStore.appendLocation(context, location(48.210000, 16.376000, 6_000L));
        ParkingPositionStore.startTracking(context);
        ParkingPositionStore.appendLocation(context, location(48.209300, 16.375200, 7_000L));
        ParkingPositionStore.appendLocation(context, location(48.208600, 16.374300, 8_000L));
    }

    @After
    public void clearTestPosition() {
        if (!InstrumentationRegistry.getArguments().getBoolean("keepDemoData", false)) {
            ParkingPositionStore.clear(context);
        }
    }

    @Test
    public void parkedCarAndCoveredPathAreShownOnMap() {
        ParkingPositionStore.ParkedCar car = ParkingPositionStore.getCar(context);
        assertNotNull(car);
        assertEquals(CAR_LATITUDE, car.latitude, 0.000001);
        assertEquals(CAR_LONGITUDE, car.longitude, 0.000001);

        List<ParkingPathCodec.Point> path = ParkingPositionStore.getPath(context);
        assertEquals(7, path.size());
        List<List<ParkingPathCodec.Point>> segments = ParkingPositionStore.getPathSegments(context);
        assertEquals(2, segments.size());
        assertEquals(5, segments.get(0).size());
        assertEquals(2, segments.get(1).size());
        assertTrue(ParkingPositionStore.isTracking(context));

        try (ActivityScenario<ParkedCarActivity> ignored = ActivityScenario.launch(ParkedCarActivity.class)) {
            onView(withContentDescription(R.string.parked_car_map)).check(matches(isDisplayed()));
        }
    }

    private static Location location(double latitude, double longitude, long time) {
        Location location = new Location("instrumented-test");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAccuracy(5f);
        location.setTime(time);
        return location;
    }
}
