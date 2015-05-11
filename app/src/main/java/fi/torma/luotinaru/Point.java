package fi.torma.luotinaru;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class represents a single lat-lon point with a depth value
 */
public class Point {
    private double lat;
    private double lon;
    private double depth;

    public Point(double lat, double lon, double depth) {
        this.lat = lat;
        this.lon = lon;
        this.depth = depth;
    }

    public LatLng getLatLng() {
        return new LatLng(lat, lon);
    }

    public double getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "Point{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", depth=" + depth +
                '}';
    }
}
