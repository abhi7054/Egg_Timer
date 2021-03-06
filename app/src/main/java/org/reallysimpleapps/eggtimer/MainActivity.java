package org.reallysimpleapps.eggtimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
//
//import com.google.android.gms.appinvite.AppInvite;
//import com.google.android.gms.appinvite.AppInviteInvitation;
//import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    SeekBar timerSeekBar;
    TextView timerTextView;
    TextView setDefaultTime;
    Button controllerBtn;
    TextView advancedBtn;
    ImageView inviteFriends;
    Boolean counterIsActive = false;
    Boolean controlBtnIsAgain = false;
    CountDownTimer countDownTimer;
    ImageView egg;
    Vibrator vibrator;
    private static final int REQUEST_INVITE = 0;
    int defaultTime;
    Button mins3;
    Button mins4;
    Button mins5;
    Button mins6;
    Button mins7;
    Button mins8;
    Button mins10;
    Button mins12;
    Button mins15;
    Button mins20;
    int timesUsed;



    public void resetTimer() {

        // start by pulling timeInSeconds from SharedPreferences file
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        int secondsLeft = sharedPreferences.getInt("timeInSeconds", 240);

        // convert seconds to mins and secs.
        int minutes = (int) secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        String secondString = Integer.toString(seconds);
        if (seconds <= 9) {
            secondString = "0" + secondString;
        }

        setDefaultTime.setText("Make this time my default");

        timerTextView.setText(((Integer.toString(minutes) + ":" + secondString)));
        timerSeekBar.setProgress(secondsLeft);
        countDownTimer.cancel();
        controllerBtn.setText(R.string.Go);
        setTimerButtonsVisible();


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        timerSeekBar.setEnabled(true);
        counterIsActive = false;
    }

    private void setTimerButtonsVisible() {

        mins3.setVisibility(View.VISIBLE);
        mins4.setVisibility(View.VISIBLE);
        mins5.setVisibility(View.VISIBLE);
        mins6.setVisibility(View.VISIBLE);
        mins7.setVisibility(View.VISIBLE);
        mins8.setVisibility(View.VISIBLE);
        mins10.setVisibility(View.VISIBLE);
        mins12.setVisibility(View.VISIBLE);
        mins15.setVisibility(View.VISIBLE);
        mins20.setVisibility(View.VISIBLE);

        advancedBtn.setVisibility(View.VISIBLE);
    }

    public void awaitRunAgain() {
        timerTextView.setText("0:00");
        timerSeekBar.setProgress(0);
        countDownTimer.cancel();
        controllerBtn.setText(R.string.Again);
        controlBtnIsAgain = true;
        timerSeekBar.setEnabled(true);
        counterIsActive = false;


        // Find out how many times app has run by checking SharedPreferences file
        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        timesUsed = sharedPreferences.getInt("numberOfUses", 0);
        timesUsed++; // add 1 to times used and store new figure in SharedPreferences

        // Store another use of timer to Shared Preferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("numberOfUses", timesUsed);
        editor.commit();

        if (timesUsed % 5 == 0) {
            // Display invite friends App Invite button
            inviteFriends.setVisibility(View.VISIBLE);
        } else {
            // Potentially request a Google Play rating, though will depend on AppRater settings
            new AppRater(this).show();
        }


    }

    public void updateTimer(int secondsLeft) {

        int minutes = (int) secondsLeft / 60;
        int seconds = secondsLeft - minutes * 60;
        String secondString = Integer.toString(seconds);
        if (seconds <= 9) {
            secondString = "0" + secondString;
        }
        timerTextView.setText(Integer.toString(minutes) + ":" + secondString);
        defaultTime = (minutes * 60) + seconds;
    }


    // controls what happens when the single control button is clicked
    public void controlTimer(View view) {

        if (controlBtnIsAgain) {
            controlBtnIsAgain = false;
            egg.setImageResource(R.drawable.egg1);
            inviteFriends.setVisibility(View.INVISIBLE);
            setTimerButtonsVisible();


            resetTimer();

        } else if (counterIsActive == false) {
            counterIsActive = true;
            timerSeekBar.setEnabled(false); // prevents user seeking whilst counter active
            controllerBtn.setText(R.string.Stop);

            // prevent set timer buttons being clicked whilst countdown running

            mins3.setVisibility(View.INVISIBLE);
            mins4.setVisibility(View.INVISIBLE);
            mins5.setVisibility(View.INVISIBLE);
            mins6.setVisibility(View.INVISIBLE);
            mins7.setVisibility(View.INVISIBLE);
            mins8.setVisibility(View.INVISIBLE);
            mins10.setVisibility(View.INVISIBLE);
            mins12.setVisibility(View.INVISIBLE);
            mins15.setVisibility(View.INVISIBLE);
            mins20.setVisibility(View.INVISIBLE);


            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.startclick); // start sound
            mediaPlayer.start();

            long[] pattern = {0, 150}; // short vibrate pattern setup for starting timer
            vibrator.vibrate(pattern, -1); // start vibrate
            setDefaultTime.setText("");

            egg.setImageResource(R.drawable.egg1); // revert to original uncooked egg image

            countDownTimer = new CountDownTimer(timerSeekBar.getProgress() * 1000 + 100, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimer((int) millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    timerTextView.setText("0:00");
                    egg.setImageResource(R.drawable.egg2);
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.rooster);
                    mediaPlayer.start();

                    createNotification();

                    long[] pattern = {0, 400, 600, 400, 600, 400}; // vibrate pattern setup
                    vibrator.vibrate(pattern, -1); // vibrate pattern

                    awaitRunAgain();
                }
            }.start();
        } else {
            resetTimer(); // calls reset timer method
        }
    }

    public void createNotification() {
        // Set up notifications
        Intent intent = new Intent(getApplicationContext(), EggPhoto.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, intent, 0);


        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.Egg_Timer_Alert))
                .setContentText(getString(R.string.Your_egg_is_boiled_and_ready_to_enjoy))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        // Display notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mins3 = (Button) findViewById(R.id.min3);
        mins4 = (Button) findViewById(R.id.min4);
        mins5 = (Button) findViewById(R.id.min5);
        mins6 = (Button) findViewById(R.id.min6);
        mins7 = (Button) findViewById(R.id.min7);
        mins8 = (Button) findViewById(R.id.min8);
        mins10 = (Button) findViewById(R.id.min10);
        mins12 = (Button) findViewById(R.id.min12);
        mins15 = (Button) findViewById(R.id.min15);
        mins20 = (Button) findViewById(R.id.min20);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setDefaultTime = (TextView) findViewById(R.id.selectFromTheFollowing);
        advancedBtn = (TextView) findViewById(R.id.advancedBtn);

        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.ic_launcher);


