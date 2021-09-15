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

public class JoyAxisView extends View implements JoyItemView {

    private static final String TAG = JoyAxisView.class.getSimpleName();

    private final AxisInput axisInput;

    private Paint circlePaint;
    private Paint buttonPaint;

    private float circleRadius;
    private float buttonRadius;
    private int centerX;
    private int centerY;
    private int positionX;
    private int positionY;

    public JoyAxisView(MainActivity mainActivity, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(mainActivity, attrs, defStyleAttr);

        this.axisInput = new AxisInput(mainActivity, this);
    }

    public JoyAxisView(MainActivity mainActivity, @Nullable AttributeSet attrs) {
        this(mainActivity, attrs, 0);
    }

    public JoyAxisView(MainActivity mainActivity) {
        this(mainActivity, null, 0);
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

        if (circlePaint != null)
            canvas.drawCircle(centerX, centerY, circleRadius, circlePaint);
        if (buttonPaint != null)
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
            dx /= factor;
            dy /= factor;
            positionX = centerX + dx;
            positionY = centerY + dy;
        }
        axisInput.setRels(dx / circleRadius, dy / circleRadius);

        invalidate(); // redraw
        return true;
    }

    @Override
    public View asView() {
        return this;
    }

    @Override
    public void onResume() {
        axisInput.onResume();
    }

    @Override
    public void onPause() {
        axisInput.onPause();
    }

    @Override
    public void config(JoyButton joyButton) {
        setDefaultPaints();
        axisInput.config(joyButton);
    }
}
