package com.nickmafra.tcpjoystick;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import com.nickmafra.tcpjoystick.layout.JoyLayout;

import java.util.List;

import static com.nickmafra.tcpjoystick.Utils.toBoolean;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SharedPreferences sharedPreferences;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            sharedPreferences = getPreferenceManager().getSharedPreferences();
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);

            loadLayoutList();
        }

        private void loadLayoutList() {
            String key = "joystick_layout";
            ListPreference layoutList = findPreference(key);
            if (layoutList == null)
                throw new NullPointerException(key + " not found.");

            List<JoyLayout> layouts = MainActivity.defaultLayouts;

            String[] ids = new String[layouts.size()];
            String[] names = new String[layouts.size()];
            for (int i = 0; i < layouts.size(); i++) {
                JoyLayout layout = layouts.get(i);
                ids[i] = layout.getId();
                names[i] = layout.getName();
            }

            layoutList.setEntries(names);
            layoutList.setEntryValues(ids);
        }

        private static int toModeNightInt(String ternary) {
            Boolean modeNight = toBoolean(ternary);
            if (modeNight == null) {
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            } else if (modeNight) {
                return AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                return AppCompatDelegate.MODE_NIGHT_NO;
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("mode_night")) {
                String modeNight = sharedPreferences.getString(key, null);
                AppCompatDelegate.setDefaultNightMode(toModeNightInt(modeNight));
            }
        }
    }
}