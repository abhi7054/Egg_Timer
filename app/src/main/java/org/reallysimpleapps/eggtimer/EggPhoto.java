package org.reallysimpleapps.eggtimer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class EggPhoto extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    Button photoButton;
    private File imageFile;
    static final int CAM_REQUEST = 1; // quality of pic, but unfortunately either 0 for MMS quality or 1 for high quality

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egg_photo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // something to do with using NoActionBar in styles, but will always be true here I think


        this.imageView = (ImageView) this.findViewById(R.id.imageView1);
        photoButton = (Button) this.findViewById(R.id.button1);
    }


    public void takeYourSnap(View v) {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "egg.jpg");

        Uri tempuri = Uri.fromFile(imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempuri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, CAM_REQUEST);
        startActivityForResult(intent, 0);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (imageFile.exists()) {
                        Toast.makeText(getApplicationContext(), "Social media your #PerfectEgg", Toast.LENGTH_LONG).show();

                        Uri uri = null;
                        File picture = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                        uri = Uri.parse("file://" + picture.toString() + "/" + "egg.jpg");

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        shareIntent.setType("image/*");
                        startActivity(Intent.createChooser(shareIntent, "Share Image"));

                    } else {
                        Toast.makeText(getApplicationContext(), "Soz, error saving your egg pic", Toast.LENGTH_LONG).show();
                    }

                    break;
                case Activity.RESULT_CANCELED:
                    break;
                default:
                    break;
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


}
