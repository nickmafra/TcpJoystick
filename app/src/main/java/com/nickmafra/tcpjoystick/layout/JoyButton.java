package com.nickmafra.tcpjoystick.layout;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoyButton {
    private String type;
    private String subtype;
    private String index;
    private String direction; // for POV buttons: d/r/u/l
    private String format; // for buttons: rectangle/round
    private String text;
    private JoyButtonPosition position;
    private float size;
    private float width;
    private float height;
}
