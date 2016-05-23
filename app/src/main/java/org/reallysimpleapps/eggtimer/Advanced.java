package org.reallysimpleapps.eggtimer;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.Serializable;
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

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    String lat, lng;
    TextView txtOutputLat, txtOutputLon;

    // Dummy check to see if we have ACCESS_FINE_LOCATION Permission on SDK23+
    @TargetApi(Build.VERSION_CODES.M)
    private void insertDummyContactWrapper() {
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("Please allow the Perfect Egg Timer access to location to know your egg cooking altitude",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS},
                    REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }
        // insertDummyContact();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // No matter Allow or Deny is chosen, Activity's onRequestPermissionsResult will always be called to inform a result which we can check from the 3rd parameter, grantResults
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    // insertDummyContact();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Location access denied meaning a slightly less accurate boil time prediction", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // something to do with using NoActionBar in styles, but will always be true here I think


        altitudeMetersTV = (TextView) findViewById(R.id.altitudeMeters);
        altitudeFeetTV = (TextView) findViewById(R.id.altitudeFeet);
        locationTV = (TextView) findViewById(R.id.location);


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            insertDummyContactWrapper();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        onLocationChanged(location);


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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            insertDummyContactWrapper();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            insertDummyContactWrapper();
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 400, 1, this);

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
                .setMessage("We help you boil your pefect egg by taking all relevant factors into account. We even measure your actual location altitude, because how high you are can alter boil times by well over a minute!  Try it. We're truly eggcited to hear your feedback :-) ")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
//                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        // do nothing
//                    }
//                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
}
