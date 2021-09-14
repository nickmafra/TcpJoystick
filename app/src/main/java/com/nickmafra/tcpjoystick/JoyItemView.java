package com.nickmafra.tcpjoystick;

import android.view.View;
import com.nickmafra.tcpjoystick.layout.JoyButton;

public interface JoyItemView {

    View asView();

    void onResume();

    void onPause();

    void setJoyIndex(int joyIndex);

    void setButtonIndex(String buttonIndex);

    void config(JoyButton joyButton);
}
