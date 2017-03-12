package com.apeplan.splayer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.apeplan.splayer.ui.VideoListActivity;

import io.vov.vitamio.Vitamio;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Vitamio.isInitialized(getApplicationContext());
    }

    public void localPlay(View view) {

//        Intent intent = new Intent(this, LocalVideoPlayerActivity.class);
        Intent intent = new Intent(this, VideoViewDemo.class);
        startActivity(intent);
    }

    public void localPlay2(View view) {
//        Intent intent = new Intent(this, VideoViewDemo.class);
        Intent intent = new Intent(this, VideoListActivity.class);
        startActivity(intent);
    }

}
