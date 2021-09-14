package com.nickmafra.tcpjoystick.layout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyButton {
    private String type;
    private String index;
    private String format; // for butons: rectangle ou round
    private String text;
    private JoyButtonPosition position;
    private float size;
    private float width;
    private float height;
}
