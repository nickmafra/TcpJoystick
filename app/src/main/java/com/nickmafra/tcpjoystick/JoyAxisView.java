package com.nickmafra.tcpjoystick;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JoyAxisView extends View implements Runnable, JoyItemView {

    private static final String TAG = JoyAxisView.class.getSimpleName();

    private static final int INITIAL_DELAY = 1000;
    private static final int DEFAULT_MAX_SEND_DELAY = 500;
    private static final String AXIS_PRE_PATTERN = "{\"${buttonIndex}\":{\"Direction\":\"${direction}\",\"Value\":";
    private static final String AXIS_POS_PATTERN = ",\"JNo\":${joyIndex}}}";

    @Getter
    private final MainActivity mainActivity;

    private int delay;
    private ScheduledExecutorService executor;

    @Setter
    private Paint circlePaint;
    @Setter
    private Paint buttonPaint;
    @Setter
    private JoyAxisViewListener listener;

    @Setter
    private volatile float deadZonePercent = 0.1F;

    private final JoyAxisView thisView;
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

    @Getter
    @Setter
    private int joyIndex;
    @Getter
    @Setter
    private String buttonIndex;

    public JoyAxisView(MainActivity mainActivity, int delay, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(mainActivity, attrs, defStyleAttr);

        this.mainActivity = mainActivity;
        this.delay = delay;
        setDefaultPaints();
        thisView = this;
        maxSendDelay = Math.max(delay, DEFAULT_MAX_SEND_DELAY);
    }

    public JoyAxisView(MainActivity mainActivity, int delay, @Nullable AttributeSet attrs) {
        this(mainActivity, delay, attrs, 0);
    }

    public JoyAxisView(MainActivity mainActivity, int delay) {
        this(mainActivity, delay, null, 0);
    }

    @Override
    public View asView() {
        return this;
    }

    @Override
    public void onResume() {
        if (executor != null)
            throw new IllegalStateException("executor is not null on resume!");

        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(this, INITIAL_DELAY, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onPause() {
        executor.shutdownNow();
        executor = null;
    }

    public void setDefaultPaints() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#AEAEAE"));
        circlePaint.setStyle(Paint.Style.FILL);

        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(Color.parseColor("#4C4C4C"));
        buttonPaint.setStyle(Paint.Style.FILL);
    }

    public String applyPattern(String pattern, String direction) {
        return pattern
                .replace("${joyIndex}", String.valueOf(joyIndex))
                .replace("${buttonIndex}", buttonIndex)
                .replace("${direction}", direction);
    }

    @Override
    public void config(JoyButton joyButton) {
        setJoyIndex(mainActivity.getJoyIndex());
        setButtonIndex(joyButton.getIndex());
        JoyAxisViewDefaultListener defaultListener = new JoyAxisViewDefaultListener(this);
        defaultListener.setPreDataX(applyPattern(AXIS_PRE_PATTERN, "X"));
        defaultListener.setPosDataX(applyPattern(AXIS_POS_PATTERN, "X"));
        defaultListener.setPreDataY(applyPattern(AXIS_PRE_PATTERN, "Y"));
        defaultListener.setPosDataY(applyPattern(AXIS_POS_PATTERN, "Y"));
        this.listener = defaultListener;
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

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long diff = time - lastSend;
        if ((relX != lastRelX || relY != lastRelY || diff > maxSendDelay)) {
            lastSend = time;
            lastRelX = relX;
            lastRelY = relY;
            if (listener != null)
                listener.onAxisChanged(thisView, relX, relY);
        }
    }
}
