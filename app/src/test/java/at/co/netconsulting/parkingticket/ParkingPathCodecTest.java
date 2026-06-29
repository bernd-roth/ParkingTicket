package at.co.netconsulting.parkingticket;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import at.co.netconsulting.parkingticket.parking.ParkingPathCodec;

import static org.junit.Assert.assertEquals;

public class ParkingPathCodecTest {
    @Test
    public void roundTripPreservesTrail() {
        List<ParkingPathCodec.Point> source = Arrays.asList(
                new ParkingPathCodec.Point(48.2082, 16.3738, 1000L),
                new ParkingPathCodec.Point(48.2090, 16.3750, 2000L));

        List<ParkingPathCodec.Point> decoded = ParkingPathCodec.decode(ParkingPathCodec.encode(source));

        assertEquals(2, decoded.size());
        assertEquals(48.2082, decoded.get(0).latitude, 0.000001);
        assertEquals(16.3750, decoded.get(1).longitude, 0.000001);
        assertEquals(2000L, decoded.get(1).timestamp);
    }

    @Test
    public void damagedAndOutOfRangePointsAreSkipped() {
        String encoded = "48.2,16.3,1000\ninvalid\n95.0,16.3,2000\n48.3,16.4,3000\n";

        List<ParkingPathCodec.Point> decoded = ParkingPathCodec.decode(encoded);

        assertEquals(2, decoded.size());
        assertEquals(3000L, decoded.get(1).timestamp);
    }
}
