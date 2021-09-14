package com.nickmafra.tcpjoystick.layout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyButton {
    private int index;
    private String text;
    private JoyButtonPosition position;
    private float size;
}
