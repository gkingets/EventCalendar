package com.magic.eventcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener deepLinkListener;

    @Override
    protected void onStart() {
        super.onStart();
        preferences.registerOnSharedPreferenceChangeListener(deepLinkListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        preferences.unregisterOnSharedPreferenceChangeListener(deepLinkListener);
        deepLinkListener = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // DeepLink Setting
        preferences =
                getSharedPreferences("google.analytics.deferred.deeplink.prefs", MODE_PRIVATE);
        deepLinkListener = (sharedPreferences, key) -> {
            Log.d("DEEPLINK_LISTENER", "Deep link changed");
            if ("deeplink".equals(key)) {
                String deeplink = sharedPreferences.getString(key, null);
                Double cTime = Double.longBitsToDouble(sharedPreferences.getLong("timestamp", 0));
                Log.d("DEEPLINK_LISTENER", "Deep link retrieved: " + deeplink);
                showDeepLinkResult(deeplink);
            }
        };

        // 常にDark theme off
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        Fragment firstFragment = new CalendarFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.container,
                        firstFragment
                ).addToBackStack(null)
                .commit();

        BottomNavigationView navigationView =
                (BottomNavigationView) findViewById(R.id.bottom_navigation);
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selectedItemId = item.getItemId();
                Fragment newFragment = null;
                if (selectedItemId == R.id.navigation_calendar) {
                    newFragment = new CalendarFragment();
                } else if (selectedItemId == R.id.navigation_search) {
                    newFragment = new SearchFragment();
                } else if (selectedItemId == R.id.navigation_create) {
                    newFragment = new CreateFragment();
                } else {
                    newFragment = new MypageFragment();
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.container,
                                newFragment
                        ).addToBackStack(null)
                        .commit();
                return true;
            }
        });
//        navigationView.getMenu().getItem(1).setChecked(true);


        // ATTENTION: This was auto-generated to handle app links.
        Intent appLinkIntent = getIntent();
        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();
    }

    public void dialog(String timestamp, String uid) {
        CalendarEdit dialogRight = new CalendarEdit();
        // 渡す値をセット
        Bundle args = new Bundle();

        args.putString("uid", uid);
        args.putString("timestamp", timestamp);

        dialogRight.setArguments(args);
        dialogRight.show(getSupportFragmentManager(), "my_dialog");
    }

    public void showDeepLinkResult(String result) {
        String toastText = result;
        if (toastText == null) {
            toastText = "The deep link retrieval failed";
        } else if (toastText.isEmpty()) {
            toastText = "Deep link empty";
        }
        Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_LONG).show();
        Log.d("DEEPLINK", toastText);
    }
}