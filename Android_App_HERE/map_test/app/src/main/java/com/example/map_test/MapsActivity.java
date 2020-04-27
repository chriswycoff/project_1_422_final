
// import statements start here
package com.example.map_test;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

// import statements end here

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference databasereference;
    private LocationListener locationLister;
    private LocationManager locationManager;
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;

    private EditText editTextLatitude;
    private EditText editTextLongitude;



    // unique ID code credit for idea Nick Henderson, adapted and debugged by Chris Wycoff
    public String getUUID() {

        // essentially this gets a unique user ID from the hardware of the users phone
        String id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Use UTF 8 for the id
        UUID id_UUID = UUID.nameUUIDFromBytes(id.getBytes(Charset.forName("UTF-8")));

        return(id_UUID.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this calls the original onCreate logic as well as ours
        super.onCreate(savedInstanceState);
        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        setContentView(R.layout.activity_maps);

        String the_id = getUUID();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // get permission to access the phones geolocation services, both fine and course grain location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                PackageManager.PERMISSION_GRANTED);

        editTextLatitude = findViewById(R.id.editText);
        editTextLongitude = findViewById(R.id.editText2);

        editTextLongitude.setClickable(false);
        editTextLatitude.setClickable(false);

        editTextLongitude.setFocusable(false);
        editTextLatitude.setFocusable(false);

        databasereference = FirebaseDatabase.getInstance().getReference(the_id);
        databasereference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // try to do the following when there is a change in Latitude / Longitude
                try{

                    LatLng the_location = new LatLng(Double.parseDouble(editTextLatitude.getText().toString()),Double.parseDouble(editTextLongitude.getText().toString()));
                    mMap.addMarker(new MarkerOptions().position(the_location).title("You were here"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(the_location));

                }
                catch (Exception e){
                    e.printStackTrace();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // final keyword make sure that when you navigate back to the main map activity...
        // that this handler only gets created on the first time, similar to static keyword...
        // main deference is that final is local to this area of the source code
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {

                    //This allows the app to do something after X seconds
                    // will allow the posting of location data while navigating other...
                    // ... activities within the app

                    // getting current time and date
                    Date currentTime = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                    String timeString = dateFormat.format(currentTime);

                    // posting to database
                    databasereference.child(timeString).child("latitude").setValue(editTextLatitude.getText().toString());
                    databasereference.child(timeString).child("longitude").setValue(editTextLongitude.getText().toString());

                    LatLng the_location = new LatLng(Double.parseDouble(editTextLatitude.getText().toString()), Double.parseDouble(editTextLongitude.getText().toString()));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(the_location));

                    // this runnable uses android OS threads to operate in the background
                    // this determines the delay between post
                }
                catch (Exception e){
                    e.printStackTrace();

                }
                handler.postDelayed(this, 300000); //the time is in milliseconds...
                // between each post (lat/long) to database. In this case 300,000 milliseconds ...
                // ... or 5 minutes between posts.
            }
        }, 5000);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("TAG", "getting device location");


        locationLister = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    // changes the lat/long text boxes
                    String lat = String.format(Locale.US,"%.5f",location.getLatitude());
                    String longt = String.format(Locale.US,"%.5f",location.getLongitude());
                    editTextLatitude.setText(lat);
                    editTextLongitude.setText(longt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // could add logic here to request the missing permissions, and then overriding
            // public void onRequestPermissionsResult(int requestCode, String[] permissions,...
            // ... int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationLister);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationLister);

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public void updateButtonOneOnClick(View view){

        try{
            Date currentTime = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            String timeString = dateFormat.format(currentTime);


            // for debugging the following will ping the database
            //databasereference.child(timeString).child("latitude").setValue(editTextLatitude.getText().toString());
            //databasereference.child(timeString).child("longitude").setValue(editTextLongitude.getText().toString());


            LatLng the_location = new LatLng(Double.parseDouble(editTextLatitude.getText().toString()),Double.parseDouble(editTextLongitude.getText().toString()));
            mMap.addMarker(new MarkerOptions().position(the_location).title("You were here"));
            float zoomLevel = 16.0f;
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(the_location));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(the_location, zoomLevel));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Button to get navigate to Facts.java
    public void updateButtonTwoOnClick(View view){

        try{
           Intent change_intent = new Intent(MapsActivity.this, Facts.class);
           MapsActivity.this.startActivity(change_intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


} // end of on OnMapReadyCallback
