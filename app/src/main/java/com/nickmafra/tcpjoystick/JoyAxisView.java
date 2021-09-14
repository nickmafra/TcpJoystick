package com.nickmafra.tcpjoystick;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import lombok.Setter;

public class JoyAxisView extends View {

    private static final String TAG = JoyAxisView.class.getSimpleName();

    @Setter
    private Paint circlePaint;
    @Setter
    private Paint buttonPaint;
    @Setter
    private Listener listener;

    @Setter
    private volatile float deadZonePercent = 0.1F;

    private float circleRadius;
    private float buttonRadius;
    private int centerX;
    private int centerY;
    private int positionX;
    private int positionY;

    public JoyAxisView(Context context) {
        super(context);
        setDefaultPaints();
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
            Log.d(TAG, "onTouchEvent: real [" + positionX + "," + positionY + "]");
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
        // normalized values (up to 1)
        double relAbs = abs / circleRadius;
        double relX;
        double relY;
        if (relAbs <= deadZonePercent) {
            relX = 0;
            relY = 0;
        } else {
            relX = dx / circleRadius;
            relY = dy / circleRadius;
        }

        Log.d(TAG, "onTouchEvent: normalized [" + relX + "," + relY + "]");

        if (listener != null)
            listener.onAxisChanged(this, relX, relY);
        invalidate(); // redraw
        return true;
    }

    public interface Listener {
        void onAxisChanged(View view, double relX, double relY);
    }
}
