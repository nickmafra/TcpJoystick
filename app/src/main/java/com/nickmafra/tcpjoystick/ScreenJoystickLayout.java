package com.nickmafra.tcpjoystick;

import android.graphics.Insets;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.RelativeLayout;
import com.nickmafra.tcpjoystick.layout.JoyButton;
import com.nickmafra.tcpjoystick.layout.JoyButtonPosition;
import com.nickmafra.tcpjoystick.layout.JoyLayout;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ScreenJoystickLayout {

    private static final String TAG = ScreenJoystickLayout.class.getSimpleName();

    @Getter
    private final MainActivity mainActivity;
    private final int unit;
    public JoyLayout joyLayout;

    private List<JoyItemView> viewItems = new ArrayList<>();

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
            mainActivity.getLayout().removeView(joyItemView.asView());
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
    }

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
        if (joyButton.getType() == null)
            joyButton.setType("button");
        switch (joyButton.getType()) {
            case "axis":
                JoyAxisView axisView = new JoyAxisView(mainActivity, 100);
                axisView.config(joyButton);
                joyItemView = axisView;
                break;
            case "button":
            default:
                JoyButtonView buttonView = new JoyButtonView(mainActivity);
                buttonView.config(joyButton);
                joyItemView = buttonView;
                break;
        }
        viewItems.add(joyItemView);

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
        mainActivity.getLayout().addView(joyItemView.asView(), layoutParams);
    }
}
