
package com.example.android.wifidirect.discovery;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;

import android.location.Location;           //bjs
import android.location.LocationListener;   //bjs;
import android.location.LocationManager;    //bjs
import android.os.Bundle;                   //bjs
//import android.widget.TextView;           //bjs
import android.widget.Toast;                //bjs


import com.example.android.wifidirect.discovery.WiFiChatFragment.MessageTarget;
import com.example.android.wifidirect.discovery.WiFiDirectServicesList.DeviceClickListener;
import com.example.android.wifidirect.discovery.WiFiDirectServicesList.WiFiDevicesAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main activity for the sample. This activity registers a local service and
 * perform discovery over Wi-Fi p2p network. It also hosts a couple of fragments
 * to manage chat operations. When the app is launched, the device publishes a
 * chat service and also tries to discover services published by other peers. On
 * selecting a peer published service, the app initiates a Wi-Fi P2P (Direct)
 * connection with the peer. On successful connection with a peer advertising
 * the same service, the app opens up sockets to initiate a chat.
 * {@code WiFiChatFragment} is then added to the the main activity which manages
 * the interface and messaging needs for a chat session.
 */
public class WiFiServiceDiscoveryActivity extends Activity implements
        DeviceClickListener, Handler.Callback, MessageTarget,
        ConnectionInfoListener {

    public static final String TAG = "wifidirectdemo";
    public static final String TAG1 = "WiFiServiceDiscoveryActivity";

    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    private WifiP2pManager manager;

    static final int SERVER_PORT = 4545;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private Handler handler = new Handler(this);
    private WiFiChatFragment chatFragment;
    private WiFiDirectServicesList servicesList;

    private TextView statusTxtView;

    private LocationManager lm;                         //bjs
    private Location myLocation;                        //bjs
    private LocationListener locationListener;          //bjs
    Location bestResult;                                //bjs
    TextView gpsData;       //bjs

    public Handler getHandler() {
        Log.d(TAG1,"getHandler()    (bjs)");    //bjs
        return handler;
    }

    public void setHandler(Handler handler) {
        Log.d(TAG1,"setHandler(Handler handler)    (bjs)");    //bjs
        this.handler = handler;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG1,"onCreate(Bundle savedInstanceState)    (bjs)");    //bjs
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        setContentView(R.layout.main);
        statusTxtView = (TextView) findViewById(R.id.status_text);
        //gpsData = (TextView)findViewById(R.id.textview);

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        startRegistrationAndDiscovery();

        servicesList = new WiFiDirectServicesList();
        getFragmentManager().beginTransaction()
                .add(R.id.container_root, servicesList, "services").commit();

//        getLastLocation();          // bjs
        getLocation();              // bjs
        //Gps_LocationFinderDemo gpsManager = new Gps_LocationFinderDemo();
        //gpsManager.setStrategicLocations();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG1,"onRestart()    (bjs)");    //bjs
        getLastLocation();          // bjs

        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null) {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG1,"onStop()    (bjs)");    //bjs
        if (manager != null && channel != null) {
            manager.removeGroup(channel, new ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }

            });
        }
        super.onStop();
    }

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery() {
        Log.d(TAG1,"startRegistrationAndDiscovery()    (bjs)");    //bjs
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, service, new ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                appendStatus("Failed to add a service");
            }
        });

        discoverService();

    }

    private void discoverService() {
        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        Log.d(TAG1,"discoverService()    (bjs)");    //bjs
        manager.setDnsSdResponseListeners(channel,
                new DnsSdServiceResponseListener() {

                    @Override
                    public void onDnsSdServiceAvailable(String instanceName,
                            String registrationType, WifiP2pDevice srcDevice) {

                        // A service has been discovered. Is this our app?

                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {

                            // update the UI and add the item the discovered
                            // device.
                            WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
                                    .findFragmentByTag("services");
                            if (fragment != null) {
                                WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
                                        .getListAdapter());
                                WiFiP2pService service = new WiFiP2pService();
                                service.device = srcDevice;
                                service.instanceName = instanceName;
                                service.serviceRegistrationType = registrationType;
                                adapter.add(service);
                                adapter.notifyDataSetChanged();
                                Log.d(TAG, "onBonjourServiceAvailable "
                                        + instanceName);
                            }
                        }

                    }
                }, new DnsSdTxtRecordListener() {

                    /**
                     * A new TXT record is available. Pick up the advertised
                     * buddy name.
                     */
                    @Override
                    public void onDnsSdTxtRecordAvailable(
                            String fullDomainName, Map<String, String> record,
                            WifiP2pDevice device) {
                        Log.d(TAG,
                                device.deviceName + " is "
                                        + record.get(TXTRECORD_PROP_AVAILABLE));
                    }
                });

        // After attaching listeners, create a service request and initiate
        // discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest,
                new ActionListener() {

                    @Override
                    public void onSuccess() {
                        appendStatus("Added service discovery request");
                    }

                    @Override
                    public void onFailure(int arg0) {
                        appendStatus("Failed adding service discovery request");
                    }
                });
        manager.discoverServices(channel, new ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                appendStatus("Service discovery failed");

            }
        });
    }

    @Override
    public void connectP2p(WiFiP2pService service) {
        Log.d(TAG1,"connectP2p(WiFiP2pService service)    (bjs)");    //bjs
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = service.device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (serviceRequest != null)
            manager.removeServiceRequest(channel, serviceRequest,
                    new ActionListener() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFailure(int arg0) {
                        }
                    });

        manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Connecting to service");
            }

            @Override
            public void onFailure(int errorCode) {
                appendStatus("Failed connecting to service");
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG1,"handleMessage(Message msg)    (bjs)");    //bjs
        //TODO Parse GPS coordinates from sender's message - bjs
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                (chatFragment).pushMessage("Buddy: " + readMessage);
                break;

            case MY_HANDLE:     //called from ChatManager line 41
                Object obj = msg.obj;
                (chatFragment).setChatManager((ChatManager) obj);  // I think this is the UI
        }
        return true;
    }

    @Override
    public void onResume() {
        Log.d(TAG1,"onResume()    (bjs)");    //bjs
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

        getLastLocation();          // bjs
    }

    @Override
    public void onPause() {
        Log.d(TAG1,"onPause()    (bjs)");    //bjs
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        Thread handler = null;
        /*
         * The group owner accepts connections using a server socket and then spawns a
         * client socket for every client. This is handled by {@code
         * GroupOwnerSocketHandler}
         */

        Log.d(TAG1,"onConnectionInfoAvailable(WifiP2pInfo p2pInfo)    (bjs)");    //bjs
        if (p2pInfo.isGroupOwner) {
            Log.d(TAG, "Connected as group owner");
            try {
                handler = new GroupOwnerSocketHandler(
                        ((MessageTarget) this).getHandler());
                handler.start();
            } catch (IOException e) {
                Log.d(TAG,
                        "Failed to create a server thread - " + e.getMessage());
                return;
            }
        } else {
            Log.d(TAG, "Connected as peer");
            handler = new ClientSocketHandler(
                    ((MessageTarget) this).getHandler(),
                    p2pInfo.groupOwnerAddress);
            handler.start();
        }
        chatFragment = new WiFiChatFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.container_root, chatFragment).commit();
        statusTxtView.setVisibility(View.GONE);
    }

    public void appendStatus(String status) {
        Log.d(TAG1,"appendStatus(String status)    (bjs)");    //bjs
        String current = statusTxtView.getText().toString();
        statusTxtView.setText(current + "\n" + status);
    }

    public void getLocation(){

        Boolean gps_enabled = false;
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);   // gps_enabled always false on LG since no SIM card.
        if(gps_enabled){

            // Listener that responds to location updates
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    myLocation = location;
                    statusTxtView.setText("Source Node - lat: "+myLocation.getLatitude()+" longitude: "+myLocation.getLongitude());
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Toast.makeText(getBaseContext(), "Out Of Service", Toast.LENGTH_LONG).show();
                }

                public void onProviderEnabled(String provider) {
                    Toast.makeText(getBaseContext(), "Provider enable", Toast.LENGTH_LONG).show();
                }

                public void onProviderDisabled(String provider) {
                    Toast.makeText(getBaseContext(), "Provider disable", Toast.LENGTH_LONG).show();
                }
            };

            // Register the listener with the Location Manager to receive location updates
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            getLastLocation();
            Log.d(TAG,"myLocation.getLatitude" + myLocation.getLatitude() + "    (bjs)");    //bjs
        }
        else{
//            gpsData.setText("GPS is Disable");
            Log.d(TAG,"GPS is disabled!   (bjs)");    //bjs

//            return;
        }
    }



    private void getLastLocation() {
        long minTime = 9999990;
        long bestTime = 99999990;

        float bestAccuracy = 999999;

        List<String> matchingProviders = lm.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = lm.getLastKnownLocation(provider);
            if (location != null) {
                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > minTime && accuracy < bestAccuracy)) {
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                    Log.d(TAG1,"getLastLocation(): bestAccuracy " + bestAccuracy + "; bestTime " +
                            bestTime + "        (bjs)");    //bjs
                } else if (time < minTime &&
                    bestAccuracy == Float.MAX_VALUE && time > bestTime) {
                    bestResult = location;
                    bestTime = time;
                    Log.d(TAG1,"getLastLocation(): bestAccuracy " + bestAccuracy + "; bestTime " +
                            bestTime + "        (bjs)");    //bjs
                }
            }
        }
        //myLocation.set(bestResult);
        if(bestResult == null) {
            bestResult.reset();
            return;
        } else {
            myLocation = bestResult;
            statusTxtView.setText("Source Node - lat: "+myLocation.getLatitude()+" longitude: "+myLocation.getLongitude());
        }

    }





}
