package com.apeplan.splayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.apeplan.splayer.domain.MediaEntry;
import com.apeplan.splayer.widget.CustomMediaController;

import java.lang.ref.WeakReference;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class LocalVideoPlayerActivity extends AppCompatActivity {
    //    private String path = Environment.getExternalStorageDirectory() + "/oppo.mp4";
    private String path = Environment.getExternalStorageDirectory() + "/我是艺术.mp4";
    private VideoView mVideoView;
    private AudioManager mAudioManager;

    /**
     * 当前缩放模式
     * <p>
     * public static final int VIDEO_LAYOUT_ORIGIN:缩放参数，原始画面大小。常量值：0
     * <p>
     * public static final int VIDEO_LAYOUT_SCALE:缩放参数，画面全屏。常量值：1
     * <p>
     * public static final int VIDEO_LAYOUT_STRETCH:缩放参数，画面拉伸。常量值：2
     * <p>
     * public static final int VIDEO_LAYOUT_ZOOM:缩放参数，画面裁剪。常量值：3
     */
//    private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
    private int mLayout = VideoView.VIDEO_LAYOUT_SCALE;

    private MediaController mMediaController;
    private CustomMediaController mCustomMediaController;
    private static final int TIME = 0;
    private static final int BATTERY = 1;
    /**
     * 定时隐藏
     */
    private final VHandler mDismissHandler = new VHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_local_video_player);

        bindViews();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            MediaEntry video = (MediaEntry) extras.getSerializable("video");
            String data = video.getData();
            path = data;
        }

        mVideoView.setVideoPath(path);
        mMediaController = new MediaController(this);
        mCustomMediaController = new CustomMediaController(this, mVideoView, this);
        mMediaController.show(5000);

        mVideoView.setMediaController(mCustomMediaController);

        mVideoView.requestFocus();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // optional need Vitamio 4.0
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });

        registerBoradcastReceiver();
    }

    private void bindViews() {
        mVideoView = (VideoView) findViewById(R.id.vov_videoView);
    }

    public void registerBoradcastReceiver() {
        //注册电量广播监听
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryBroadcastReceiver, intentFilter);

    }
    private BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                //获取当前电量
                int level = intent.getIntExtra("level", 0);
                //电量的总刻度
                int scale = intent.getIntExtra("scale", 100);
                //把它转成百分比
                //tv.setText("电池电量为"+((level*100)/scale)+"%");
                Message msg = new Message();
                msg.obj = (level*100)/scale+"";
                msg.what = BATTERY;
                mDismissHandler.sendMessage(msg);
            }
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mVideoView != null)
            mVideoView.setVideoLayout(mLayout, 0);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(batteryBroadcastReceiver);
        } catch (IllegalArgumentException ex) {

        }
    }

    private static class VHandler extends Handler {
        WeakReference<Activity> mWeakReference;

        public VHandler(Activity activity) {
            mWeakReference = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LocalVideoPlayerActivity activity = (LocalVideoPlayerActivity) mWeakReference.get();
            switch (msg.what) {
                case TIME:
                    activity.mCustomMediaController.setTime(msg.obj.toString());
                    break;
                case BATTERY:
                    activity.mCustomMediaController.setBattery(msg.obj.toString());
                    break;
            }
        }
    }

}
