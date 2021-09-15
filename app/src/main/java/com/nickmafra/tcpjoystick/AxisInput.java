package com.nickmafra.tcpjoystick;

import android.view.View;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import lombok.Setter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AxisInput implements Runnable {

    private static final int INITIAL_DELAY = 1000;
    private static final int DEFAULT_DELAY = 100;
    private static final int DEFAULT_MAX_SEND_DELAY = 500;
    private static final float DEFAULT_DEAD_ZONE_PERCENT = 0.1F;

    private static final String AXIS_PRE_PATTERN = "{\"${buttonIndex}\":{\"Direction\":\"${direction}\",\"Value\":";
    private static final String AXIS_POS_PATTERN = ",\"JNo\":${joyIndex}}}";

    private final MainActivity mainActivity;
    private final View view;

    private volatile int delay = DEFAULT_DELAY;
    private volatile int maxSendDelay = DEFAULT_MAX_SEND_DELAY;

    private int joyIndex;
    private String buttonIndex;

    private volatile boolean isInt;
    private volatile double minIntValue;
    private volatile double maxIntValue;
    private volatile double range;
    private volatile double midOffset;

    private final AtomicReference<JoyAxisListener> listenerRef = new AtomicReference<>();

    private ScheduledExecutorService executor;

    @Setter
    private volatile float deadZonePercent = DEFAULT_DEAD_ZONE_PERCENT;

    private volatile double relX;
    private volatile double relY;

    private volatile double lastRelX;
    private volatile double lastRelY;
    private volatile long lastSend;

    public AxisInput(MainActivity mainActivity, View view) {
        this.mainActivity = mainActivity;
        this.view = view;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        this.maxSendDelay = Math.max(delay, DEFAULT_MAX_SEND_DELAY);
    }

    public void setRels(double relX, double relY) {
        double abs = Math.hypot(relX, relY);
        if (abs <= deadZonePercent) {
            this.relX = 0;
            this.relY = 0;
        } else {
            this.relX = relX;
            this.relY = relY;
        }
    }

    public void onResume() {
        if (executor != null)
            throw new IllegalStateException("executor is not null on resume!");

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this, INITIAL_DELAY, delay, TimeUnit.MILLISECONDS);
    }

    public void onPause() {
        executor.shutdownNow();
        executor = null;
    }

    public void setMinMaxValues(int minIntValue, int maxIntValue) {
        this.minIntValue = minIntValue;
        this.maxIntValue = maxIntValue;
        this.range = maxIntValue - minIntValue;
        this.midOffset = (minIntValue + maxIntValue) / 2D;
    }

    public String applyPattern(String pattern, String direction) {
        return pattern
                .replace("${joyIndex}", String.valueOf(joyIndex))
                .replace("${buttonIndex}", buttonIndex)
                .replace("${direction}", direction);
    }

    private void setDefaultListenerFromPattern(String prePattern, String posPattern) {
        JoyAxisDefaultListener listener = new JoyAxisDefaultListener(mainActivity);
        listener.setPerformanceData(
                applyPattern(prePattern, "X"),
                applyPattern(posPattern, "X"),
                applyPattern(prePattern, "Y"),
                applyPattern(posPattern, "Y")
        );
        this.listenerRef.set(listener);
    }

    public void config(JoyButton joyButton) {
        joyIndex = mainActivity.getJoyIndex();
        buttonIndex = joyButton.getIndex();
        isInt = true;
        // defaults for vJoy server
        setMinMaxValues(0, 1000);
        setDefaultListenerFromPattern(AXIS_PRE_PATTERN, AXIS_POS_PATTERN);
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long diff = time - lastSend;
        if ((relX != lastRelX || relY != lastRelY || diff > maxSendDelay)) {
            lastSend = time;
            lastRelX = relX;
            lastRelY = relY;
            JoyAxisListener listener = listenerRef.get();
            if (listener != null) {
                Number x = relX;
                Number y = relY;
                if (range != 0) {
                    x = axisValueToRange(relX);
                    y = axisValueToRange(relY);
                }
                if (isInt) {
                    x = x.intValue();
                    y = y.intValue();
                }
                listener.onAxisChanged(view, x, y);
            }
        }
    }

    public double axisValueToRange(double real) {
        if (real <= -1)
            return minIntValue;
        if (real >= 1)
            return maxIntValue;
        return range * (real / 2) + midOffset;
    }
}
