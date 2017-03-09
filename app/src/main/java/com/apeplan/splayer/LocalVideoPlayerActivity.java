package com.apeplan.splayer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class LocalVideoPlayerActivity extends AppCompatActivity {
    private String path = Environment.getExternalStorageDirectory() + "/oppo.mp4";
    //    private String path = Environment.getExternalStorageDirectory() + "/rmvb.rmvb";
    private RelativeLayout mOperation_layout;
    private VideoView mVideoView;
    private ImageView mImv_volume_bg;
    private AudioManager mAudioManager;


    /**
     * 最大声音
     */
    private int mMaxVolume;
    /**
     * 当前声音
     */
    private int mVolume = -1;
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
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
    private GestureDetector mGestureDetector;

    /**
     * 定时隐藏
     */
    private final VHandler mDismissHandler = new VHandler(this);
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager
                .LayoutParams.FLAG_FULLSCREEN);
        if (!LibsChecker.checkVitamioLibs(this))
            return;
        setContentView(R.layout.activity_local_video_player);

        bindViews();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        Log.d("Simon", "onCreate: 最大音量= " + mMaxVolume);
        if (path == "") {
            // Tell the user to provide a media file URL/path.
            Toast.makeText(LocalVideoPlayerActivity.this, "Please edit VideoViewDemo Activity, " +
                    "and set path variable to your media file URL/path", Toast.LENGTH_LONG).show();
            return;
        } else {
            mVideoView.setVideoPath(path);
            mVideoView.setMediaController(new MediaController(this));
            mVideoView.requestFocus();

            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    // optional need Vitamio 4.0
                    mediaPlayer.setPlaybackSpeed(1.0f);
                }
            });
        }
        mVideoView.setVideoPath(path);
        boolean playing = mVideoView.isPlaying();
        Log.d("Simon", "onCreate: playing= " + playing);
        if (!playing) {
            mVideoView.start();
        }
        Log.d("Simon", "onCreate: playing= " + mVideoView.isPlaying());
        mGestureDetector = new GestureDetector(this, new MyGestureListener());

    }

    private void bindViews() {
        mOperation_layout = (RelativeLayout) findViewById(R.id.frl_operation);
        mVideoView = (VideoView) findViewById(R.id.vov_videoView);
        mImv_volume_bg = (ImageView) findViewById(R.id.imv_volume_bg);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        int i = event.getAction() & MotionEvent.ACTION_MASK;
        switch (i) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }

        return super.onTouchEvent(event);
    }

    private void endGesture() {
        mVolume = -1;
        mBrightness = -1.0f;
        mDismissHandler.removeMessages(0);
        mDismissHandler.sendEmptyMessageDelayed(0, 500);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mVideoView != null)
            mVideoView.setVideoLayout(mLayout, 0);
        super.onConfigurationChanged(newConfig);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float oldX = e1.getX(), oldY = e1.getY();
            float rawY = e2.getRawY();
            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            if (oldX > width >> 1) {// 右边滑动
                Log.d("Simon", "onScroll: 右边滑动");
                float p = (oldY - rawY) / height;
                onVolumeSlide(p);
            } else {
                float p = (oldY - rawY) / height;
                onBrightnessSlide(p);
            }


            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        /* @Override
         public boolean onDoubleTap(MotionEvent e) {
             if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
                 mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
             else
                 mLayout++;
             if (mVideoView != null)
                 mVideoView.setVideoLayout(mLayout, 0);
             return true;
         }*/
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mLayout == VideoView.VIDEO_LAYOUT_STRETCH)
                mLayout = VideoView.VIDEO_LAYOUT_SCALE;
            else
                mLayout = VideoView.VIDEO_LAYOUT_STRETCH;
            if (mVideoView != null)
                mVideoView.setVideoLayout(mLayout, 0);
            return true;
        }
    }

    /**
     * 滑动调节音量
     *
     * @param percent 手指在屏幕上滑动的百分比
     */
    private void onVolumeSlide(float percent) {
        int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mProgressBar.setMax(mMaxVolume);
        mProgressBar.setProgress(currVolume);

        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0) {
                mVolume = 0;
            }

            mImv_volume_bg.setImageResource(R.drawable.player_gesture_sound_big);
            mOperation_layout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }
        Log.d("Simon", "onVolumeSlide: 音量= " + index);
        mProgressBar.setProgress(index);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
    }

    /**
     * 滑动调节亮度
     *
     * @param percent 手指在屏幕上滑动的百分比
     */
    private void onBrightnessSlide(float percent) {
        int currVolume = (int) (getWindow().getAttributes().screenBrightness * 100);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(currVolume);
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            }
            if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
            // 显示亮度
            mImv_volume_bg.setImageResource(R.drawable.player_gesture_bright_big);
            mOperation_layout.setVisibility(View.VISIBLE);
        }

        WindowManager.LayoutParams wlp = getWindow().getAttributes();
        wlp.screenBrightness = mBrightness + percent;
        if (wlp.screenBrightness > 1.0f) {
            wlp.screenBrightness = 1.0f;
        } else if (wlp.screenBrightness < 0.01f) {
            wlp.screenBrightness = 0.01f;
        }
        int currBrightness = (int) (getWindow().getAttributes().screenBrightness * 100);
        mProgressBar.setProgress(currBrightness);
        getWindow().setAttributes(wlp);

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
            if (null != activity) {
                activity.mOperation_layout.setVisibility(View.GONE);
            }
        }
    }

}
