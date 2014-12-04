package com.example.android.wifidirect.discovery;


import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
//import android.widget.TextView;
import android.widget.Toast;
import java.lang.Math;


public class Gps_LocationFinderDemo extends Activity {

    LocationManager lm;
    Location myLocation;
    Location sourceL1 = new Location("");		// 90 degrees from Source Node
    Location sourceL2 = new Location("");		// 330 degree from Source Node
    Location sourceL3 = new Location("");		// 210 degree from Source Node
    double broadcastDistance = 30;    	// Radius of the regular hexagon
    double latitudeDistance = 111000;	// Apprx distance (meters) of one degree latitude
    double longitudeDistance = 98068;	// Apprx distance (meters) of one degree longitude at Earth's equator (111320 meters at equator)
    LocationListener locationListener;
//    TextView gpsData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //      setContentView(R.layout.gps_demo);
        //       gpsData = (TextView)findViewById(R.id.textview);

        Boolean gps_enabled = false;
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(gps_enabled){

            // Listener that responds to location updates
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    makeUseOfNewLocation();
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
        }
        else{
//            gpsData.setText("GPS is Disable");
            return;
        }
    }

    // Displays coordinates of Source and strategic locations for testing
    public void makeUseOfNewLocation() {
        //myLocation = location;
        setStrategicLocations();
        /*gpsData.setText("Source Node - lat: "+myLocation.getLatitude()+" longitude: "+myLocation.getLongitude()
        		+ "\r\nStrategic L1 (90 deg) - lat: " + sourceL1.getLatitude() + " long:" + sourceL1.getLongitude()
        		+ "\r\nStrategic L2 (210 deg) - lat: " + sourceL2.getLatitude() + " long:" + sourceL2.getLongitude()
        		+ "\r\nStrategic L3 (330 deg) - lat: " + sourceL3.getLatitude() + " long:" + sourceL3.getLongitude()
        		+ "\r\nBroadcast Distance: " + broadcastDistance
        		) ;      
		*/
    }

    // Calculates the strategic coordinates in relation to source node.
    public void setStrategicLocations() {
        //double currentLongitudeDistance = Math.abs(Math.cos(myLocation.getLatitude())*longitudeDistance);

        // Strategic node 90 degrees from Source Node
        sourceL1.setLatitude(myLocation.getLatitude() + broadcastDistance/latitudeDistance);
        sourceL1.setLongitude(myLocation.getLongitude());

        // Strategic node 210 degrees from Source Node
        sourceL2.setLongitude(myLocation.getLongitude() - broadcastDistance*Math.cos(210)/longitudeDistance);
        sourceL2.setLatitude(myLocation.getLatitude() + broadcastDistance*Math.sin(210)/latitudeDistance);

        // Strategic node 330 degrees from Source Node
        sourceL3.setLongitude(myLocation.getLongitude() + broadcastDistance*Math.cos(330)/longitudeDistance);
        sourceL3.setLatitude(myLocation.getLatitude() + broadcastDistance*Math.sin(330)/latitudeDistance);

    }
}
