package fi.torma.luotinaru;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * This class parses the /proc/net/arp file to find a client with the specified
 * HW address
 */
public class WlanClientFinder {

    public static final String TAG = "WlanClientFinder";

    private final String mAddr;

    public WlanClientFinder(String mAddr) {
        this.mAddr = mAddr.toLowerCase();
    }

    /**
     * Find the client and return ip
     *
     * also update the found ip address to the shared preferences
     *
     * @return
     * @param defaultSharedPreferences
     */
    public String find(SharedPreferences defaultSharedPreferences) {

        String addr = null;

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/net/arp"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                Log.d(TAG, line);

                String[] split = line.split(" +");

                if ((split != null) && (split.length >= 4)) {
                    String mac = split[3];

                    Log.d(TAG, mac);

                    if (mAddr.equals(mac.toLowerCase())) {
                        Log.d(TAG, "Found!: " + split[0]);
                        addr = split[0];
                        break;
                    }
                }
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        SharedPreferences.Editor editor = defaultSharedPreferences.edit();
        editor.putString("client_ip", String.valueOf(addr));
        editor.commit();

        return addr;
    }
}
