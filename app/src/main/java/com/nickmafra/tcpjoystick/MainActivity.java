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

    public static final Parser parser = new Parser();
    private static final int[] defaultLayoutsIds = {
            R.raw.default_joylayout,
            R.raw.default_race_joylayout
    };
    public static final List<JoyLayout> defaultLayouts = new ArrayList<>();

    private ScreenJoystickLayout screenJoystickLayout;
    private JoyClient joyClient;

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

        loadDefaultLayouts();

        menuLayout = findViewById(R.id.menu_layout);

        screenJoystickLayout = new ScreenJoystickLayout(this);
    }

    private void loadDefaultLayouts() {
        if (defaultLayouts.isEmpty()) {
            for (int rawFile : defaultLayoutsIds) {
                defaultLayouts.add(parser.load(getResources().openRawResource(rawFile)));
            }
        }
    }

    private boolean loadLayout() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);

        String layoutId = shared.getString("joystick_layout", "default");
        JoyLayout joyLayout = null;
        for (JoyLayout aLayout : defaultLayouts) {
            if (aLayout.getId().equals(layoutId)) {
                joyLayout = aLayout;
                break;
            }
        }
        if (joyLayout == null) {
            Toast.makeText(this, "Not found layout " + layoutId, Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            screenJoystickLayout.setJoyLayout(joyLayout);
            screenJoystickLayout.load();
            return true;
        } catch (Exception e) {
            String msg = "Error during load layout";
            Log.d(TAG, msg, e);
            Toast.makeText(this, msg + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
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

        if (!loadJoyIndex() || !loadLayout())
            return;

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