package com.nickmafra.tcpjoystick;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyButtonTouchListener implements View.OnTouchListener {

    // for performance only
    private byte[] pressData;
    private byte[] releaseData;

    private final JoyButtonView joyButtonView;

    public JoyButtonTouchListener(JoyButtonView joyButtonView) {
        this.joyButtonView = joyButtonView;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        byte[] command;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                command = getPressData();
                break;
            case MotionEvent.ACTION_UP:
                command = getReleaseData();
                break;
            default:
                command = null;
                break;
        }
        if (command != null) {
            joyButtonView.getMainActivity().addCommand(command);
            return true;
        } else {
            return false;
        }
    }
}
