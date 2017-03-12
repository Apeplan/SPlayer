package com.apeplan.splayer.frame;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.apeplan.splayer.R;
import com.apeplan.splayer.widget.StateView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public abstract class BasicActivity extends AppCompatActivity implements View.OnClickListener {

    private StateView mStateView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getLayoutId());
        if (isStatusBarTranslucent()) {
            setStatusBarTranslucent();
        }
        initView(savedInstanceState);
        initEventAndData();
        mStateView = getLoadingView();
    }
    /**
     * 返回页面的布局资源ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化控件
     */
    protected abstract void initView(Bundle savedInstanceState);
    /**
     * 初始化事件，和数据
     */
    protected abstract void initEventAndData();

    /**
     * 返回加载状态View
     *
     * @return
     */
    protected abstract StateView getLoadingView();

    /**
     * 设置通用的 ToolBar
     *
     * @param toolbar
     * @param title
     */
    protected void setCommonBackToolBack(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new Toolbar.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // TODO:适配4.4
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected void setStatusBarTranslucent() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            // 激活状态栏
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintResource(R.color.colorPrimaryDark);
        }

    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 状态栏是否设置为透明
     */
    protected boolean isStatusBarTranslucent() {
        return false;
    }

    /**
     * 跳转到另一个 Activity
     *
     * @param aClass 要跳转页面的 Class
     */
    protected void startIntent(Class aClass) {
        startIntent(aClass, null);
    }

    /**
     * 跳转到另一个 Activity，并携带数据
     *
     * @param aClass 要跳转页面的 Class
     * @param bundle 携带的数据
     */
    protected void startIntent(Class aClass, Bundle bundle) {
        Intent intent = new Intent(this, aClass);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 跳转到另一个 Activity，携带数据，并且需要接收到返回信息
     *
     * @param aClass      要跳转页面的 Class
     * @param bundle      携带的数据
     * @param requestCode 请求 code
     */
    protected void startForResultIntent(Class aClass, Bundle bundle, int requestCode) {
        Intent intent = new Intent(this, aClass);
        if (null != bundle) {
            intent.putExtras(bundle);
        }
        startActivityForResult(intent, requestCode);
    }

    /**
     * 返回 携带的数据
     *
     * @return
     */
    protected Bundle getBundle() {
        return getIntent().getExtras();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    /**
     * 显示正在加载页面
     *
     * @param message
     */
    public void showLoading(String message) {
        if (null == mStateView) {
            throw new IllegalArgumentException("You must return a right target view for loading");
        }
        if (!TextUtils.isEmpty(message)) {
            mStateView.setTitle(message);
        }
        mStateView.setState(StateView.STATE_LOADING);
    }

    /**
     * 显示内容页面
     */
    public void showContent() {
        if (null == mStateView) {
            throw new IllegalArgumentException("You must return a right target view for loading");
        }

        mStateView.setState(StateView.STATE_CONTENT);
    }

    /**
     * 显示空数据页面
     *
     * @param message         提示信息
     * @param onClickListener 是否支持重新加载
     */
    public void showEmtry(String message, View.OnClickListener onClickListener) {
        if (null == mStateView) {
            throw new IllegalArgumentException("You must return a right target view for loading");
        }

        mStateView.setState(StateView.STATE_EMPTY);
        if (!TextUtils.isEmpty(message)) {
            mStateView.setTitle(message);
        }
        if (null != onClickListener) {
            mStateView.setOnClickListener(onClickListener);
        }
    }

    /**
     * 显示错误页面
     *
     * @param error           提示信息
     * @param onClickListener 是否支持重新加载
     */
    public void showError(String error, View.OnClickListener onClickListener) {
        if (null == mStateView) {
            throw new IllegalArgumentException("You must return a right target view for loading");
        }

        mStateView.setState(StateView.STATE_ERROR);
        if (!TextUtils.isEmpty(error)) {
            mStateView.setTitle(error);
        }
        if (null != onClickListener) {
            mStateView.setOnClickListener(onClickListener);
        }
    }

    /**
     * 显示网络错误页面
     *
     * @param error
     * @param onClickListener 是否支持重新加载
     */
    public void showNetworkError(String error, View.OnClickListener onClickListener) {
        if (null == mStateView) {
            throw new IllegalArgumentException("You must return a right target view for loading");
        }

        mStateView.setState(StateView.STATE_ERROR);
        if (!TextUtils.isEmpty(error)) {
            mStateView.setTitle(error);
        }
        mStateView.setIcon(R.drawable.state_net_error);
        if (null != onClickListener) {
            mStateView.setOnClickListener(onClickListener);
        }

    }
}
