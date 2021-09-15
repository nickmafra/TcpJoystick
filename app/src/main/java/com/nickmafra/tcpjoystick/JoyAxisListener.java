package com.nickmafra.tcpjoystick;

import android.view.View;

public interface JoyAxisListener {
    void onAxisChanged(View view, Number x, Number y);
}
