package uwaterloo.ca.lab4_206_01;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

    public class AccelerometerEventListener implements SensorEventListener {
        enum gesture {WAIT, LEFT, RIGHT, UP, DOWN, CHECK_X, CHECK_Z}



        float threshold_x = 2.0f;
        float threshold_z = 3.5f;
        int probe_size = 20;

        int cooldown = 0; //measured in samples. This is the cooldown after a direction has been chosen.
        int cooldown_time = probe_size;

        LinkedList probe_x = new LinkedList();
        LinkedList probe_z = new LinkedList();

        private TextView output;
        private TextView direction;

        private GameLoopTask myGameLoop;

        float high1, high2, high3;
        float[][] aReadings;
        float[] filteredReadings = new float[3];

        Map.Entry[] extrema_x = new Map.Entry[2]; //contains the max and min values of the points within the probe
        Map.Entry[] extrema_z = new Map.Entry[2]; //contains the max and min values of the points within the probe



        static boolean first;

        gesture gestureState = gesture.WAIT;

        public AccelerometerEventListener(TextView directionView, GameLoopTask gameLoop, float[][] readings) {
            direction = directionView;

            aReadings = readings;
            filteredReadings[0] = 0;
            filteredReadings[1] = 0;
            filteredReadings[2] = 0;

            myGameLoop = gameLoop;
            high1 = 0; //max x reading
            high2 = 0; //max y reading
            high3 = 0; //max z reading
            first = true;
        }

        public void onAccuracyChanged(Sensor s, int i) {

        }

        public void onSensorChanged(SensorEvent se) {

            if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                final float C = 5.0f;

                if (probe_x.size() >= probe_size) {
                    probe_x.removeLast();
                }
                probe_x.addFirst(filteredReadings[0]);

                if (probe_z.size() >= probe_size) {
                    probe_z.removeLast();
                }
                probe_z.addFirst(filteredReadings[2]);

                float x = se.values[0];
                float y = se.values[1];
                float z = se.values[2];
                filteredReadings[0] += (x - filteredReadings[0]) / C;
                filteredReadings[1] += (y - filteredReadings[1]) / C;
                filteredReadings[2] += (z - filteredReadings[2]) / C;
                float k = filteredReadings[0];
                float l = filteredReadings[1];
                float m = filteredReadings[2];



                //check for first value received
                if (first) {
                    high1 = se.values[0];
                    high2 = se.values[1];
                    high3 = se.values[2];
                    first = false;
                }

                //checks for maximum reading
                if (se.values[0] > high1) {
                    high1 = se.values[0];
                    high1 = se.values[0];
                }
                if (se.values[1] > high2) {
                    high2 = se.values[1];
                }
                if (se.values[2] > high3) {
                    high3 = se.values[2];
                }

                for (int i = aReadings.length - 2; i >= 0; i--) {
                    for (int j = 0; j < 3; j++) {
                        aReadings[i + 1][j] = aReadings[i][j];
                    }
                }

                for (int j = 0; j < 3; j++) {
                    aReadings[0][j] = se.values[j];
                }

                switch (gestureState) {
                    case WAIT:
                        String wait = "";
                        direction.setText(wait);
                        direction.setTextSize(50.50f);
                        Log.d("WAIT", String.format("%d", probe_x.size()));
                        if (probe_x.size() >= probe_size) {
                            Log.d("WAIT", String.format("I have %d samples", probe_x.size()));
                            extrema_x = getMaxMin(probe_x);
                            extrema_z = getMaxMin(probe_z);
                            if (Math.max(Math.abs((float) extrema_x[0].getValue()),
                                    Math.abs((float) extrema_x[1].getValue()))
                                    > Math.max(Math.abs((float) extrema_z[0].getValue()),
                                    Math.abs((float) extrema_z[1].getValue()))) {
                                Log.d("WAIT", "Going to gesture.CHECK_X");
                                gestureState = gesture.CHECK_X;
                            } else {
                                Log.d("WAIT", "Going to gesture.CHECK_Z");
                                gestureState = gesture.CHECK_Z;
                            }
                        }
                        break;
                    case CHECK_X:
                        Log.d("CHECK_X", "Before if statements");
                        if (Math.abs((float) extrema_x[0].getValue()) > threshold_x &&
                                Math.abs((float) extrema_x[1].getValue()) > threshold_x) {
                            Log.d("CHECK_X", "Inside threshold if statement");
                            if ((int) extrema_x[0].getKey() > (int) extrema_x[1].getKey()) {
                                Log.d("CHECK_X", "Going to gesture.RIGHT");
                                gestureState = gesture.RIGHT;
                            } else {
                                Log.d("CHECK_X", "Going to gesture.LEFT");
                                gestureState = gesture.LEFT;
                            }
                        } else {
                            Log.d("CHECK_X", "Going back to Wait");
                            gestureState = gesture.WAIT;
                        }
                        break;
                    case CHECK_Z:
                        Log.d("CHECK_Z", "Before if statements");
                        if (Math.abs((float) extrema_z[0].getValue()) > threshold_z &&
                                Math.abs((float) extrema_z[1].getValue()) > threshold_z) {
                            Log.d("CHECK_Z", "Inside threshold if statement");
                            if ((int) extrema_z[0].getKey() > (int) extrema_z[1].getKey()) {
                                Log.d("CHECK_Z", "Going to gesture.DOWN");
                                gestureState = gesture.DOWN;
                            } else {
                                Log.d("CHECK_Z", "Going to gesture.UP");
                                gestureState = gesture.UP;
                            }
                        } else {
                            Log.d("CHECK_Z", "Going back to gesture.WAIT");
                            gestureState = gesture.WAIT;
                        }
                        break;
                    case LEFT:
                        Log.d("LEFT", "Inside LEFT");
                        String left = "LEFT";
                        direction.setTextSize(50.50f);
                        myGameLoop.setDirection(GameLoopTask.directions.LEFT);
                        direction.setText(left);

                        if (cooldown < cooldown_time) {
                            cooldown++;
                        } else {
                            cooldown = 0;
                            gestureState = gesture.WAIT;
                        }
                        break;
                    case RIGHT:
                        Log.d("RIGHT", "Inside RIGHT");
                        String right = "RIGHT";
                        direction.setText(right);
                        myGameLoop.setDirection(GameLoopTask.directions.RIGHT);
                        direction.setTextSize(50.50f);
                        if (cooldown < cooldown_time) {
                            cooldown++;
                        } else {
                            cooldown = 0;
                            gestureState = gesture.WAIT;
                        }
                        break;
                    case UP:
                        Log.d("UP", "Inside UP");
                        String up = "UP";
                        direction.setText(up);
                        myGameLoop.setDirection(GameLoopTask.directions.UP);
                        direction.setTextSize(50.50f);
                        if (cooldown < cooldown_time) {
                            cooldown++;
                        } else {
                            cooldown = 0;
                            gestureState = gesture.WAIT;
                        }
                        break;
                    case DOWN:
                        Log.d("DOWN", "Inside DOWN");
                        String down = "DOWN";
                        direction.setTextSize(50.50f);
                        direction.setText(down);
                        myGameLoop.setDirection(GameLoopTask.directions.DOWN);
                        if (cooldown < cooldown_time) {
                            cooldown++;
                        } else {
                            cooldown = 0;
                            gestureState = gesture.WAIT;
                        }
                        break;
                    default:
                        Log.d("default", "Inside default");
                        gestureState = gesture.WAIT;
                        break;

                }

            }

        }
        private Map.Entry[] getMaxMin(LinkedList probe){

            float max_val = 0.0f;
            float min_val = 0.0f;

            int ind_max = 0;
            int ind_min = 0;

            Map.Entry[] out = new Map.Entry[2];

            for(int i = 0; i < 20; i++){
                if((float)probe.get(i) > max_val){
                    max_val = (float)probe.get(i);
                    ind_max = i;
                }
                if((float)probe.get(i) < min_val){
                    min_val = (float)probe.get(i);
                    ind_min = i;
                }


            }

            Map.Entry<Integer, Float> max = new AbstractMap.SimpleEntry<Integer, Float>(ind_max, max_val);
            Map.Entry<Integer, Float> min = new AbstractMap.SimpleEntry<Integer, Float>(ind_min, min_val);

            out[0] = max;
            out[1] = min;
            return out;
        }

    }


