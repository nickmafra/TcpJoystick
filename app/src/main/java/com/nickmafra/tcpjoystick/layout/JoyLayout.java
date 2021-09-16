package com.nickmafra.tcpjoystick.layout;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JoyLayout {
    private String id;
    private String name;
    private List<JoyButton> buttons;
}
