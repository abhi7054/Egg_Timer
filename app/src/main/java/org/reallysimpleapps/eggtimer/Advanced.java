package org.reallysimpleapps.eggtimer;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Advanced extends AppCompatActivity implements android.location.LocationListener {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    LocationManager locationManager;
    String provider;
    String subLocality;
    TextView altitudeMetersTV;
    TextView altitudeFeetTV;
    TextView locationTV;
    Double altInFeet;
    int masterBoilTime;
    int baseTimeSeconds = 180;
    int eggSizeTimeSeconds = 20; // sets standard egg to medium
    int eggTempTimeSeconds = 60; // sets standard egg to fridge
    int eggAltitudeTime = 0; // sea level initially
    int eggHardMedSoftTimeSeconds = 120; // sets standard egg to medium
    TextView recommendedTimeDisplay; // in mins

    boolean accessLocationSorted = false;


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Called when the user is performing an action which requires the app to read the
    // user's contacts
    public void getPermissionToAccessLocation() {
        // 1) Use the support library version ContextCompat.checkSelfPermission(...) to avoid
        // checking the build version since Context.checkSelfPermission(...) is only available
        // in Marshmallow
        // 2) Always check for permission (even if permission has already been granted)
        // since the user can revoke permissions at any time through Settings
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // The permission is NOT already granted.
            // Check if the user has been asked about this permission already and denied
            // it. If so, we want to give more explanation about why the permission is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (shouldShowRequestPermissionRationale(
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Show our own UI to explain to the user why we need to read the contacts
                    // before actually requesting the permission and showing the default UI
                    Toast.makeText(this, "Give us your location to make the most of the Perfect Egg Timer", Toast.LENGTH_SHORT).show();
                }
            }

            // Fire off an async request to actually get the permission
            // This will show the standard permission request dialog UI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original FINE ACCESS request
        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Fine Access permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Allow location to use egg altitude in cooking time", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);

        altInFeet = Double.valueOf(0);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // something to do with using NoActionBar in styles, but will always be true here I think

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        altitudeMetersTV = (TextView) findViewById(R.id.altitudeMeters);
        altitudeFeetTV = (TextView) findViewById(R.id.altitudeFeet);
        locationTV = (TextView) findViewById(R.id.location);

        getPermissionToAccessLocation();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            getPermissionToAccessLocation();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            onLocationChanged(location);
        }


        recommendedTimeDisplay = (TextView) findViewById(R.id.recommendedTimeDisplay);
        updateRecommendedTime();

        final RadioGroup radioGroupSize = (RadioGroup) findViewById(R.id.radioEggSizeGroup);
        if (radioGroupSize != null) {
            radioGroupSize.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    // get index of radio button pressed
                    int index = radioGroupSize.indexOfChild(findViewById(radioGroupSize.getCheckedRadioButtonId()));
                    Log.i("AppInfo", String.valueOf(index));

                    if (index == 0) {
                        eggSizeTimeSeconds = 0; // small egg
                        updateRecommendedTime();
                    }


                    if (index == 1) {
                        eggSizeTimeSeconds = 20; // medium egg
                        updateRecommendedTime();
                    }


                    if (index == 2) {
                        eggSizeTimeSeconds = 40; // large egg
                        updateRecommendedTime();

                    }
                }
            });
        }


        final RadioGroup radioTempGroup = (RadioGroup) findViewById(R.id.radioEggTempGroup);
        if (radioTempGroup != null)

        {
            radioTempGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // checkedId is the RadioButton selected

                    // get index of radio button pressed
                    int index = radioTempGroup.indexOfChild(findViewById(radioTempGroup.getCheckedRadioButtonId()));
                    Log.i("AppInfo", String.valueOf(index));

                    if (index == 0) {
                        eggTempTimeSeconds = 0; // room temp
                        updateRecommendedTime();
                    }


                    if (index == 1) {
                        eggTempTimeSeconds = 45; // fridge temp
                        updateRecommendedTime();
                    }
                }
            });
        }


        final RadioGroup radioHardOrSoftGroup = (RadioGroup) findViewById(R.id.radioHardOrSoftGroup);
        if (radioHardOrSoftGroup != null)

        {
            radioHardOrSoftGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    // get index of radio button pressed
                    int index = radioHardOrSoftGroup.indexOfChild(findViewById(radioHardOrSoftGroup.getCheckedRadioButtonId()));
                    Log.i("AppInfo", String.valueOf(index));

                    if (index == 0) {
                        eggHardMedSoftTimeSeconds = 240; // hard boiled
                        updateRecommendedTime();
                    }


                    if (index == 1) {
                        eggHardMedSoftTimeSeconds = 120; // medium boiled
                        updateRecommendedTime();
                    }


                    if (index == 2) {
                        eggHardMedSoftTimeSeconds = 0; // soft boiled
                        updateRecommendedTime();
                    }
                }
            });
        }

    }

    // method to add up total masterBoilTime in seconds and display the result in mins and secs
    private void updateRecommendedTime() {

        // convert double altInFeet to altInFeetasInt
        Double d = new Double(altInFeet);
        int altInFeetasInt = d.intValue();
        eggAltitudeTime = altInFeetasInt / 10;


        masterBoilTime = baseTimeSeconds + eggSizeTimeSeconds + eggTempTimeSeconds + eggAltitudeTime + eggHardMedSoftTimeSeconds;


        // convert seconds to mins and secs.
        int minutes = (int) masterBoilTime / 60;
        int seconds = masterBoilTime - minutes * 60;
        String secondString = Integer.toString(seconds);
        if (seconds <= 9) {
            secondString = "0" + secondString;
        }

        recommendedTimeDisplay.setText(((Integer.toString(minutes) + ":" + secondString)));

    }


    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
        else {
            // TODO: Consider calling
            getPermissionToAccessLocation();
         
            return;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
            else {
            // TODO: Consider calling
            // getPermissionToAccessLocation();


            return;

        }

    }


    @Override
    public void onLocationChanged(Location location) {

        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        Double alt = location.getAltitude();
        Float bearing = location.getBearing();
        Float speed = location.getSpeed();
        Float accuracy = location.getAccuracy();

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            List<Address> listAddresses = geocoder.getFromLocation(lat, lng, 1);

            if (listAddresses != null && listAddresses.size() > 0) {

                Log.i("PlaceInfo", listAddresses.get(0).toString());

                String addressHolder = "";

                for (int i = 0; i <= listAddresses.get(0).getMaxAddressLineIndex(); i++) {

                    addressHolder += listAddresses.get(0).getAddressLine(i) + "\n";

                }

                // addressTV.setText("Address:\n" + addressHolder);

                subLocality = listAddresses.get(0).getSubLocality();

                if (subLocality == "") {
                    locationTV.setText("Your location");
                } else {
                    locationTV.setText(subLocality);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        altInFeet = alt * 3.28;
        String roundUpFeet = String.format("%.0f", altInFeet);

        String roundedUpMeters = String.format("%.0f", alt);

        altitudeFeetTV.setText(roundUpFeet + "ft  / ");
        altitudeMetersTV.setText(roundedUpMeters + "m");


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

    public void controlTimer(View v) {

        int masterBoilTimeSecondsInt = masterBoilTime;
        String masterBoilTimeSecondsString = String.valueOf(masterBoilTimeSecondsInt);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Advanced_Time", masterBoilTimeSecondsString);
        startActivity(intent);
    }

    public void explanationClicked(View view) {
        Log.i("AppInfo", "? Clicked");

        new AlertDialog.Builder(this)
                .setTitle("Perfect Egg Timer algorithm")
                .setMessage("REMEMBER: Timings are based on water at boiling point before placing egg in pan.\n\nWe help you boil your pefect egg by taking all relevant factors into account. We even measure your actual location altitude, because how high you are can alter boil times by well over a minute!  Try it. We're truly eggcited to hear your feedback :-) ")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_about) {
            Intent intent = new Intent(this, About.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }


}
