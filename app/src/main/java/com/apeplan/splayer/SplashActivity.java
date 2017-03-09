package com.apeplan.splayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    public void localPlay(View view) {
        Intent intent = new Intent(this, LocalVideoPlayerActivity.class);
        startActivity(intent);
    }

    public void localPlay2(View view) {
        Intent intent = new Intent(this, VideoViewDemo.class);
        startActivity(intent);
    }

}
