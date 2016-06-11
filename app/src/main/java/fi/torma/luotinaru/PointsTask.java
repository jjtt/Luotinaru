package fi.torma.luotinaru;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

/**
 * This AsyncTask gets the depths and adds them as markers
 */
public class PointsTask extends AsyncTask<String, Void, LinkedList<Point>> {

    public static final String TAG = "PointsTask";

    private final GoogleMap mMap;
    private final Context mContext;
    private final IconGenerator mIconGenerator;
    private final SharedPreferences mSharedPreferences;
    private String mErrorMessage = null;

    public PointsTask(Context context, GoogleMap map) {
        this.mMap = map;
        mContext = context;
        mIconGenerator = new IconGenerator(context);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected LinkedList<Point> doInBackground(String... params) {
        String addr = params[0];

        String file = mSharedPreferences.getString("file", "");

        if (!file.trim().isEmpty()) {
            file = "?id=" + file;
        }

        LinkedList<Point> points = new LinkedList<>();

        try {
            URL url = new URL("http://" + addr + "/cgi-bin/latest.py" + file);

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

        } catch (IOException | NumberFormatException e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
            mErrorMessage = "" + e.getLocalizedMessage();
        }

        Log.d(TAG, "Returning a list of " + points.size() + " points");
        return points;
    }

    @Override
    protected void onPostExecute(LinkedList<Point> points) {
        Log.d(TAG, "Processing the list");

        if (mErrorMessage != null) {
            Log.d(TAG, "Errors in retrieving the points: " + mErrorMessage);
            Toast.makeText(mContext, mErrorMessage, Toast.LENGTH_LONG).show();
        }

        if (points.isEmpty()) {
            Log.d(TAG, "No points");
            Toast.makeText(mContext, "No points", Toast.LENGTH_LONG).show();

            if ("true".equals(mSharedPreferences.getString("debug", "false"))) {
                double lat = 59.98843285;
                double lon = 24.57099222;
                for (int i=0; i<20; i++) {
                    points.add(new Point(lat + i * 0.000005, lon + i * 0.000005, 1.0+i*0.1));
                }
            } else {
                return;
            }
        }

        int skip = getSkipFromPreferences();
        int count = -1;

        for (Point point : points) {
            count++;

            if (count % skip != 0) {
                continue;
            }

            String visualizationType = mSharedPreferences.getString("visualization_type", "circles");
            switch (visualizationType) {
                case "icons":
                    Bitmap icon = mIconGenerator.makeIcon(String.valueOf(point.getDepth()));

                    //FIXME: OOM here from the fromBitmap-method with a list of justh 477 points
                    mMap.addMarker(new MarkerOptions()
                            .position(point.getLatLng())
                            .icon(BitmapDescriptorFactory.fromBitmap(icon))
                            .title(point.toString()));
                    break;
                case "circles":
                    mMap.addCircle(new CircleOptions()
                            .center(point.getLatLng())
                            .radius(getRadiusFromPreferences())
                            .strokeColor(Color.TRANSPARENT)
                            .fillColor(colorForPoint(point)));
                    break;
            }
        }

        if (mSharedPreferences.getBoolean("zoom_to_latest", true)) {
            // Zoom map to latest point
            Point point = points.getLast();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    point.getLatLng(),
                    19);
            mMap.moveCamera(cameraUpdate);
        }
    }

    /**
     * This method returns a color used to draw a point when using circles
     * @param point
     * @return
     */
    private int colorForPoint(Point point) {
        String color;
        if (point.getDepth() < getDepthThresholdFromPreferences()) {
            color = mSharedPreferences.getString("visualization_color_shallow", "#33FF0000");
        } else {
            color = mSharedPreferences.getString("visualization_color_deep", "#3300FF00");
        }
        return Color.parseColor(color);
    }

    /**
     * Helper method for getting the current skip_points setting
     *
     * @return
     */
    private int getSkipFromPreferences() {
        String str = mSharedPreferences.getString("skip_points", "1");

        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    /**
     * Helper method for getting the current visualization_depth setting
     *
     * @return
     */
    private double getDepthThresholdFromPreferences() {
        String str = mSharedPreferences.getString("visualization_depth", "2.0");

        try {
            return Double.valueOf(str);
        } catch (NumberFormatException ex) {
            return 2.0;
        }
    }

    /**
     * Helper method for getting the current visualization_radius setting
     *
     * @return
     */
    private double getRadiusFromPreferences() {
        String str = mSharedPreferences.getString("visualization_radius", "1.0");

        try {
            return Double.valueOf(str);
        } catch (NumberFormatException ex) {
            return 1.0;
        }
    }
}
