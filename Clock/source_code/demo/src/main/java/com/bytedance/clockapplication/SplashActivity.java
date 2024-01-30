package com.bytedance.clockapplication;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.byted.clockapplication.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                jumpToMainActivity();
            }
        }, 3000);
    }

    private void jumpToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
