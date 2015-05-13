package fi.torma.luotinaru;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

/**
 * This AsyncTask gets the depths and adds them as markers
 */
public class PointsTask extends AsyncTask<String, Void, LinkedList<Point>> {

    public static final String TAG = "PointsTask";

    private final GoogleMap mMap;
    private final IconGenerator mIconGenerator;

    public PointsTask(Context context, GoogleMap map) {
        this.mMap = map;
        mIconGenerator = new IconGenerator(context);
    }

    @Override
    protected LinkedList<Point> doInBackground(String... params) {
        String addr = params[0];

        LinkedList<Point> points = new LinkedList<>();

        try {
            URL url = new URL("http://" + addr + "/cgi-bin/latest.py");

            Log.d(TAG, url.toString());

            URLConnection conn = url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");

                    points.add(new Point(
                            Double.valueOf(split[0]),
                            Double.valueOf(split[1]),
                            Double.valueOf(split[2])
                    ));
                }

            }

        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

        Log.d(TAG, "Returning a list of " + points.size() + " points");
        return points;
    }

    @Override
    protected void onPostExecute(LinkedList<Point> points) {
        Log.d(TAG, "Processing the list");

        if (points.isEmpty()) {
            Log.d(TAG, "No points");
            return;
        }

        for (Point point : points) {

            Bitmap icon = mIconGenerator.makeIcon(String.valueOf(point.getDepth()));

            //FIXME: OOM here from the fromBitmap-method with a list of justh 477 points
            mMap.addMarker(new MarkerOptions()
                    .position(point.getLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .title(point.toString()));
        }

        // Zoom map to latest point
        Point point = points.getLast();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                point.getLatLng(),
                19);
        mMap.moveCamera(cameraUpdate);
    }
}
