package com.nickmafra.tcpjoystick;

public class ButtonData {
    public final int joyIndex;
    public final int buttonIndex;
    public byte[] pressData;
    public byte[] releaseData;

    public ButtonData(int joyIndex, int buttonIndex) {
        this.joyIndex = joyIndex;
        this.buttonIndex = buttonIndex;
    }

    public String applyPattern(String pattern) {
        return pattern
                .replace("${joyIndex}", String.valueOf(joyIndex))
                .replace("${buttonIndex}", String.valueOf(buttonIndex));
    }
}
