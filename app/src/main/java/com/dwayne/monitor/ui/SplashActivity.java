package com.dwayne.monitor.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dwayne.monitor.MainActivity;
import com.dwayne.monitor.R;
import com.dwayne.monitor.mqtt.MqttClient;
import com.dwayne.monitor.util.PermissionUtil;
import com.dwayne.monitor.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 启动页
 */
public class SplashActivity extends BaseActivity {

    /**
     * 运行时权限请求码
     */
    private final int REQ_CODE_PERMISSIONS = 1;

    /**
     * 动画时长
     */
    private final long DURATION = 800;
    private ValueAnimator mAnimator;
    /*private ImageView mIvTop;
    private ImageView mIvCenter;
    private ImageView mIvBottom;*/
    private boolean hasOnResumeChecked;
    MqttClient mqttClient;
    Context context;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    private void initView() {
        context = this;
        mqttClient = MqttClient.getInstance(this);
        initAnimator();
        mqttClient.setOnConnectListener(new MqttClient.OnConnectListener() {
            @Override
            public void OnConnectSuccess() {
                mAnimator.end();
            }

            @Override
            public void OnConnectFail(String failMsg) {
                Message message = new Message();
                message.what = 1;
                message.obj = failMsg;
                handler.sendMessage(message);
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case 1:
                        ToastUtil.showToast(context, (String) msg.obj);
                }
                return false;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void initAnimator() {
        mAnimator = ValueAnimator.ofFloat(0, 80);
        mAnimator.setDuration(DURATION);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                mqttClient.connectMQTT();
            }

            /**
             * 动画结束,进入主页MapActivity
             *
             * @param animation
             */
            @Override
            public void onAnimationEnd(Animator animation) {
                showMapPage();
            }
        });
        mAnimator.start();
    }


    /**
     * 页面显示后才检查权限
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!hasOnResumeChecked) {
            checkPermissions();
            hasOnResumeChecked = true;
        }
    }

    private void checkPermissions() {
        // android 6.0以上检查运行时权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = PermissionUtil.getNoGrantedPermissions(this);
            if (permissions != null && permissions.length > 0) {
                // 有未授予权限,动态申请
                PermissionUtil.requestPermissions(permissions, this, REQ_CODE_PERMISSIONS);
            }else{
                initView();
            }
        }else{
            initView();
        }
    }


    /**
     * 关闭动画,释放动画资源
     */
    @Override
    protected void onStop() {
        super.onStop();
        if (mAnimator != null) {
            if (mAnimator.isRunning()) {
                mAnimator.cancel();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAnimator != null) {
            mAnimator = null;
        }
    }

    /**
     * 进入主页
     */
    private void showMapPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CODE_PERMISSIONS) {
            List<String> noGrantedPermissions = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    noGrantedPermissions.add(permissions[i]);
                }
            }
            // 有未授予权限,重新申请
            if (noGrantedPermissions.size() > 0) {
                checkPermissions();
            } else {
                showMapPage();
            }

        }

    }
}
