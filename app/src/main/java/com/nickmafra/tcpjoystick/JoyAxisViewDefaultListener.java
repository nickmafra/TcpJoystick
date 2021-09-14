package com.nickmafra.tcpjoystick;

import android.view.View;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyAxisViewDefaultListener implements JoyAxisViewListener {

    private final JoyAxisView joyAxisView;

    // for performance only
    public String preDataX;
    public String posDataX;
    public String preDataY;
    public String posDataY;

    public JoyAxisViewDefaultListener(JoyAxisView joyAxisView) {
        this.joyAxisView = joyAxisView;
    }

    public byte[] getBytesX(double value) {
        return (preDataX + value + posDataX).getBytes();
    }

    public byte[] getBytesY(double value) {
        return (preDataY + value + posDataY).getBytes();
    }

    @Override
    public void onAxisChanged(View v, double relX, double relY) {
        joyAxisView.getMainActivity().addCommand(getBytesX(axisValueToPositiveInt(relX, 1000)));
        joyAxisView.getMainActivity().addCommand(getBytesY(axisValueToPositiveInt(relY, 1000)));
    }

    private int axisValueToPositiveInt(double real, int max) {
        if (real <= -1)
            return 0;
        if (real >= 1)
            return max;
        return (int) (max * (real + 1) / 2);
    }
}
