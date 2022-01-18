package com.example.covid19app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    DatabaseHelper db_main;
    private CameraManager cameraManager;
    private String mCameraId;
    private boolean status = false;
    Context context = this;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private Camera mCamera;
    private Uri videoUri;;
    private SensorManager sensorManager;
    Sensor mAccelerometer;
    float accelValuesZ[] = new float[128];
    int index = 0;
    SensorHandler sensorHandler;
    int respiratory_rate = 0;
    int heart_rate = 0;

    MediaMetadataRetriever retriever = new MediaMetadataRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button hrate = (Button) findViewById(R.id.button4);
        Button symptoms_button = (Button) findViewById(R.id.symptoms);
        Button resp_rate = (Button) findViewById(R.id.resp_rate);
        Button upload_signs = (Button) findViewById(R.id.upload_signs);

        db_main = new DatabaseHelper(MainActivity.this);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        try {
            mCameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.v("test debug", e.getMessage());
        }

        hrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCamera(Integer.parseInt(mCameraId));
                try {
                    startRecordingVideo();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        symptoms_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent (getApplicationContext(), Symptoms_activity.class);
                startActivity(intent);
            }
        });

        resp_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(MainActivity.this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });
        upload_signs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db_main.insert_hrate_resprate(heart_rate, respiratory_rate);
                Toast.makeText(MainActivity.this, "Heart Rate and Respiratory rate has been inserted.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void startRecordingVideo()  throws InterruptedException {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            File mediaFile = new File(getExternalFilesDir(null).toString() + "/FingertipVideo.mp4");
            Log.d("main class", "mediaFile = "+mediaFile);
            videoUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.covid19app", mediaFile);
            Log.d("main class", "videouri = "+videoUri);
            startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            Log.d("main class", "uri = "+uri);
            retriever.setDataSource(getApplicationContext(), uri);

            Bitmap bitmap;

            int j = 0;
            MediaPlayer mediaPlayer = MediaPlayer.create(this, uri);
            int millis = mediaPlayer.getDuration();
            float[] red_avg = new float[millis];
            for(int i = 1000000; i < millis*1000; i+=100000) {
                bitmap = retriever.getFrameAtTime((long) i, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                float r = 0;
                try {
                    int pixel1 = bitmap.getPixel(540, 960);
                    int pixel2 = bitmap.getPixel(100, 100);
                    int pixel3 = bitmap.getPixel(900, 1800);
                    int pixel4 = bitmap.getPixel(1040, 200);
                    int pixel5 = bitmap.getPixel(1000, 500);
                    int pixel6 = bitmap.getPixel(700, 100);
                    int pixel = pixel1+pixel2+pixel3+pixel4+pixel5+pixel6;
                    r += Color.red(pixel);
                    i++;
                } catch (Exception e) {
                    Log.d("bitmap err", "Bitmap error:" + e.getStackTrace());
                }
                red_avg[j] = r;
                j++;
                Log.d("redvalues", "Red values = "+r);
                Log.d("red average length", "Red average = "+red_avg.length);

            }

            sensorHandler = new SensorHandler();

            heart_rate = (int)Math.ceil(sensorHandler.calculate_filter_arrays(red_avg)*1.5);//multiplied to get heart rate for 1 minute.
            Toast.makeText(getApplicationContext(), "Heart rate ="+ (heart_rate), Toast.LENGTH_LONG).show();
            Log.d("heartrate", "Heart rate = "+heart_rate);

        } else {
            Toast.makeText(this, "No camera on device", Toast.LENGTH_LONG).show();
        }
    }


    private void openCamera(int id) {
        try {
            releaseCameraAndPreview();
            mCamera = Camera.open(id);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to open Camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("Mainactivity", "onSensorChanged : X: "+sensorEvent.values[0] + " Y: "+sensorEvent.values[1] + " Z: "+ sensorEvent.values[2]);
        accelValuesZ[index] = sensorEvent.values[2];
        index++;
        if(index > 127){
            index = 0;
            sensorHandler = new SensorHandler();
            respiratory_rate = (int)(sensorHandler.calculate_filter_arrays(accelValuesZ)*1.33);
            Toast.makeText(getApplicationContext(), "Respiratory rate = "+respiratory_rate, Toast.LENGTH_LONG).show();
            sensorManager.unregisterListener(this, mAccelerometer);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}