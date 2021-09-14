package com.nickmafra.tcpjoystick;

import android.annotation.SuppressLint;
import android.graphics.Insets;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import com.nickmafra.tcpjoystick.layout.JoyLayout;
import com.nickmafra.tcpjoystick.layout.JoyButtonPosition;

import java.util.HashMap;
import java.util.Map;

public class ScreenJoystickLayout implements View.OnTouchListener, JoyAxisView.Listener {

    private static final String TAG = ScreenJoystickLayout.class.getSimpleName();

    private final MainActivity activity;
    private final int unit;
    public JoyLayout joyLayout;

    private Map<View, ButtonData> map = new HashMap<>();

    private String pressPattern = "{\"B\":{\"Index\":${buttonIndex},\"Mode\":\"p\",\"JNo\":${joyIndex}}}";
    private String releasePattern = "{\"B\":{\"Index\":${buttonIndex},\"Mode\":\"r\",\"JNo\":${joyIndex}}}";
    private String axisPattern = "{\"${buttonIndex}\":{\"Direction\":\"${direction}\",\"Value\":${value},\"JNo\":${joyIndex}}}";

    public ScreenJoystickLayout(MainActivity activity) {
        this.activity = activity;
        this.unit = getSpaceUnit();
    }

    public void clear() {
        for (Map.Entry<View, ButtonData> entries : map.entrySet()) {
            activity.getLayout().removeView(entries.getKey());
        }
        map.clear();
    }

    public void load() {
        clear();
        if (joyLayout == null) {
            Log.d(TAG, "load: joyLayout is null");
            return;
        }

        for (JoyButton joyButton : joyLayout.getButtons()) {
            addButton(joyButton);
        }
    }

    public int getSpaceUnit() {
        Log.d(TAG, "getSpaceUnit: SDK version=" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().height() - insets.top - insets.bottom;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        }
    }

    public void addButton(JoyButton joyButton) {
        View view;
        if (joyButton.getType() == null)
            joyButton.setType("button");
        switch (joyButton.getType()) {
            case "axis":
                JoyAxisView axisView = new JoyAxisView(activity);
                axisView.setListener(this);
                AxisButtonData axisData = new AxisButtonData(1, "A");
                view = axisView;
                map.put(view, axisData);
                break;
            case "button":
            default:
                TextView textView = new TextView(activity);
                textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                textView.setText(joyButton.getText());
                textView.setBackground(ContextCompat.getDrawable(activity, R.drawable.round_button));
                ButtonData data = new ButtonData(activity.joyIndex, joyButton.getIndex());
                data.pressData = data.applyPattern(pressPattern).getBytes();
                data.releaseData = data.applyPattern(releasePattern).getBytes();
                textView.setOnTouchListener(this);
                view = textView;
                map.put(view, data);
                break;
        }

        Log.d(TAG, "addButton: unit=" + unit);
        int size = (int) (joyButton.getSize() * unit);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(size, size);
        JoyButtonPosition position = joyButton.getPosition();
        int center = size / 2;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (position.getBase() == null)
            position.setBase("center");
        switch (position.getBase()) {
            case "left":
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.leftMargin = (int) (position.getX() * unit) - center;
                layoutParams.bottomMargin = (int) (position.getY() * unit) - center;
                break;
            case "right":
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.rightMargin = (int) -(position.getX() * unit) - center;
                layoutParams.bottomMargin = (int) (position.getY() * unit) - center;
                break;
            case "center":
            default:
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                // TODO: use x as offset
                layoutParams.bottomMargin = (int) (position.getY() * unit) - center + unit / 2;
                break;
        }
        activity.getLayout().addView(view, layoutParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ButtonData data = map.get(v);
        if (data == null) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                sendCommand(data.pressData);
                return true;
            case MotionEvent.ACTION_UP:
                sendCommand(data.releaseData);
                return true;
            default:
                return false;
        }
    }

    private void sendCommand(byte[] command) {
        if (command != null) {
            activity.joyClient.addCommand(command);
        }
    }

    @Override
    public void onAxisChanged(View v, double relX, double relY) {
        ButtonData buttonData = map.get(v);
        if (!(buttonData instanceof AxisButtonData)) {
            return;
        }
        AxisButtonData data = (AxisButtonData) buttonData;

        sendCommand(data.applyPattern(axisPattern, "X", axisValueToPositiveInt(relX, 1000)).getBytes());
        sendCommand(data.applyPattern(axisPattern, "Y", axisValueToPositiveInt(relY, 1000)).getBytes());
    }

    private int axisValueToPositiveInt(double real, int max) {
        if (real <= -1)
            return 0;
        if (real >= 1)
            return max;
        return (int) (max * (real + 1) / 2);
    }
}
