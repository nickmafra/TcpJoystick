package com.nickmafra.tcpjoystick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorInput implements SensorEventListener {

    private static final String TAG = SensorInput.class.getSimpleName();

    private final MainActivity activity;

    private final SensorManager sensorManager;
    private final Sensor sensor;

    public SensorInput(MainActivity activity) {
        this.activity = activity;
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    private float[] values = new float[3];

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            values[0] = event.values[0];
            values[1] = event.values[1];
            values[2] = event.values[2];
            Log.d(TAG, "onSensorChanged: [" + values[0] + ", " + values[1] + ", " + values[2] + "]");
            updateValues2d();
        }
    }

    private float[] values2d = new float[2];

    private void updateValues2d() {
        double magXY = Math.hypot(values[0], values[1]);
        if (magXY < Math.abs(values[2])) {
            values2d[0] = 0;
            values2d[1] = 0;
        } else {
            values2d[0] = (float) (values[0] / magXY);
            values2d[1] = (float) (values[1] / magXY);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not needed
    }
}
