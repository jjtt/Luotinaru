package fi.torma.luotinaru;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MapsActivity extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = "MapsActivity";

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        Button stop = (Button) findViewById(R.id.button_stop);
        stop.setOnClickListener(this);
        Button refresh = (Button) findViewById(R.id.button_refresh);
        refresh.setOnClickListener(this);
        Button clear = (Button) findViewById(R.id.button_clear);
        clear.setOnClickListener(this);
        Button settings = (Button) findViewById(R.id.button_settings);
        settings.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        WlanClientFinder finder = new WlanClientFinder("c8:3a:35:c1:3a:98");

        mClient = finder.find(PreferenceManager.getDefaultSharedPreferences(this));

        Log.d(TAG, String.format("client address=%s", mClient));

        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));

        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // redraw the marker when get location update.
                drawMarker(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (location != null) {
            // zoom
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    10);
            mMap.moveCamera(cameraUpdate);

            // initial marker
            drawMarker(location);
        }

        locationManager.requestLocationUpdates(provider, 5000, 0, locationListener);

        requestPoints();
    }

    private void requestPoints() {
        new PointsTask(this, mMap).execute(mClient);
    }

    private void drawMarker(Location location) {
        // Remove any existing markers on the map
        //mMap.clear();
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .snippet("Lat:" + location.getLatitude() + "Lng:" + location.getLongitude())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("ME"));
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;

        switch (button.getId()) {
            case R.id.button_stop:
                // start an async task to request a stop
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            URL url = new URL("http://" + mClient + "/cgi-bin/stop.py");

                            URLConnection conn = url.openConnection();
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                                String line = reader.readLine();

                                Log.d(TAG, "Stop response: " + line);
                            }
                        } catch (IOException e) {
                            Log.d(TAG, e.getLocalizedMessage());
                        }

                        return null;
                    }
                }.execute();


                break;
            case R.id.button_refresh:
                requestPoints();
                break;
            case R.id.button_clear:
                mMap.clear();
                break;
            case R.id.button_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }
}
