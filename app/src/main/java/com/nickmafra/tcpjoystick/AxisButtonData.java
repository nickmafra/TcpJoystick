package com.nickmafra.tcpjoystick;

public class AxisButtonData extends ButtonData {

    public String preDataX;
    public String posDataX;
    public String preDataY;
    public String posDataY;

    public AxisButtonData(int joyIndex, String buttonIndex) {
        super(joyIndex, buttonIndex);
    }

    public String applyPattern(String pattern, String direction) {
        return applyPattern(pattern)
                .replace("${direction}", direction);
    }

    public void setPrePosAxisData(String prePattern, String posPattern) {
        preDataX = applyPattern(prePattern, "X");
        posDataX = applyPattern(posPattern, "X");
        preDataY = applyPattern(prePattern, "Y");
        posDataY = applyPattern(posPattern, "Y");
    }

    public byte[] getBytesX(double value) {
        return (preDataX + value + posDataX).getBytes();
    }

    public byte[] getBytesY(double value) {
        return (preDataY + value + posDataY).getBytes();
    }

}
