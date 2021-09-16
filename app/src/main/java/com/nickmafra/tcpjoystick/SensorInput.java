package com.nickmafra.tcpjoystick;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Surface;
import android.view.View;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import lombok.Setter;

public class SensorInput implements JoyItemView, SensorEventListener {

    @SuppressWarnings("unused")
    private static final String TAG = SensorInput.class.getSimpleName();

    private static final int DEFAULT_DELAY = 100;
    private static final float DEFAULT_DEAD_ZONE_PERCENT = 0.2F;
    private static final float DEFAULT_MULTIPLIER = 1.1F;

    private final MainActivity mainActivity;
    private final AxisInput axisInput;
    private final SensorManager sensorManager;
    private final Sensor sensor;

    @Setter
    private float multiplier = DEFAULT_MULTIPLIER;

    private final float[] values = new float[3];

    public SensorInput(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.axisInput = new AxisInput(mainActivity, null);
        sensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);

        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null)
            throw new UnsupportedOperationException("Accelerometer sensor not available.");
        sensor = accelerometer;
    }

    private void setValues(float[] rawValues) {
        System.arraycopy(rawValues, 0, this.values, 0, 3);
    }

    private void rotateToScreen() {
        int rotation = mainActivity.getDisplay().getRotation();
        float x = values[0];
        float y = values[1];
        switch (rotation) {
            case Surface.ROTATION_90:
                values[0] = -y;
                values[1] = x;
                break;
            case Surface.ROTATION_180:
                values[0] = -x;
                values[1] = -y;
                break;
            case Surface.ROTATION_270:
                values[0] = y;
                values[1] = -x;
                break;
            case Surface.ROTATION_0:
            default:
                break;
        }
    }

    private void normalize() {
        float magXY = (float) Math.hypot(values[0], values[1]);
        float magXYZ = (float) Math.hypot(magXY, values[2]);
        for (int i = 0; i < 3; i++) {
            values[i] *= multiplier / magXYZ;
            if (values[i] > 1)
                values[i] = 1;
            if (values[i] < -1)
                values[i] = -1;
        }
    }

    private void fixSignals() {
        values[0] = -values[0];
        values[2] = -values[2];
    }

    private void sendValues2d() {
        axisInput.setRels(values[0], values[1]);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            setValues(event.values);
            rotateToScreen();
            normalize();
            fixSignals();
            sendValues2d();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not needed
    }

    @Override
    public View asView() {
        return null;
    }

    @Override
    public void onResume() {
        axisInput.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);
        axisInput.onPause();
    }

    @Override
    public void config(JoyButton joyButton) {
        axisInput.config(joyButton);
        axisInput.setDelay(DEFAULT_DELAY);
        axisInput.setDeadZonePercent(DEFAULT_DEAD_ZONE_PERCENT);
    }
}
