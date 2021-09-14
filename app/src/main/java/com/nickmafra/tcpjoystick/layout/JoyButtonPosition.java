package com.nickmafra.tcpjoystick.layout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyButtonPosition {
    private String base; // "left", "right" or "center"
    private float x;
    private float y;
}
