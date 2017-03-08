package com.apeplan.splayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    private class ScanVideoTask extends AsyncTask<Void, File, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        public void eachVideo(File file) {
            if (file != null && file.exists() && file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length != 0) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            eachVideo(f);
                        } else if (f.exists() && f.canRead()){

                        }
                    }
                }
            }
        }

    }



}
