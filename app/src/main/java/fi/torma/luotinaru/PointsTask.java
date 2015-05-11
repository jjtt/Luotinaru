package fi.torma.luotinaru;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
public class PointsTask extends AsyncTask<String, Void, List<Point>> {

    public static final String TAG = "PointsTask";

    private final GoogleMap mMap;
    private final IconGenerator mIconGenerator;

    public PointsTask(Context context, GoogleMap map) {
        this.mMap = map;
        mIconGenerator = new IconGenerator(context);
    }

    @Override
    protected List<Point> doInBackground(String... params) {
        String addr = params[0];

        List<Point> points = new LinkedList<>();

        try {
            URL url = new URL("http://" + addr + "/points.csv");

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
    protected void onPostExecute(List<Point> points) {
        Log.d(TAG, "Processing the list");

        for (Point point : points) {

            Log.d(TAG, point.toString());

            Bitmap icon = mIconGenerator.makeIcon(String.valueOf(point.getDepth()));

            mMap.addMarker(new MarkerOptions()
                    .position(point.getLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .title(point.toString()));
        }
    }
}
