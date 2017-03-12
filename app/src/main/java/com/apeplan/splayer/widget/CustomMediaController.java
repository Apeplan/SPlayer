package com.apeplan.splayer.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.apeplan.splayer.R;

import io.vov.vitamio.utils.StringUtils;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by 鹤 on 2015/10/29.
 */
public class CustomMediaController extends MediaController {

    private static final int HIDEFRAM = 0;
    private static final int SHOW_PROGRESS = 2;

    private GestureDetector mGestureDetector;
    private ImageButton img_back;//返回键
    private ImageView img_Battery;//电池电量显示
    private TextView textViewTime;//时间提示
    private TextView textViewBattery;//文字显示电池
    private VideoView videoView;
    private Activity activity;
    private Context context;
    private int controllerWidth = 0;//设置mediaController高度为了使横屏时top显示在屏幕顶端

    private View mVolumeLayout;
    private ImageView mOperationBg;
    private ProgressBar mProgressBar;
    private TextView mPercent;
    private AudioManager mAudioManager;

    private SeekBar seekBarProgress;
    private boolean progress_turn;
    private int progress;

    private boolean mDragging;
    private MediaPlayerControl player;
    //最大声音
    private int mMaxVolume;
    // 当前声音
    private int mVolume = -1;
    //当前亮度
    private float mBrightness = -1f;

    //返回监听
    private OnClickListener backListener = new OnClickListener() {
        public void onClick(View v) {
            if (activity != null) {
                activity.finish();
            }
        }
    };


    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDEFRAM:
                    mVolumeLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };


    //videoview 用于对视频进行控制的等，activity为了退出
    public CustomMediaController(Context context, VideoView videoView, Activity activity) {
        super(context);
        this.context = context;
        this.videoView = videoView;
        this.activity = activity;
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        controllerWidth = wm.getDefaultDisplay().getWidth();
        mGestureDetector = new GestureDetector(context, new MyGestureListener());
    }

    @Override
    protected View makeControllerView() {
        View v = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)
        ).inflate(getResources().getIdentifier("custom_mediacontroller", "layout", getContext()
                .getPackageName()), this);
        v.setMinimumHeight(controllerWidth);
        //TOP

        img_back = (ImageButton) v.findViewById(R.id.mediacontroller_top_back);
        img_Battery = (ImageView) v.findViewById(R.id.mediacontroller_imgBattery);
        img_back.setOnClickListener(backListener);
        textViewBattery = (TextView) v.findViewById(R.id.mediacontroller_Battery);
        textViewTime = (TextView) v.findViewById(R.id.mediacontroller_time);
        seekBarProgress = (SeekBar) v.findViewById(R.id.mediacontroller_seekbar);

        //mid
        mVolumeLayout = v.findViewById(R.id.rlv_operation);
        mOperationBg = (ImageView) v.findViewById(R.id.imv_volume_bg);
        mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        mPercent = (TextView) v.findViewById(R.id.operation_tv);
        mPercent.setVisibility(View.GONE);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return v;

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("MYApp-MyMediaController-dispatchKeyEvent");
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) return true;
        // 处理手势结束
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                if (progress_turn) {
                    onFinishSeekBar();
                    progress_turn = false;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;
        // 隐藏
        myHandler.removeMessages(HIDEFRAM);
        myHandler.sendEmptyMessageDelayed(HIDEFRAM, 1);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        /**
         * 用户按下屏幕后松开（单击），由一个MotionEvent ACTION_UP 触发
         *
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        /**
         * 单击事件
         *
         * @param e
         * @return
         */
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //当收拾结束，并且是单击结束时，控制器隐藏/显示
            toggleMediaControlsVisiblity();
            return super.onSingleTapConfirmed(e);
        }

        /**
         * 用户轻触屏幕，由一个MotionEvent ACTION_DOWN 触发
         *
         * @param e
         * @return
         */
        @Override
        public boolean onDown(MotionEvent e) {
            progress = getProgress();
            return true;
        }

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
            float startX = e1.getX(), startY = e1.getY();
            float endX = e2.getRawX(), endY = e2.getRawY();

            Display disp = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            disp.getSize(size);
            int windowWidth = size.x;
            int windowHeight = size.y;
            if (Math.abs(endX - startX) + 50 < Math.abs(startY - endY)) {//上下滑动
                if (startX > windowWidth >> 1) {// 右边滑动 屏幕
                    onVolumeSlide((startY - endY) / windowHeight);
                } else {// 左边滑动
                    onBrightnessSlide((startY - endY) / windowHeight);
                }
            } else if (startX > windowWidth / 5.0 && startX < windowWidth * 4.0 / 5.0) {
                onSeekTo((endX - startX) / 20);
            } else {
                mVolumeLayout.setVisibility(GONE);
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        //双击暂停或开始
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            playOrPause();
            return true;
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
    }

    /**
     * 滑动改变播放进度
     *
     * @param percent
     */
    private void onSeekTo(float percent) {
        //计算并显示 前进后退
        if (!progress_turn) {
            onStartSeekBar();
            progress_turn = true;
        }
        int change = (int) (percent);
        if (change > 0) {
            mOperationBg.setImageResource(R.drawable.right);
        } else {
            mOperationBg.setImageResource(R.drawable.left);
        }
        mPercent.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);

        mVolumeLayout.setVisibility(View.VISIBLE);
        if (progress + change > 0) {
            if ((progress + change < 1000))
                mPercent.setText(setSeekBarChange(progress + change) + "/" + StringUtils
                        .generateTime(videoView.getDuration()));
            else
                mPercent.setText(setSeekBarChange(1000) + "/" + StringUtils.generateTime
                        (videoView.getDuration()));
        } else {
            mPercent.setText(setSeekBarChange(0) + "/" + StringUtils.generateTime(videoView
                    .getDuration()));

        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mProgressBar.setMax(mMaxVolume);
        mProgressBar.setProgress(currVolume);

        if (mVolume == -1) {
            mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            mVolumeLayout.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            mPercent.setVisibility(View.GONE);
        }

        int index = (int) (percent * mMaxVolume) + mVolume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;
//        if (index >= 10) {
//            mOperationBg.setImageResource(R.drawable.volmn_100);
//        } else if (index >= 5 && index < 10) {
//            mOperationBg.setImageResource(R.drawable.volmn_60);
//        } else if (index > 0 && index < 5) {
//            mOperationBg.setImageResource(R.drawable.volmn_30);
//        } else {
//            mOperationBg.setImageResource(R.drawable.volmn_no);
//        }
        //DecimalFormat    df   = new DecimalFormat("######0.00");
        mOperationBg.setImageResource(R.drawable.player_gesture_sound_big);
        mProgressBar.setProgress(index);
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        int currVolume = (int) (activity.getWindow().getAttributes().screenBrightness * 100);
        mProgressBar.setMax(100);
        mProgressBar.setProgress(currVolume);

        if (mBrightness < 0) {
            mBrightness = activity.getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mOperationBg.setImageResource(R.drawable.player_gesture_bright_big);
            mVolumeLayout.setVisibility(View.VISIBLE);
            mPercent.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);

        }


        WindowManager.LayoutParams wlp = activity.getWindow().getAttributes();
        wlp.screenBrightness = mBrightness + percent;
        if (wlp.screenBrightness > 1.0f) {
            wlp.screenBrightness = 1.0f;
        } else if (wlp.screenBrightness < 0.01f) {
            wlp.screenBrightness = 0.01f;
        }
        int currBrightness = (int) (activity.getWindow().getAttributes().screenBrightness * 100);
        mProgressBar.setProgress(currBrightness);
        activity.getWindow().setAttributes(wlp);

