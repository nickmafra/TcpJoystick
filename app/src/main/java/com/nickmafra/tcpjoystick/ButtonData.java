package com.nickmafra.tcpjoystick;

public class ButtonData {
    public final int joyIndex;
    public final String buttonIndex;

    // for performance only
    public byte[] pressData;
    public byte[] releaseData;

    public ButtonData(int joyIndex, String buttonIndex) {
        this.joyIndex = joyIndex;
        this.buttonIndex = buttonIndex;
    }

    public String applyPattern(String pattern) {
        return pattern
                .replace("${joyIndex}", String.valueOf(joyIndex))
                .replace("${buttonIndex}", buttonIndex);
    }

    public void setPressReleaseData(String pressPattern, String releasePattern) {
        pressData = applyPattern(pressPattern).getBytes();
        releaseData = applyPattern(releasePattern).getBytes();
    }
}
