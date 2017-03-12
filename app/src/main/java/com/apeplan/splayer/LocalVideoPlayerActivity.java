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

import com.apeplan.splayer.domain.MediaEntry;

import java.lang.ref.WeakReference;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class LocalVideoPlayerActivity extends AppCompatActivity {
    //    private String path = Environment.getExternalStorageDirectory() + "/oppo.mp4";
    private String path = Environment.getExternalStorageDirectory() + "/我是艺术.mp4";
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

        Bundle extras = getIntent().getExtras();
        if (null != extras) {
            MediaEntry video = (MediaEntry) extras.getSerializable("video");
            String data = video.getData();
            Log.d("Simon", "onCreate: data= " + data);
            path = data;
        }

        mVideoView.setVideoPath(path);
        mVideoView.setMediaController(new MediaController(this) {
            @Override
            public void doBack(View v) {
                finish();
            }
        });
        mVideoView.requestFocus();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // optional need Vitamio 4.0
                mediaPlayer.setPlaybackSpeed(1.0f);
            }
        });

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
        /**
         * 用户按下触摸屏，并拖动，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE触发
         *
         * @param e1
         * @param e2
         * @param distanceX
         * @param distanceY
         * @return
         */
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

        /**
         * 用户按下屏幕后松开（单击），由一个MotionEvent ACTION_UP 触发
         *
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return super.onSingleTapUp(e);
        }

        /**
         * 用户长按触摸屏，由多个MotionEvent ACTION_DOWN触发
         *
         * @param e
         */
        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }

        /**
         * 用户按下屏幕、快速移动后松开，由一个MotionEvent ACTION_DOWN,多个ACTION_MOVE,一个ACTION_UP触发
         *
         * @param e1
         * @param e2
         * @param velocityX
         * @param velocityY
         * @return
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        /**
         * 用户轻触屏幕，尚未松开或者拖动，由一个MotionEvent ACTION_DOWN 触发
         * 和onDown的区别是：没有松开或者拖动
         *
         * @param e
         */
        @Override
        public void onShowPress(MotionEvent e) {
            super.onShowPress(e);
        }

        /**
         * 用户轻触屏幕，由一个MotionEvent ACTION_DOWN 触发
         *
         * @param e
         * @return
         */
        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onContextClick(MotionEvent e) {
            return super.onContextClick(e);
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
