package at.co.netconsulting.parkingticket.parking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A small, dependency-free codec used to persist the recorded walking trail. */
public final class ParkingPathCodec {
    /** Line that separates two independently recorded tracking sessions. */
    public static final String SEGMENT_SEPARATOR = "|";

    private ParkingPathCodec() {}

    public static String encode(List<Point> points) {
        StringBuilder result = new StringBuilder();
        for (Point point : points) {
            result.append(point.latitude).append(',')
                    .append(point.longitude).append(',')
                    .append(point.timestamp).append('\n');
        }
        return result.toString();
    }

    public static String encodeSegments(List<List<Point>> segments) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            if (i > 0) result.append(SEGMENT_SEPARATOR).append('\n');
            result.append(encode(segments.get(i)));
        }
        return result.toString();
    }

    /** Decodes the points of all segments as one flat list. */
    public static List<Point> decode(String encoded) {
        List<Point> points = new ArrayList<>();
        for (List<Point> segment : decodeSegments(encoded)) points.addAll(segment);
        return points;
    }

    public static List<List<Point>> decodeSegments(String encoded) {
        if (encoded == null || encoded.trim().isEmpty()) return Collections.emptyList();
        List<List<Point>> segments = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        segments.add(points);
        for (String line : encoded.split("\\n")) {
            if (line.trim().equals(SEGMENT_SEPARATOR)) {
                points = new ArrayList<>();
                segments.add(points);
                continue;
            }
            String[] values = line.split(",");
            if (values.length != 3) continue;
            try {
                double latitude = Double.parseDouble(values[0]);
                double longitude = Double.parseDouble(values[1]);
                long timestamp = Long.parseLong(values[2]);
                if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180) {
                    points.add(new Point(latitude, longitude, timestamp));
                }
            } catch (NumberFormatException ignored) {
                // Ignore a damaged point while retaining the rest of the trail.
            }
        }
        return segments;
    }

    public static final class Point {
        public final double latitude;
        public final double longitude;
        public final long timestamp;

        public Point(double latitude, double longitude, long timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }
    }
}