//        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
////                .addApi(AppInvite.API)
//                .enableAutoManage(this, this)
//                .build();

        // Check for App Invite invitations and launch deep-link activity if possible.
        // Requires that an Activity is registered in AndroidManifest.xml to handle
        // deep-link URLs.
        /*boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d("TAG", "getInvitation:onResult:" + result.getStatus());
                                // Because autoLaunchDeepLink = true we don't have to do anything
                                // here, but we could set that to false and manually choose
                                // an Activity to launch to handle the deep link here.
                            }
                        });*/


        //check whether vibrator exists on running device and log
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator == null) {
            Log.i("Vibrator", "No vibrator service exists");
        } else {
            Log.i("Vibrator", "Good news, vibrator exits");

            timerSeekBar = (SeekBar) findViewById(R.id.timerSeekBar);
            timerTextView = (TextView) findViewById(R.id.timerTextView);
            controllerBtn = (Button) findViewById(R.id.controllerBtn);
            egg = (ImageView) findViewById(R.id.imageViewEgg);
            inviteFriends = (ImageView) findViewById(R.id.inviteFriends);

            // retrieve saved default time if available
            SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
            int timeCheck = sharedPreferences.getInt("timeInSeconds", -1); // if no previous file set to -1 and use 4mins
            timerSeekBar.setMax(1200);

            if (timeCheck == -1) { // no default time has been saved yet
                timerSeekBar.setProgress(240);
                timerTextView.setText("4:00");


            } else // there is a saved default time so use it
            {
                timerSeekBar.setProgress(timeCheck);

                // convert seconds to mins and secs.
                int minutes = timeCheck / 60;
                int seconds = timeCheck - minutes * 60;
                String secondString = Integer.toString(seconds);
                if (seconds <= 9) {
                    secondString = "0" + secondString;
                }
                timerTextView.setText(((Integer.toString(minutes) + ":" + secondString)));

            }

        }


        timerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateTimer(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // grab any time sent from Advanced.class
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            String recommendedBoilTimeSecondsString = (String) bundle.get("Advanced_Time");
            Log.i("AppInfo", "recommendedBoilTimeSecondsString in MainActivity: " + recommendedBoilTimeSecondsString);

            int recommendedBoilTimeSecondsInt = 0;

            try {
                recommendedBoilTimeSecondsInt = Integer.parseInt(recommendedBoilTimeSecondsString);
                Log.i("AppInfo", "recommendedBoilTimeSecondsInt: " + recommendedBoilTimeSecondsInt);
                setTime(recommendedBoilTimeSecondsInt);

                runWhenAdvancedAccepted();

            } catch (NumberFormatException nfe) {
                // Handle parse error.
            }
        }


    }


    /*public void onInviteClicked(View v) {

        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }*/


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("TAG", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent and log a message
                // The ids array contains the unique invitation ids for each invitation sent
                // (one for each contact select by the user). You can use these for analytics
                // as the ID will be consistent on the sending and receiving devices.
//                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
//                inviteFriends.setVisibility(View.INVISIBLE);
//
//                Log.i("TAG", getString(R.string.sent_invitations_fmt, ids.length));


            } else {
                // Sending failed or it was canceled, show failure message to the user
                Toast.makeText(this, getString(R.string.send_failed), Toast.LENGTH_LONG).show();
                Log.i("TAG", "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);
            }
        }
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


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void makeDefaultTime(View view) {

        SharedPreferences sharedPreferences = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("timeInSeconds", defaultTime);
        editor.commit();

        setDefaultTime.setText("");

        // convert seconds to mins and secs.
        int minutes = (int) defaultTime / 60;
        int seconds = defaultTime - minutes * 60;
        String secondString = Integer.toString(seconds);
        if (seconds <= 9) {
            secondString = "0" + secondString;
        }


        Toast.makeText(this, "New default time saved as " + (((Integer.toString(minutes) + ":" + secondString))), Toast.LENGTH_SHORT).show();

        long[] pattern = {0, 150}; // short vibrate pattern for setting default time
        vibrator.vibrate(pattern, -1); // start vibrate

    }


    @Override
    public void onClick(View v) {

        if (!counterIsActive) {

            switch (v.getId()) {
                case R.id.min3:
                    setTime(180);
                    break;

                case R.id.min4:
                    setTime(240);
                    break;

                case R.id.min5:
                    setTime(300);
                    break;

                case R.id.min6:
                    setTime(360);
                    break;

                case R.id.min7:
                    setTime(420);
                    break;

                case R.id.min8:
                    setTime(480);
                    break;

                case R.id.min10:
                    setTime(600);
                    break;

                case R.id.min12:
                    setTime(720);
                    break;

                case R.id.min15:
                    setTime(900);
                    break;

                case R.id.min20:
                    setTime(1200);
                    break;
            }
        }
    }

    public void setTime(int secondsToSet) {

        int timeCheck = secondsToSet;
        timerSeekBar.setProgress(timeCheck);
        // convert seconds to mins and secs.
        int minutes = timeCheck / 60;
        int seconds = timeCheck - minutes * 60;
        String secondString = Integer.toString(seconds);
        if (seconds <= 9) {
            secondString = "0" + secondString;
        }
        timerTextView.setText(((Integer.toString(minutes) + ":" + secondString)));

        long[] pattern = {0, 50}; // short vibrate pattern for setting default time
        vibrator.vibrate(pattern, -1); // start vibrate
    }


    public void goAdvanced(View view) {
        Intent intent = new Intent(this, Advanced.class);
        startActivity(intent);
    }


    public void runWhenAdvancedAccepted() {

            counterIsActive = true;
            timerSeekBar.setEnabled(false); // prevents user seeking whilst counter active
            controllerBtn.setText(R.string.Stop);

            // prevent set timer buttons being clicked whilst countdown running
            mins3.setVisibility(View.INVISIBLE);
            mins4.setVisibility(View.INVISIBLE);
            mins5.setVisibility(View.INVISIBLE);
            mins6.setVisibility(View.INVISIBLE);
            mins7.setVisibility(View.INVISIBLE);
            mins8.setVisibility(View.INVISIBLE);
            mins10.setVisibility(View.INVISIBLE);
            mins12.setVisibility(View.INVISIBLE);
            mins15.setVisibility(View.INVISIBLE);
            mins20.setVisibility(View.INVISIBLE);

            // Also prevent Advanced button being clicked whilst coundown running
            advancedBtn.setVisibility(View.INVISIBLE);



            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.startclick); // start sound
            mediaPlayer.start();

            long[] pattern = {0, 150}; // short vibrate pattern setup for starting timer
            vibrator.vibrate(pattern, -1); // start vibrate
            setDefaultTime.setText("");

            egg.setImageResource(R.drawable.egg1); // revert to original uncooked egg image

            countDownTimer = new CountDownTimer(timerSeekBar.getProgress() * 1000 + 100, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    updateTimer((int) millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    timerTextView.setText("0:00");
                    egg.setImageResource(R.drawable.egg2);
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.rooster);
                    mediaPlayer.start();

                    createNotification();

                    long[] pattern = {0, 400, 600, 400, 600, 400}; // vibrate pattern setup
                    vibrator.vibrate(pattern, -1); // vibrate pattern

                    awaitRunAgain();
                }
            }.start();

    }



}
