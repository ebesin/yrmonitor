package com.jiyouliang.fmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.common.CatLoadingView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Toolbar toolbar;
    CardView agriculture_robot1;
    CardView agriculture_robot2;
    Button testConn;
    CatLoadingView mView;
    Intent robot1_intent;
    Intent robot2_intent;
    Intent testIntent;
    Thread thread;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mView.show(getSupportFragmentManager(), "");
                    break;
                case 2:
                    mView.dismiss();
                    startActivity(robot1_intent);
                    break;
                case 3:
                    mView.dismiss();
                    startActivity(robot2_intent);
                    break;
                case 4:
                    mView.dismiss();
                    startActivity(testIntent);
                    break;
            }
        }
    };

    Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            Message message1 = new Message();
            message1.what = 1;
            handler.sendMessage(message1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message2 = new Message();
            message2.what = 2;
            handler.sendMessage(message2);
        }
    };

    Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            Message message1 = new Message();
            message1.what = 1;
            handler.sendMessage(message1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message2 = new Message();
            message2.what = 3;
            handler.sendMessage(message2);
        }
    };

    Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            Message message1 = new Message();
            message1.what = 1;
            handler.sendMessage(message1);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message message2 = new Message();
            message2.what = 4;
            handler.sendMessage(message2);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        initView();

    }

    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        agriculture_robot1 = findViewById(R.id.agriculture_robot1);
        agriculture_robot1.setOnClickListener(this);
        agriculture_robot2 = findViewById(R.id.agriculture_robot2);
        agriculture_robot2.setOnClickListener(this);
        testConn = findViewById(R.id.testConn);
        testConn.setOnClickListener(this);
        setToolbar();
        mView = new CatLoadingView();
        robot1_intent = new Intent(this, MapActivity.class);
        robot2_intent = new Intent(this, main3Activity.class);
        testIntent = new Intent(this, RosBridgeActivity.class);
    }

    /**
     * 设置toolbar样式
     */
    private void setToolbar() {
        // 设置navigation button
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_menu1, null));
// 设置溢出菜单的图标
        toolbar.setOverflowIcon(getResources().getDrawable(R.drawable.ic_more, null));
// 设置Menu
        toolbar.inflateMenu(R.menu.toolbar_menu);

// 设置Navigation Button监听
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                finish();
            }
        });

// 设置Menu监听
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.agriculture_robot1:
                thread = new Thread(runnable1);
                thread.start();
                break;
            case R.id.agriculture_robot2:
                thread = new Thread(runnable2);
                thread.start();
                break;
            case R.id.testConn:
                thread = new Thread(runnable3);
                thread.start();
                break;
            default:

        }
    }
}