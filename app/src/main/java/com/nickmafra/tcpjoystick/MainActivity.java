package com.nickmafra.tcpjoystick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.nickmafra.tcpjoystick.layout.JoyLayout;
import com.nickmafra.tcpjoystick.layout.Parser;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RelativeLayout layout;
    @Getter
    private LinearLayout menuLayout;

    private Parser parser;
    private ScreenJoystickLayout screenJoystickLayout;
    private JoyClient joyClient;
    private static final int[] defaultLayouts = {
            R.raw.default_joylayout,
            R.raw.race_joylayout
    };
    private final List<JoyLayout> layouts = new ArrayList<>();

    @Getter
    public int joyIndex;

    public RelativeLayout getLayout() {
        return layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        layout = findViewById(R.id.main_layout);

        parser = new Parser();
        loadDefaultLayouts();

        menuLayout = findViewById(R.id.menu_layout);

        screenJoystickLayout = new ScreenJoystickLayout(this);
        screenJoystickLayout.setJoyLayout(layouts.get(0));
    }

    private void loadDefaultLayouts() {
        for (int rawFile : defaultLayouts) {
            layouts.add(parser.load(getResources().openRawResource(rawFile)));
        }
    }

    private void loadLayout() {
        try {
            screenJoystickLayout.load();
        } catch (Exception e) {
            String msg = "Error during load layout";
            Log.d(TAG, msg, e);
            Toast.makeText(this, msg + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private boolean loadJoyIndex() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);

        joyIndex = -1;
        try {
            joyIndex = Integer.parseInt(shared.getString("joystick_index", "-1"));
        } catch (Exception e) {
            Log.d(TAG, "connect: error during get joystick.", e);
        }
        if (joyIndex < 0) {
            Toast.makeText(this, "Invalid joystick index", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!loadJoyIndex())
            return;

        loadLayout();

        if (joyClient != null)
            throw new IllegalStateException("joyClient is not null on resume!");

        joyClient = new JoyClient(this);
        joyClient.start();
        screenJoystickLayout.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        screenJoystickLayout.onPause();
        if (joyClient != null) {
            joyClient.interrupt();
            joyClient = null;
        }
    }

    @SuppressWarnings("unused")
    public void openSettings(View view) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("unused")
    public void connect(View view) {
        if (joyClient == null) {
            Toast.makeText(this, "Invalid server settings.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);

        String ip = null;
        int port = -1;
        try {
            ip = shared.getString("ip", "");
            port = Integer.parseInt(shared.getString("port", "-1"));
        } catch (Exception e) {
            Log.d(TAG, "connect: error during get IP/port preferences.", e);
        }
        if (ip == null || ip.isEmpty() || port <= 0) {
            Toast.makeText(this, "Invalid IP/port", Toast.LENGTH_SHORT).show();
            return;
        }
        joyClient.setConnection(ip, port);
    }

    public void addCommand(byte[] command) {
        if (joyClient != null)
            joyClient.addCommand(command);
    }
}