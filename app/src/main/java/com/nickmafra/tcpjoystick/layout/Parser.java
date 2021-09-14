package com.nickmafra.tcpjoystick.layout;

import android.app.Activity;
import android.content.Context;
import com.google.gson.Gson;
import com.nickmafra.tcpjoystick.R;

import java.io.InputStream;
import java.io.InputStreamReader;

public class Parser {

    public final Gson gson = new Gson();

    public JoyLayout load(InputStream in) {
        return gson.fromJson(new InputStreamReader(in), JoyLayout.class);
    }

    public JoyLayout loadDefault(Context context) {
        return load(context.getResources().openRawResource(R.raw.default_joylayout));
    }
}
