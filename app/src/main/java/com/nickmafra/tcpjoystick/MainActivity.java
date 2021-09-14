package com.nickmafra.tcpjoystick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import com.nickmafra.tcpjoystick.layout.JoyLayout;
import com.nickmafra.tcpjoystick.layout.Parser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RelativeLayout layout;
    private Parser parser;
    private ScreenJoystickLayout screenJoystickLayout;
    public final JoyClient joyClient = new JoyClient(this);
    public int joyIndex = 1; // TODO

    public RelativeLayout getLayout() {
        return layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layout = findViewById(R.id.main_layout);

        parser = new Parser();
        JoyLayout joyLayout = parser.loadDefault(this);

        screenJoystickLayout = new ScreenJoystickLayout(this);
        screenJoystickLayout.joyLayout = joyLayout;
        screenJoystickLayout.load();
        joyClient.start();
    }

    @Override
    protected void onDestroy() {
        joyClient.interrupt();
        super.onDestroy();
    }

    public void openSettings(View view) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void connect(View view) {
        String ip = null;
        Integer port = null;
        try {
            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
            ip = shared.getString("ip", null);
            port = Integer.parseInt(shared.getString("port", "0"));
        } catch (Exception e) {
            Log.d(TAG, "connect: error during get IP/port preferences.", e);
        }
        if (ip == null || ip.isEmpty() || port == null || port <= 0) {
            Toast.makeText(this, "Invalid IP/port", Toast.LENGTH_SHORT).show();
            return;
        }
        joyClient.setConnection(ip, port);
    }
}