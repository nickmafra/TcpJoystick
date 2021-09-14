package com.nickmafra.tcpjoystick;

public class AxisButtonData extends ButtonData {

    public AxisButtonData(int joyIndex, String buttonIndex) {
        super(joyIndex, buttonIndex);
    }

    public String applyPattern(String pattern, String direction, double value) {
        return applyPattern(pattern)
                .replace("${direction}", direction)
                .replace("${value}", String.valueOf(value));
    }

}
