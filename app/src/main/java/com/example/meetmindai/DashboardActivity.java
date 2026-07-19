package com.example.meetmindai;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvDate;
    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateDateTime();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvGreeting = findViewById(R.id.tvGreeting);
        tvDate = findViewById(R.id.tvDate);

        handler.post(runnable);

        // Setup default Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.contentFrame, new DashboardFragment())
                    .commit();
        }

        // Setup Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                fragment = new DashboardFragment();
            } else if (id == R.id.nav_meetings) {
                fragment = new MeetingsFragment();
            } else if (id == R.id.nav_tasks) {
                fragment = new TasksFragment();
            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.contentFrame, fragment)
                        .commit();
            }
            return true;
        });
    }

    private void updateDateTime() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);

        // Update Tanggal
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyyy | HH:mm", new Locale("id", "ID"));
        tvDate.setText(dateFormat.format(c.getTime()));

        // Update Greeting
        if (hour >= 4 && hour < 10) {
            tvGreeting.setText("Selamat Pagi, Adi");
        } else if (hour >= 10 && hour < 15) {
            tvGreeting.setText("Selamat Siang, Adi");
        } else if (hour >= 15 && hour < 18) {
            tvGreeting.setText("Selamat Sore, Adi");
        } else {
            tvGreeting.setText("Selamat Malam, Adi");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
