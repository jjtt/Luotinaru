package fi.torma.luotinaru;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This AsyncTask gets a list of nmea files for the ListActivity
 */
public class FilesTask extends AsyncTask<Void, Void, List<Map<String, String>>> {

    public static final String TAG = "FilesTask";

    private final Context mContext;
    private final ArrayAdapter<Map<String, String>> mAdapter;
    private final SharedPreferences mSharedPreferences;
    private String mErrorMessage = null;

    public FilesTask(Context context, ArrayAdapter<Map<String, String>> adapter) {
        mContext = context;
        mAdapter = adapter;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {

        List<Map<String, String>> files = new LinkedList<>();

        // add a row for the latest data as the first row
        Map<String, String> m = new HashMap<>();
        m.put("title", "Latest");
        m.put("description", "");
        files.add(m);

        String addr = mSharedPreferences.getString("client_ip", null);

        try {
            URL url = new URL("http://" + addr + "/cgi-bin/index.py?plain=1");

            Log.d(TAG, url.toString());

            URLConnection conn = url.openConnection();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        Log.d(TAG, "Empty line in index file skipped");
                        continue;
                    }

                    String[] split = line.split(",");

                    m = new HashMap<>();

                    m.put("title", split[0]);
                    m.put("description", split[1] + ", " + split[2]);

                    files.add(m);
                }

            }

        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
            mErrorMessage = e.getLocalizedMessage();
        }

        Log.d(TAG, "Returning a list of " + files.size() + " files");
        return files;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> files) {
        Log.d(TAG, "Processing the list");

        if (mErrorMessage != null) {
            Log.d(TAG, "Errors in retrieving the files: " + mErrorMessage);
            Toast.makeText(mContext, mErrorMessage, Toast.LENGTH_LONG).show();
        }

        if (files.isEmpty()) {
            Log.d(TAG, "No files");
            Toast.makeText(mContext, "No files", Toast.LENGTH_LONG).show();
            return;
        }

        mAdapter.clear();
        mAdapter.addAll(files);
        mAdapter.notifyDataSetChanged();
    }

}
