package uwaterloo.ca.lab4_206_01;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Timer;



public class MainActivity extends AppCompatActivity {

    public GameLoopTask myTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Timer myTimer = new Timer();
        Activity myActivity = new Activity();
        RelativeLayout myRL = (RelativeLayout) findViewById(R.id.main_LL);
        Context myContext = getApplicationContext();
        myTask = new GameLoopTask(myActivity, myRL, myContext);

        Log.i("DEBUG", "INITIALIZING");

        final float[][] accelReadings = new float[100][3];
        RelativeLayout ll = (RelativeLayout) findViewById(R.id.main_LL);
        RelativeLayout tvRl = (RelativeLayout) findViewById(R.id.tv_rl);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        ll.getLayoutParams().width = width;
        ll.getLayoutParams().height = width;

        ll.setBackgroundResource(R.drawable.gameboard);
        //TextView for LightSensor Readings
        TextView myDisplayTV = new TextView(getApplicationContext());
        myDisplayTV.setTextColor(Color.BLACK);
        Log.i("DEBUG", "RUNNING TIMER");
        myTimer.schedule(myTask,50,50);
        if (myTask.gameOverFlag) {
            myTimer.cancel();
        }


        //Button for resetting record data to 0
        Button resetButton = new Button(getApplicationContext());
        resetButton.setText("Clear Record-High Data");
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AccelerometerEventListener.first = true;
            }
        });

        //Button to generate CSV file
        Button generateButton = new Button(getApplicationContext());
        generateButton.setText("Generate CSV Record for Accelerometer Readings");
        generateButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                File file = new File(getExternalFilesDir("Readings"), "accelReading.csv");
                Log.w("File path: ", file.getAbsolutePath());
                PrintWriter printWriter;
                try {
                    printWriter = new PrintWriter(file);
                    for (int i = 0; i < 100; i++) {
                        printWriter.println(String.format("%f, %f, %f", accelReadings[i][0], accelReadings[i][1], accelReadings[i][2]));
                    }
                    printWriter.close();
                    Log.i("Completion", "Succesfully wrote file");
                } catch(FileNotFoundException e) {
                    Log.i("Completion", "Failed to write File");
                }
            }
        });

        //adding views(vertically)


        tvRl.addView(myDisplayTV);
        tvRl.setY(1600);


        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        //initializing values with constructors
        SensorEventListener a = new AccelerometerEventListener (myDisplayTV, myTask, accelReadings);


        //registering sensors
        sensorManager.registerListener(a, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);


    }
}


