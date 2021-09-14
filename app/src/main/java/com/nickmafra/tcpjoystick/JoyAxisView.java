package com.nickmafra.tcpjoystick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import lombok.Setter;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class JoyAxisView extends View implements Runnable {

    private static final String TAG = JoyAxisView.class.getSimpleName();

    private static final int INITIAL_DELAY = 1000;
    private static final int DEFAULT_MAX_SEND_DELAY = 200;

    @Setter
    private Paint circlePaint;
    @Setter
    private Paint buttonPaint;
    @Setter
    private Listener listener;

    @Setter
    private volatile float deadZonePercent = 0.1F;

    private final JoyAxisView THIS;
    private float circleRadius;
    private float buttonRadius;
    private int centerX;
    private int centerY;
    private int positionX;
    private int positionY;

    private volatile double relX;
    private volatile double relY;
    private volatile double lastRelX;
    private volatile double lastRelY;

    private final int maxSendDelay;
    private long lastSend;

    public JoyAxisView(Context context, int delay) {
        super(context);
        setDefaultPaints();
        Executors.newSingleThreadScheduledExecutor()
                .scheduleAtFixedRate(this, INITIAL_DELAY, delay, TimeUnit.MILLISECONDS);
        THIS = this;
        maxSendDelay = Math.min(delay, DEFAULT_MAX_SEND_DELAY);
    }

    public void setDefaultPaints() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#AEAEAE"));
        circlePaint.setStyle(Paint.Style.FILL);

        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(Color.parseColor("#4C4C4C"));
        buttonPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        positionX = getWidth() / 2;
        positionY = getWidth() / 2;
        int d = Math.min(xNew, yNew) / 2;

        // circleRadius + buttonRadius == 1
        circleRadius = (int) (0.7 * d);
        buttonRadius = (int) (0.3 * d);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

        canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);
        canvas.drawCircle(positionX, positionY, buttonRadius, buttonPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            positionX = centerX;
            positionY = centerY;
        } else {
            positionX = (int) event.getX();
            positionY = (int) event.getY();
        }

        int dx = positionX - centerX;
        int dy = positionY - centerY;
        double abs = Math.hypot(dx, dy);
        if (abs > circleRadius) {
            double factor = abs / circleRadius;
            abs = circleRadius;
            dx /= factor;
            dy /= factor;
            positionX = centerX + dx;
            positionY = centerY + dy;
        }
        double relAbs = abs / circleRadius;
        if (relAbs <= deadZonePercent) {
            relX = 0;
            relY = 0;
        } else {
            relX = dx / circleRadius;
            relY = dy / circleRadius;
        }

        invalidate(); // redraw
        return true;
    }

    public interface Listener {
        void onAxisChanged(View view, double relX, double relY);
    }

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long diff = time - lastSend;
        if ((relX != lastRelX || relY != lastRelY || diff > maxSendDelay)) {
            lastSend = time;
            lastRelX = relX;
            lastRelY = relY;
            if (listener != null)
                listener.onAxisChanged(THIS, relX, relY);
        }
    }
}
