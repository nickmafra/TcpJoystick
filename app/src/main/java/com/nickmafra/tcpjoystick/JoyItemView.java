package com.nickmafra.tcpjoystick;

import android.view.View;
import androidx.annotation.Nullable;
import com.nickmafra.tcpjoystick.layout.JoyButton;

public interface JoyItemView {

    @Nullable View asView();

    void onResume();

    void onPause();

    void config(JoyButton joyButton);
}
