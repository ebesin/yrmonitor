package com.jiyouliang.fmap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.lifecycle.ViewModelStore;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.dadac.testrosbridge.RCApplication;
import com.github.mikephil.charting.data.Entry;
import com.github.onlynight.waveview.WaveView;
import com.google.gson.Gson;
import com.jilk.ros.ROSClient;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.jilk.ros.rosbridge.implementation.PublishEvent;
import com.jiyouliang.fmap.ViewModel.FanSpeedViewModel2;
import com.jiyouliang.fmap.ViewModel.GPSData;
import com.jiyouliang.fmap.ViewModel.GPSDataViewModel;
import com.jiyouliang.fmap.ViewModel.StatusViewModel;
import com.jiyouliang.fmap.bean.Status;
import com.jiyouliang.fmap.factory.InputStreamBitmapDecoderFactory;
import com.jiyouliang.fmap.factory.LargeImageView;
import com.jiyouliang.fmap.ui.BaseActivity;
import com.jiyouliang.fmap.util.DeviceUtils;
import com.jiyouliang.fmap.util.LogUtil;
import com.jiyouliang.fmap.view.map.GPSView;
import com.jiyouliang.fmap.view.map.TrafficView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class main3Activity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, GPSView.OnGPSViewClickListener {

    private static final String TAG = "MapActivity3";
    private View mBottomSheet;
    private BottomSheetBehavior<View> mBehavior;
    private boolean slideDown;//向下滑动
    private int mMaxPeekHeight;//最大高的
    private int mMinPeekHeight;//最小高度
    private int mScreenHeight;
    private int mScreenWidth;
    private View mPoiColseView;
    private GPSView mGpsView;

    private TextView mTvLocTitle;
    private TextView mTvLocation;

    private TextView slideUpInfo;


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    gpsDataViewModel.setData((GPSData) msg.obj);
                    break;
                case 2:
                    fanSpeedViewModel2.setSpeedValue((int[]) msg.obj);
                    break;
                case 3:
                    statusViewModel.setValue((Status) msg.obj);
                    break;
            }
        }
    };

    @SuppressLint("HandlerLeak")
    Handler viewhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Status status = (Status) msg.obj;
                    speed_data.setText(String.valueOf(status.getSpeed()));
                    front_wheel_angle_data.setText(String.valueOf(status.getFront_wheel_angle()));
                    yaw_angle_data.setText(String.valueOf(status.getYaw_angle()));
                    lat_data.setText(String.valueOf(status.getLat()));
                    lng_data.setText(String.valueOf(status.getLng()));
                    break;
            }
        }
    };


    CardView charge_cardview;
    CardView setArgs_cardView;
    CardView device_state_cardView;
    Button determine;
    //用来保存数据
    List<Entry> list = new ArrayList<>();

    //电池波纹
    WaveView charge_waveView;
    WaveView spray_waveView;

    //开关
    Switch aSwitch;

    //9个风机
    private final ImageView[] fans = new ImageView[8];
    private final ImageView[] spray_heads = new ImageView[8];
    private final int[] speed = new int[8];


    //控制旋转文件
    Animation animation_0_speed;
    Animation animation_20_speed;
    Animation animation_40_speed;
    Animation animation_60_speed;
    Animation animation_80_speed;
    Animation animation_100_speed;

    AnimationDrawable[] animationDrawable_0_speed = new AnimationDrawable[8];
    AnimationDrawable[] animationDrawable_20_speed = new AnimationDrawable[8];
    AnimationDrawable[] animationDrawable_40_speed = new AnimationDrawable[8];
    AnimationDrawable[] animationDrawable_60_speed = new AnimationDrawable[8];
    AnimationDrawable[] animationDrawable_80_speed = new AnimationDrawable[8];

    LinearInterpolator linearInterpolator;

    Gson gson = new Gson();

    //地图
    LargeImageView largeImageView;

    /**
     * 数据
     */
    private GPSDataViewModel gpsDataViewModel;
    private FanSpeedViewModel2 fanSpeedViewModel2;
    private StatusViewModel statusViewModel;

    //dialog
    AlertDialog charge_dialog;
    AlertDialog args_dialog;
    AlertDialog status_dialog;

    //dialog控件
    TextView speed_data;
    TextView front_wheel_angle_data;
    TextView yaw_angle_data;
    TextView lat_data;
    TextView lng_data;


    //ros通信
    ROSBridgeClient client;
    String ip = "192.168.1.176";   //ros的 IP
    String port = "9090";
    boolean isConn = false;
    Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            connect(ip, port);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map3);
        EventBus.getDefault().register(this);
        initView(savedInstanceState);
        initData();
        setListener();
    }

    private void initView(Bundle savedInstanceState) {
        //建立连接,订阅topic
        new Thread(connectRunnable).start();
        largeImageView = findViewById(R.id.largeImage);
        try {
            largeImageView.setImage(new InputStreamBitmapDecoderFactory(getAssets().open("map2.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBottomSheet = findViewById(R.id.poi_detail_bottom);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mPoiColseView = findViewById(R.id.iv_close);
        mGpsView = (GPSView) findViewById(R.id.gps_view);
        mBottomSheet.setVisibility(View.GONE);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mTvLocTitle = (TextView) findViewById(R.id.tv_title);
        mTvLocation = (TextView) findViewById(R.id.tv_my_loc);
        slideUpInfo = findViewById(R.id.slide_up_info);
        //数据显示控件
        charge_cardview = findViewById(R.id.charge_cardview);
        charge_cardview.setOnClickListener(this);
        setArgs_cardView = findViewById(R.id.setArgs_cardView);
        setArgs_cardView.setOnClickListener(this);
        device_state_cardView = findViewById(R.id.device_state_cardView);
        device_state_cardView.setOnClickListener(this);
        charge_waveView = findViewById(R.id.charge_waveView);
        charge_waveView.start();
        spray_waveView = findViewById(R.id.spray_waveView);
        spray_waveView.start();
        aSwitch = findViewById(R.id.switch1);
        aSwitch.setOnCheckedChangeListener(this);
        aSwitch.setOnClickListener(this);
//        setChart();
        Arrays.fill(speed, 0);
        //风机与旋转
        fans[0] = findViewById(R.id.fan_no_1);
        fans[1] = findViewById(R.id.fan_no_2);
        fans[2] = findViewById(R.id.fan_no_3);
        fans[3] = findViewById(R.id.fan_no_4);
        fans[4] = findViewById(R.id.fan_no_5);
        fans[5] = findViewById(R.id.fan_no_6);
        fans[6] = findViewById(R.id.fan_no_7);
        fans[7] = findViewById(R.id.fan_no_8);

        spray_heads[0] = findViewById(R.id.spray_head_1);
        spray_heads[1] = findViewById(R.id.spray_head_2);
        spray_heads[2] = findViewById(R.id.spray_head_3);
        spray_heads[3] = findViewById(R.id.spray_head_4);
        spray_heads[4] = findViewById(R.id.spray_head_5);
        spray_heads[5] = findViewById(R.id.spray_head_6);
        spray_heads[6] = findViewById(R.id.spray_head_7);
        spray_heads[7] = findViewById(R.id.spray_head_8);

        animation_0_speed = AnimationUtils.loadAnimation(this, R.anim.image_0_rotation);
        animation_20_speed = AnimationUtils.loadAnimation(this, R.anim.image_20_rotation);
        animation_40_speed = AnimationUtils.loadAnimation(this, R.anim.image_40_rotation);
        animation_60_speed = AnimationUtils.loadAnimation(this, R.anim.image_60_rotation);
        animation_80_speed = AnimationUtils.loadAnimation(this, R.anim.image_80_rotation);
        animation_100_speed = AnimationUtils.loadAnimation(this, R.anim.image_100_rotation);


        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_20_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_20_round);
        }

        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_40_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_40_round);
        }

        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_60_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_60_round);
        }

        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
            animationDrawable_80_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_80_round);
        }

        linearInterpolator = new LinearInterpolator();
        setFan();

        setBottomSheet();
        setDialog();
    }


    private void initData() {

//        设置gps数据改变时的事件
        gpsDataViewModel = ViewModelProviders.of(this).get((GPSDataViewModel.class));
        gpsDataViewModel.getGpsData().observe(this, new Observer<GPSData>() {
            @Override
            public void onChanged(@Nullable GPSData gpsData) {

            }
        });
        fanSpeedViewModel2 = ViewModelProviders.of(this).get(FanSpeedViewModel2.class);
        fanSpeedViewModel2.getSpeed().observe(this, new Observer<int[]>() {
            @Override
            public void onChanged(@Nullable int[] ints) {
                changeFromSpeed(ints);
            }
        });
        setSpeed();

        statusViewModel = ViewModelProviders.of(this).get(StatusViewModel.class);
        statusViewModel.getStatusMutableLiveData().observe(this, new Observer<Status>() {
            @Override
            public void onChanged(@Nullable Status status) {
                Message message = new Message();
                message.what = 1;
                message.obj = status;
                viewhandler.sendMessage(message);
            }
        });

    }

    private void setSpeed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int[] temp = new int[8];
                    for (int i = 0; i < fanSpeedViewModel2.getSpeedValue().length; i++) {
                        temp[i] = (fanSpeedViewModel2.getSpeedValue()[i] + 20) % 100;
                    }
                    Message message = new Message();
                    message.what = 2;
                    message.obj = temp;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void setListener() {
        mGpsView.setOnGPSViewClickListener((GPSView.OnGPSViewClickListener) this);
        mBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            private float lastSlide;//上次slideOffset
            private float currSlide;//当前slideOffset


            //BottomSheet状态改变回调
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    //展开
                    case BottomSheetBehavior.STATE_EXPANDED:
                        log("STATE_EXPANDED");
                        smoothSlideUpMap();
                        break;
                    //折叠
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        log("STATE_COLLAPSED");
                        /*if (slideDown) {
                            maxMapView();
                            slideDown = false;
                        }*/
                        slideDown = false;
                        break;
                    //隐藏
                    case BottomSheetBehavior.STATE_HIDDEN:
                        log("STATE_HIDDEN");
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        //拖拽
                        log("STATE_DRAGGING");
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        //结束：释放
                        log("STATE_SETTLING");
                        break;
                }
            }

            /**
             * BottomSheet滑动回调
             */

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                currSlide = slideOffset;
                log("onSlide:slideOffset=" + slideOffset + ",getBottom=" + bottomSheet.getBottom() + ",currSlide=" + currSlide + ",lastSlide=" + lastSlide);
                if (slideOffset > 0) {
                    // > 0:向上拖动
                    if (slideOffset < 1) {

                    }
                    if (currSlide - lastSlide > 0) {
                        log(">>>>>向上滑动");
                        slideDown = false;

                    } else if (currSlide - lastSlide < 0) {
                        log("<<<<<向下滑动");
                        //smoothSlideDownMap(slideOffset);
                        if (!slideDown) {
                            smoothSlideDownMap();
                        }
                    }
                } else if (slideOffset == 0) {
                    //滑动到COLLAPSED状态

                } else if (slideOffset < 0) {
                    //从COLLAPSED向HIDDEN状态滑动，此处禁止BottomSheet隐藏
                    //setHideable(false)禁止Behavior执行：可以实现禁止向下滑动消失
                    mBehavior.setHideable(false);
                }
                lastSlide = currSlide;
            }
        });
    }

    public void smoothSlideDownMap() {
        ViewGroup.LayoutParams lp = largeImageView.getLayoutParams();
        lp.height = mScreenHeight;
        largeImageView.setLayoutParams(lp);
        slideDown = true;
    }

    /**
     * @Function: 建立连接
     * @Return:
     */
    public void connect(String ip, String port) {

        client = new ROSBridgeClient("ws://" + ip + ":" + port);
        boolean conneSucc = client.connect(new ROSClient.ConnectionStatusListener() {
            @Override
            public void onConnect() {
                client.setDebug(true);
                ((RCApplication) getApplication()).setRosClient(client);
                isConn = true;
                publish();
                Log.d("dwayne", "Connect ROS success");
            }

            @Override
            public void onDisconnect(boolean normal, String reason, int code) {
                isConn = false;
                Log.d("dwayne", "ROS disconnect");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
                isConn = false;
                Log.d("dwayne", "ROS communication error");
            }
        });
    }

    //订阅topic
    private void publish() {
        String msg1 = "{\"op\":\"subscribe\",\"topic\":\"/chatter\"}";
        client.send(msg1);
    }

    //发送数据到ROS端
    private void sendData(String data) {
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/dwayne\", \"msg\": { \"data\": \"" + data + " \" }}";
        //        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
        //                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        client.send(msg1);
    }


    public void onEvent(@NonNull final PublishEvent event) {
        Log.i("dwayne2", event.msg);
        if ("/chatter".equals(event.name)) {
            Status status = new Gson().fromJson(event.msg, Status.class);
            Message message = new Message();
            message.what = 3;
            message.obj = status;
            handler.sendMessage(message);
        }
    }


    private void parseChatterTopic(PublishEvent event) {
        Log.d("event2", "receive2");
        Log.i("dwayne2", event.msg);

    }

    @Override
    protected void onDestroy() {
        if (isConn) {
            client.disconnect();
        }
        super.onDestroy();
    }

    private void setFan() {
        animation_0_speed.setInterpolator(linearInterpolator);
        animation_20_speed.setInterpolator(linearInterpolator);
        animation_40_speed.setInterpolator(linearInterpolator);
        animation_60_speed.setInterpolator(linearInterpolator);
        animation_80_speed.setInterpolator(linearInterpolator);
        animation_100_speed.setInterpolator(linearInterpolator);
    }

    public void changeFromSpeed(int[] ints) {
        for (int i = 0; i < fans.length; i++) {
            switch (ints[i]) {
                case 20:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_20));
                    fans[i].startAnimation(animation_20_speed);
                    spray_heads[i].setImageDrawable(animationDrawable_20_speed[i]);
                    animationDrawable_20_speed[i].start();
                    break;
                case 40:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_40));
                    fans[i].startAnimation(animation_40_speed);
                    spray_heads[i].setImageDrawable(animationDrawable_40_speed[i]);
                    animationDrawable_40_speed[i].start();
                    break;
                case 60:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_60));
                    fans[i].startAnimation(animation_60_speed);
                    spray_heads[i].setImageDrawable(animationDrawable_60_speed[i]);
                    animationDrawable_60_speed[i].start();
                    break;
                case 80:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_80));
                    fans[i].startAnimation(animation_80_speed);
                    spray_heads[i].setImageDrawable(animationDrawable_80_speed[i]);
                    animationDrawable_80_speed[i].start();
                    break;
                default:
                    fans[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_fan_20));
                    spray_heads[i].startAnimation(animation_0_speed);
                    spray_heads[i].setImageDrawable(animationDrawable_80_speed[i]);
                    animationDrawable_80_speed[i].start();
            }
        }
    }

    public void showPoiDetail(String locTitle, String locInfo) {
        mGpsView.setVisibility(View.VISIBLE);
        mBottomSheet.setVisibility(View.VISIBLE);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mTvLocTitle.setText(locTitle);
        mTvLocation.setText(locInfo);

        int poiTaxiHeight = getResources().getDimensionPixelSize(R.dimen.setting_item_large_height);

        mBehavior.setHideable(true);
        mBehavior.setPeekHeight(mMinPeekHeight + poiTaxiHeight);

    }

    /**
     * 设置底部POI详细BottomSheet
     */
    private void setBottomSheet() {
        mMinPeekHeight = mBehavior.getPeekHeight();
        //虚拟键盘高度
        int navigationHeight = DeviceUtils.getNavigationBarHeight(this);
        //加上虚拟键盘高度，避免被遮挡
//        mBehavior.setPeekHeight(mMinPeekHeight + navigationHeight);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (null == wm) {
            LogUtil.e(TAG, "获取WindowManager失败:" + wm);
            return;
        }
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        //屏幕高度5/7
        mScreenHeight = point.y;
        mScreenWidth = point.x;
        //设置bottomsheet高度为屏幕 5/7
        int height = mScreenHeight * 5 / 7;
        mMaxPeekHeight = height;
        ViewGroup.LayoutParams params = mBottomSheet.getLayoutParams();
        params.height = height;
    }


    public void onPoiDetailCollapsed() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_chevrons_up);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        slideUpInfo.setCompoundDrawables(drawable, null, drawable, null);
        slideUpInfo.setText("上滑查看详细信息");
        mPoiColseView.setVisibility(View.VISIBLE);
    }


    public void onPoiDetailExpanded() {
        //BottomSheet展开：隐藏头部搜索、显示反馈、隐藏右边侧边栏

    }

    public void smoothSlideUpMap() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_chevrons_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        slideUpInfo.setCompoundDrawables(drawable, null, drawable, null);
        slideUpInfo.setText("下滑隐藏");
        ViewGroup.LayoutParams lp = largeImageView.getLayoutParams();
        lp.height = mScreenHeight * 2 / 7;
        largeImageView.setLayoutParams(lp);
    }

    private void log(String msg) {
        LogUtil.d(TAG, msg);
    }

    @Override
    public void onClick(View v) {
        if (v == aSwitch) {
            final boolean ischeck = aSwitch.isChecked();
            if (!ischeck) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_prompt);   //设置图标
                builder.setTitle("确认吗");                //标题
                builder.setMessage("确定要关闭设备吗");       //设置内容
                builder.setCancelable(false);
                //确认按钮监听事件
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();      //取消显示(关闭)对话框
                    }
                });

                //忽略按钮监听事件
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        aSwitch.setChecked(!ischeck);
                        dialog.dismiss();      //取消显示(关闭)对话框
                    }
                });
                builder.create().show();         //创建并显示对话框
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_prompt);   //设置图标
                builder.setTitle("确认吗");                //标题
                builder.setMessage("确定要启动设备吗");       //设置内容
                builder.setCancelable(false);
                //确认按钮监听事件
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();      //取消显示(关闭)对话框
                    }
                });

                //忽略按钮监听事件
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        aSwitch.setChecked(!ischeck);
                        dialog.dismiss();      //取消显示(关闭)对话框
                    }
                });
                builder.create().show();         //创建并显示对话框
            }
        }

        if (v == charge_cardview) {
            charge_dialog.show();         //创建并显示对话框
        }
        if (v == setArgs_cardView) {
            args_dialog.show();
        }
        if (v == device_state_cardView) {
            status_dialog.show();
        }
    }

    private void setDialog() {
        /**
         * status dialog
         */
        AlertDialog.Builder status_builder = new AlertDialog.Builder(this);  //先得到构造器
        status_builder.setCancelable(true);           //将对话框以外的区域设置成无法点击
        // 载入自定义布局
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.device_detail_layout, null);
        status_builder.setView(layout);

        yaw_angle_data = layout.findViewById(R.id.yaw_angle_data);
        speed_data = layout.findViewById(R.id.speed_data);
        lat_data = layout.findViewById(R.id.lat_data);
        lng_data = layout.findViewById(R.id.lng_data);
        front_wheel_angle_data = layout.findViewById(R.id.front_wheel_angle_data);

        // 设置确认按钮
        status_builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();      //取消显示(关闭)对话框
            }
        });

        // 设置取消按钮
        status_builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();      //取消显示(关闭)对话框
            }
        });
        status_dialog = status_builder.create();

        /**
         * charge dialog
         */
        AlertDialog.Builder charge_builder = new AlertDialog.Builder(this);  //先得到构造器
        charge_builder.setCancelable(true);           //将对话框以外的区域设置成无法点击
        // 载入自定义布局
        LayoutInflater inflater2 = getLayoutInflater();
        View layout2 = inflater2.inflate(R.layout.charge_detail_layout, null);
        charge_builder.setView(layout2);

        // 设置确认按钮
        charge_builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();      //取消显示(关闭)对话框
            }
        });
        charge_dialog = charge_builder.create();

        /**
         * args dialog
         */
        AlertDialog.Builder args_builder = new AlertDialog.Builder(this);  //先得到构造器
        args_builder.setCancelable(true);           //将对话框以外的区域设置成无法点击
        // 载入自定义布局
        LayoutInflater inflater3 = getLayoutInflater();
        View layout3 = inflater3.inflate(R.layout.set_args_layout2, null);
        args_builder.setView(layout3);

        // 设置确认按钮
        args_builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();      //取消显示(关闭)对话框
            }
        });
        // 设置取消按钮
        args_builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();      //取消显示(关闭)对话框
            }
        });
        args_dialog = args_builder.create();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    @Override
    public void onGPSClick() {
        if (mBottomSheet.getVisibility() == View.GONE || mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            showPoiDetail("机器人的位置", String.format("在江苏大学附近"));

        } else {
            mTvLocTitle.setText("机器人的位置");
            mTvLocation.setText(String.format("在江苏大学附近"));
        }
    }
}
