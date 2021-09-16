package com.nickmafra.tcpjoystick;

import android.graphics.Insets;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.RelativeLayout;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import com.nickmafra.tcpjoystick.layout.JoyButtonPosition;
import com.nickmafra.tcpjoystick.layout.JoyLayout;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class ScreenJoystickLayout {

    private static final String TAG = ScreenJoystickLayout.class.getSimpleName();

    public static final String BUTTON_TYPE = "button";
    public static final String AXIS_TYPE = "axis";
    public static final String SENSOR_TYPE = "sensor";
    public static final String DEFAULT_TYPE = BUTTON_TYPE;

    @Getter
    private final MainActivity mainActivity;
    private final int unit;
    @Setter
    private JoyLayout joyLayout;

    private final List<JoyItemView> viewItems = new ArrayList<>();

    public ScreenJoystickLayout(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.unit = getSpaceUnit();
    }

    protected void onResume() {
        for (JoyItemView joyItemView : viewItems) {
            joyItemView.onResume();
        }
    }

    protected void onPause() {
        for (JoyItemView joyItemView : viewItems) {
            joyItemView.onPause();
        }
    }

    public void clear() {
        for (JoyItemView joyItemView : viewItems) {
            joyItemView.onPause();
            View view = joyItemView.asView();
            if (view != null)
                mainActivity.getLayout().removeView(view);
        }
        viewItems.clear();
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

        mainActivity.getMenuLayout().bringToFront();
    }

    @SuppressWarnings("deprecation")
    public int getSpaceUnit() {
        Log.d(TAG, "getSpaceUnit: SDK version=" + Build.VERSION.SDK_INT);
        int spaceUnit;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = mainActivity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            spaceUnit = Math.min(
                    windowMetrics.getBounds().width() - insets.left - insets.right,
                    windowMetrics.getBounds().height() - insets.top - insets.bottom
            );
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            mainActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            spaceUnit = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        Log.i(TAG, "getSpaceUnit: " + spaceUnit);
        return spaceUnit;
    }

    public void addButton(JoyButton joyButton) {
        JoyItemView joyItemView;
        joyItemView = toView(joyButton);
        joyItemView.config(joyButton);
        viewItems.add(joyItemView);

        if (joyItemView.asView() == null)
            return; // invisible

        int width = (int) ((joyButton.getSize() > 0 ? joyButton.getSize() : joyButton.getWidth()) * unit);
        int height = (int) ((joyButton.getSize() > 0 ? joyButton.getSize() : joyButton.getHeight()) * unit);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        JoyButtonPosition position = joyButton.getPosition();
        int centerX = width / 2;
        int centerY = height / 2;
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (position.getBase() == null)
            position.setBase("center");
        switch (position.getBase()) {
            case "left":
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.leftMargin = (int) (position.getX() * unit) - centerX;
                layoutParams.bottomMargin = (int) (position.getY() * unit) - centerY;
                break;
            case "right":
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.rightMargin = (int) -(position.getX() * unit) - centerX;
                layoutParams.bottomMargin = (int) (position.getY() * unit) - centerY;
                break;
            case "center":
            default:
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                // TODO: use x as offset
                layoutParams.bottomMargin = (int) (position.getY() * unit) - centerY + unit / 2;
                break;
        }
        mainActivity.getLayout().addView(joyItemView.asView(), layoutParams);
    }

    public JoyItemView toView(JoyButton joyButton) {
        if (joyButton.getType() == null)
            joyButton.setType(DEFAULT_TYPE);

        switch (joyButton.getType()) {
            case AXIS_TYPE:
                return new JoyAxisView(mainActivity);
            case BUTTON_TYPE:
                return new JoyButtonView(mainActivity);
            case SENSOR_TYPE:
                return new SensorInput(mainActivity);
            default:
                throw new IllegalArgumentException("Invalid joyButton type: " + joyButton.getType());
        }
    }
}
