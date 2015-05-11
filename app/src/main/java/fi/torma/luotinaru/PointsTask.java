package fi.torma.luotinaru;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.Collections;
import java.util.List;

/**
 * This AsyncTask gets the depths and adds them as markers
 */
public class PointsTask extends AsyncTask<Void, Void, List<Point>> {

    public static final String TAG = "PointsTask";

    private final GoogleMap mMap;
    private final IconGenerator mIconGenerator;

    public PointsTask(Context context, GoogleMap map) {
        this.mMap = map;
        mIconGenerator = new IconGenerator(context);
    }

    @Override
    protected List<Point> doInBackground(Void... params) {
        Log.d(TAG, "Returning a list of one");
        return Collections.singletonList(new Point(60.0, 25.0, 0.0));
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
