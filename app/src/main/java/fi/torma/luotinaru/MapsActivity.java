package fi.torma.luotinaru;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MapsActivity extends FragmentActivity implements View.OnClickListener {

    public static final String TAG = "MapsActivity";
    private static final int SELECT_FILE_REQUEST = 1;
    private static final int SETTINGS_REQUEST = 2;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private String mClient;
    private Marker mMyLocation;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // redraw the marker when get location update.
            drawMyLocationMarker(location);
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

    ;

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
        Button list = (Button) findViewById(R.id.button_list);
        list.setOnClickListener(this);
        Button shutdown = (Button) findViewById(R.id.button_shutdown);
        shutdown.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // netvjork dongle
        WlanClientFinder finder = new WlanClientFinder("c8:3a:35:c1:3a:98");

        // rpi3 wlan
        //WlanClientFinder finder = new WlanClientFinder("b8:27:eb:30:04:98");

        mClient = finder.find(PreferenceManager.getDefaultSharedPreferences(this));

        Log.d(TAG, String.format("client address=%s", mClient));

        resumeLocationUpdates();

        requestPoints();
    }

    @Override
    protected void onPause() {
        super.onPause();

        pauseLocationUpdates();
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
        baseMapFromSettings();

        // Enabling MyLocation Layer of Google Map
        mMap.setMyLocationEnabled(true);
    }

    /**
     * This method should be called onResume to start a location listener and zoom the map to
     * the latest location
     */
    private void resumeLocationUpdates() {

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null && mMap != null) {
            // zoom
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    10);
            mMap.moveCamera(cameraUpdate);

            // initial marker
            drawMyLocationMarker(location);
        }

        locationManager.requestLocationUpdates(provider, 5000, 0, mLocationListener);
    }

    /**
     * This method should be called onPause to avoid keeping the GPS on while the map is in the
     * background
     */
    private void pauseLocationUpdates() {
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationManager.removeUpdates(mLocationListener);
    }

    /**
     * This method executes a new AsyncTask fetching the points
     */
    private void requestPoints() {
        if ("true".equals(PreferenceManager.getDefaultSharedPreferences(this).getString("debug", "false"))
            || (mMap != null && mClient != null)) {
            Log.d(TAG, "Requesting points");
            new PointsTask(this, mMap).execute(mClient);
        } else {
            Log.d(TAG, "No map or no client - Not requesting points");
        }
    }

    /**
     * This method adds or moves the marker showing the users current location
     *
     * @param location
     * @return
     */
    private void drawMyLocationMarker(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        drawMyLocationMarker(currentPosition);
    }

    /**
     * This method adds or moves the marker showing the position given as LatLng
     *
     * @param currentPosition
     * @return
     */
    private void drawMyLocationMarker(LatLng currentPosition) {
        Log.d("location", currentPosition.toString());
        if (mMyLocation == null) {
            mMyLocation = mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .snippet("Lat:" + currentPosition.latitude + "Lng:" + currentPosition.longitude)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Käyttäjän sijainti"));
        } else {
            mMyLocation.setPosition(currentPosition);
        }
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
                if (mMyLocation != null) {
                    LatLng position = mMyLocation.getPosition();
                    mMyLocation = null;
                    drawMyLocationMarker(position);
                }
                break;
            case R.id.button_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, SETTINGS_REQUEST);
                break;
            case R.id.button_list:
                Intent intent2 = new Intent(this, ListActivity.class);
                startActivityForResult(intent2, SELECT_FILE_REQUEST);
                break;
            case R.id.button_shutdown:
                // Show a confirmation dialog
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Sammutetaanko pii?")
                        .setMessage("Haluatko varmasti sammuttaa piin?")
                        .setPositiveButton("Kyllä", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MapsActivity.this, "Piin pitäisi sammua n. minuutin kuluttua", Toast.LENGTH_LONG).show();

                                // start an async task to request a shutdown
                                new AsyncTask<Void, Void, String>() {

                                    @Override
                                    protected String doInBackground(Void... params) {
                                        String message;

                                        try {
                                            URL url = new URL("http://" + mClient + "/cgi-bin/shutdown.cgibin");

                                            URLConnection conn = url.openConnection();
                                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                                                String line = reader.readLine();

                                                Log.d(TAG, "Shutdown response: " + line);

                                                message = line;
                                            }
                                        } catch (IOException e) {
                                            Log.d(TAG, e.getLocalizedMessage());
                                            message = e.getLocalizedMessage();
                                        }

                                        return message;
                                    }

                                    @Override
                                    protected void onPostExecute(String message) {
                                        // Show the message
                                        Toast.makeText(MapsActivity.this, message, Toast.LENGTH_LONG).show();
                                    }
                                }.execute();

                            }
                        })
                        .setNegativeButton("Ei", null)
                        .show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_FILE_REQUEST) {
            if (resultCode == RESULT_OK) {
                String file = data.getStringExtra("file");

                Log.d(TAG, "New file selected: " + file);

                if ("Latest".equalsIgnoreCase(file)) {
                    file = "";
                }

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                editor.putString("file", file);
                editor.commit();

                requestPoints();
            }
        } else if (requestCode == SETTINGS_REQUEST) {
            baseMapFromSettings();
        }
    }

    /**
     * This method sets the base map layer for the map
     */
    private void baseMapFromSettings() {
        String maptype = PreferenceManager.getDefaultSharedPreferences(this).getString("map_type", "Kartta");

        if ("Satelliitti".equals(maptype)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if ("Hybridi".equals(maptype)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if ("Maasto".equals(maptype)) {
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }
}
