package com.nickmafra.tcpjoystick;

public class Utils {
    private Utils() {}

    @SuppressWarnings("java:S2447")
    public static Boolean toBoolean(String str) {
        if (str == null || str.equalsIgnoreCase("null"))
            return null;
        return Boolean.parseBoolean(str);
    }
}
