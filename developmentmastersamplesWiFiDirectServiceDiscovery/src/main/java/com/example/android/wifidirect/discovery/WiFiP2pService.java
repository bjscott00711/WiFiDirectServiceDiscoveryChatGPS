
package com.example.android.wifidirect.discovery;

/* Adapted from the example at https://android.googlesource.com/platform/development/+
    /master/samples/WiFiDirectServiceDiscovery/src/com/example/android/wifidirect/
    discovery/WiFiServiceDiscoveryActivity.java
*/

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
//Log.d(TAG,"    (bjs)");    //bjs
//private static final String TAG = "WiFiDirectBroadcastReceiver";   //bjs

/**
 * A structure to hold service information.
 */
public class WiFiP2pService {
    WifiP2pDevice device;
    String instanceName = null;
    String serviceRegistrationType = null;
}
