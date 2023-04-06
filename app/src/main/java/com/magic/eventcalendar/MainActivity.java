package com.magic.eventcalendar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private SharedPreferences.OnSharedPreferenceChangeListener deepLinkListener;
    private FirebaseAnalytics mFirebaseAnalytics;
    String uid;
    Fragment firstFragment;
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String FIRST_TIME_LAUNCH = "first_time_launch";

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        // 常にDark theme off
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        getUid();

        // チュートリアル
        if (isFirstTimeLaunch()) {
            // 最初の起動時にのみチュートリアルを表示する
            Log.d("genki","Tutorial Start!");
            showTutorial();
//            setFirstTimeLaunch(false);
            return;
        }

        if (uid.equals("GUEST")) {
            firstFragment = new CreateFragment();
        } else {
            firstFragment = new CalendarFragment();
        }
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

    }

    // 最初の起動時にtrueを返す
    private boolean isFirstTimeLaunch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(FIRST_TIME_LAUNCH, true);
    }

    // 最初の起動時にfalseを設定する
    private void setFirstTimeLaunch(boolean isFirstTime) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
        Log.d("genki", "Tutorial Done!");
    }

    // チュートリアルを表示する
    private void showTutorial() {
        setContentView(R.layout.tutorial_layout1);
        // レイアウト1を表示する
        // ユーザーが「次へ」ボタンを押すと、次のレイアウトを表示する
        // 最後のレイアウトの「完了」ボタンを押すと、メインアクティビティに戻る

        Button nextButton = findViewById(R.id.tutorial_next_button_1);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.tutorial_layout2);
                // レイアウト2を表示する
                // ユーザーが「次へ」ボタンを押すと、次のレイアウトを表示する
                // 最後のレイアウトの「完了」ボタンを押すと、メインアクティビティに戻る

                Button nextButton2 = findViewById(R.id.tutorial_next_button_2);
                nextButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setContentView(R.layout.tutorial_layout3);
                        // レイアウト3を表示する
                        // ユーザーが「次へ」ボタンを押すと、次のレイアウトを表示する
                        // 最後のレイアウトの「完了」ボタンを押すと、メインアクティビティに戻る

                        Button doneButton = findViewById(R.id.done_button);
                        doneButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 最初の起動フラグをfalseに設定して、メインアクティビティに戻る
                                setFirstTimeLaunch(false);
//                                setContentView(R.layout.create);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                });
            }
        });
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

    public void getUid() {
        // Get UID
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            uid = user.getUid();
        } catch (Exception e) {
            uid = "GUEST";
//            Intent intent = new Intent(getActivity(), MypageLogin.class);
//            startActivity(intent);
        }
    }
}