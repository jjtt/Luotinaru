package fi.torma.luotinaru;

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
     * @return
     */
    public String find() {

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
                        return split[0];
                    }
                }
            }


        } catch (Exception ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }

        return null;
    }
}