//        if (lpa.screenBrightness * 100 >= 90) {
//            mOperationBg.setImageResource(R.drawable.light_100);
//        } else if (lpa.screenBrightness * 100 >= 80 && lpa.screenBrightness * 100 < 90) {
//            mOperationBg.setImageResource(R.drawable.light_90);
//        } else if (lpa.screenBrightness * 100 >= 70 && lpa.screenBrightness * 100 < 80) {
//            mOperationBg.setImageResource(R.drawable.light_80);
//        } else if (lpa.screenBrightness * 100 >= 60 && lpa.screenBrightness * 100 < 70) {
//            mOperationBg.setImageResource(R.drawable.light_70);
//        } else if (lpa.screenBrightness * 100 >= 50 && lpa.screenBrightness * 100 < 60) {
//            mOperationBg.setImageResource(R.drawable.light_60);
//        } else if (lpa.screenBrightness * 100 >= 40 && lpa.screenBrightness * 100 < 50) {
//            mOperationBg.setImageResource(R.drawable.light_50);
//        } else if (lpa.screenBrightness * 100 >= 30 && lpa.screenBrightness * 100 < 40) {
//            mOperationBg.setImageResource(R.drawable.light_40);
//        } else if (lpa.screenBrightness * 100 >= 20 && lpa.screenBrightness * 100 < 20) {
//            mOperationBg.setImageResource(R.drawable.light_30);
//        } else if (lpa.screenBrightness * 100 >= 10 && lpa.screenBrightness * 100 < 20) {
//            mOperationBg.setImageResource(R.drawable.light_20);
//        }


    }


    public void setTime(String time) {
        if (textViewTime != null)
            textViewTime.setText(time);
    }

    //显示电量，
    public void setBattery(String stringBattery) {
        if (textViewTime != null && img_Battery != null) {
            textViewBattery.setText(stringBattery + "%");
            int battery = Integer.valueOf(stringBattery);
            if (battery < 15)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_15));
            if (battery < 30 && battery >= 15)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_15));
            if (battery < 45 && battery >= 30)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_30));
            if (battery < 60 && battery >= 45)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_45));
            if (battery < 75 && battery >= 60)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_60));
            if (battery < 90 && battery >= 75)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_75));
            if (battery > 90)
                img_Battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_90));
        }
    }

    //隐藏/显示
    private void toggleMediaControlsVisiblity() {
        if (isShowing()) {
            hide();
        } else {
            show();
        }
    }

    //播放与暂停
    private void playOrPause() {
        if (videoView != null)
            if (videoView.isPlaying()) {
                videoView.pause();
            } else {
                videoView.start();
            }
    }


}
