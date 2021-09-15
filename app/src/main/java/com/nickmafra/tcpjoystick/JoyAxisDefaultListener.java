package com.nickmafra.tcpjoystick;

import android.view.View;
import lombok.Getter;
import lombok.Setter;

public class JoyAxisDefaultListener implements JoyAxisListener {

    private final MainActivity mainActivity;

    // for performance only
    private String preDataX;
    private String betweenXandY;
    private String posDataY;

    public JoyAxisDefaultListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setPerformanceData(String preDataX, String posDataX, String preDataY, String posDataY) {
        this.preDataX = preDataX;
        this.betweenXandY = posDataX + preDataY;
        this.posDataY = posDataY;
    }

    @Override
    public void onAxisChanged(View v, Number x, Number y) {
        String strCommand = preDataX + x + betweenXandY + y + posDataY;
        mainActivity.addCommand(strCommand.getBytes());
    }
}
