package com.nickmafra.tcpjoystick;

import android.view.View;

public interface JoyAxisViewListener {
    void onAxisChanged(View view, double relX, double relY);
}
