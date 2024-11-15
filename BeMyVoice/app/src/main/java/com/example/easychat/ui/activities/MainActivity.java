package com.example.easychat.ui.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.easychat.R;
import com.example.easychat.utils.FirebaseUtil;
import com.example.easychat.ui.fragments.ChatFragment;
import com.google.firebase.messaging.FirebaseMessaging;
import com.example.easychat.ui.fragments.ProfileFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;
import com.google.android.material.navigation.NavigationView;

@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
public class MainActivity extends AppCompatActivity {
    ImageView drawerButton;
    NavigationView navView;
    ImageButton searchButton;
    DrawerLayout drawerLayout;
    ChatFragment chatFragment;
    ProfileFragment profileFragment;
    ChipNavigationBar bottomNavigationView;

    private static final int PERMISSION_REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        if (!isNotificationPermissionGranted()) {
            requestNotificationPermission();
        }

        chatFragment = new ChatFragment();
        profileFragment = new ProfileFragment();

        navView = findViewById(R.id.navView);
        drawerButton = findViewById(R.id.drawerMenu);
        drawerLayout = findViewById(R.id.drawerLayout);
        searchButton = findViewById(R.id.main_search_btn);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
        navView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.basic_needs) {
                startActivity(new Intent(MainActivity.this, BasicNeedsActivity.class));
                drawerLayout.close();
                return false;
            } else if (itemId == R.id.signs) {
                startActivity(new Intent(MainActivity.this, SignsActivity.class));
                drawerLayout.close();
            } else if (itemId == R.id.feedback) {
                Toast.makeText(this, "Feedback", Toast.LENGTH_SHORT).show();
                drawerLayout.close();
                return false;
            } else if (itemId == R.id.about_us) {
                Toast.makeText(this, "About Us", Toast.LENGTH_SHORT).show();
                drawerLayout.close();
                return false;
            }
            return false;
        });

        drawerButton.setOnClickListener(v -> drawerLayout.openDrawer(navView));

        searchButton.setOnClickListener((v) -> startActivity(new Intent(MainActivity.this, SearchUserActivity.class)));

        bottomNavigationView.setItemSelected(R.id.menu_chat, true);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item == R.id.menu_chat) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, chatFragment).commit();
            }
            if (item == R.id.menu_profile) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, profileFragment).commit();
            }
        });
        getFCMToken();
    }

    void getFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                FirebaseUtil.currentUserDetails().update("fcmToken", token);

            }
        });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Log.d("TAG_PERMISSIONS", "Permissions granted");
            } else {
                Log.d("TAG_PERMISSIONS", "Permission denied");
            }
        }
    }

    private boolean isNotificationPermissionGranted() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            return notificationManager.areNotificationsEnabled();
        }
        return false;
    }

    private void requestNotificationPermission() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        startActivity(intent);
    }

}