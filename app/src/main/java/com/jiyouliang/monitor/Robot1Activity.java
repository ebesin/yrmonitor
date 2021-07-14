package com.jiyouliang.monitor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
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
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.AMapGestureListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Poi;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.InputtipsQuery;
import com.amap.api.services.help.Tip;
import com.dadac.testrosbridge.RCApplication;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.onlynight.waveview.WaveView;
import com.google.gson.Gson;
import com.jilk.ros.rosbridge.ROSBridgeClient;
import com.jilk.ros.rosbridge.implementation.PublishEvent;
import com.jiyouliang.monitor.ViewModel.BatteryViewModel;
import com.jiyouliang.monitor.ViewModel.FanSpeedViewModel;
import com.jiyouliang.monitor.ViewModel.GPSData;
import com.jiyouliang.monitor.ViewModel.GPSDataViewModel;
import com.jiyouliang.monitor.ViewModel.StatusViewModel;
import com.jiyouliang.monitor.bean.Angular;
import com.jiyouliang.monitor.bean.Battery;
import com.jiyouliang.monitor.bean.Control;
import com.jiyouliang.monitor.bean.Linear;
import com.jiyouliang.monitor.bean.Spray;
import com.jiyouliang.monitor.bean.Status;
import com.jiyouliang.monitor.bean.Twist;
import com.jiyouliang.monitor.harware.SensorEventHelper;
import com.jiyouliang.monitor.ui.BaseActivity;

import com.jiyouliang.monitor.ui.user.UserActivity;
import com.jiyouliang.monitor.util.DeviceUtils;
import com.jiyouliang.monitor.util.InputMethodUtils;
import com.jiyouliang.monitor.util.LogUtil;
import com.jiyouliang.monitor.util.MyAMapUtils;
import com.jiyouliang.monitor.view.base.MapViewInterface;
import com.jiyouliang.monitor.view.map.GPSView;
import com.jiyouliang.monitor.view.map.MapHeaderView;
import com.jiyouliang.monitor.view.map.NearbySearchView;
import com.jiyouliang.monitor.view.map.PoiDetailBottomView;
import com.jiyouliang.monitor.view.map.TrafficView;
import com.jiyouliang.monitor.view.widget.OnItemClickListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class Robot1Activity extends BaseActivity implements GPSView.OnGPSViewClickListener, NearbySearchView.OnNearbySearchViewClickListener, AMapGestureListener, AMapLocationListener, LocationSource, TrafficView.OnTrafficChangeListener, View.OnClickListener, MapViewInterface, PoiDetailBottomView.OnPoiDetailBottomClickListener, AMap.OnPOIClickListener, TextWatcher, Inputtips.InputtipsListener, MapHeaderView.OnMapHeaderViewClickListener, OnItemClickListener, GeocodeSearch.OnGeocodeSearchListener, CompoundButton.OnCheckedChangeListener{
    private static final String TAG = "MapActivity";
    /**
     * 首次进入申请定位、sd卡权限
     */
    private static final int REQ_CODE_INIT = 0;
    private static final int REQ_CODE_FINE_LOCATION = 1;
    private static final int REQ_CODE_STORAGE = 2;
    private TextureMapView mMapView;
    private AMap aMap;
    private UiSettings mUiSettings;

    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private GPSView mGpsView;


    private static boolean mFirstLocation = true;//第一次定位
    private int mCurrentGpsState = STATE_UNLOCKED;//当前定位状态
    private static final int STATE_UNLOCKED = 0;//未定位状态，默认状态
    private static final int STATE_LOCKED = 1;//定位状态
    private static final int STATE_ROTATE = 2;//根据地图方向旋转状态
    private int mZoomLevel = 20;//地图缩放级别，最大缩放级别为20
    private LatLng mLatLng;//当前定位经纬度
    private LatLng mClickPoiLatLng;//当前点击的poi经纬度
    private static long mAnimDuartion = 500L;//地图动效时长
    private int mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;//地图状态类型
    private SensorEventHelper mSensorHelper;
    private Marker mLocMarker;//自定义小蓝点
    private Circle mCircle;
    private static final int STROKE_COLOR = Color.argb(240, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    private OnLocationChangedListener mLocationListener;
    private float mAccuracy;
    private boolean mMoveToCenter = true;//是否可以移动地图到定位点
    private TrafficView mTrafficView;
    private View mBottomSheet;
    private BottomSheetBehavior<View> mBehavior;
    private int mMaxPeekHeight;//最大高的
    private int mMinPeekHeight;//最小高度
    private View mPoiColseView;


    private int mPadding;
    //poi detail动画时长
    private static final int DURATION = 100;
    private View mGspContainer;
    private float mTransY;
    private float mOriginalGspY;
    private int moveY;
    private int[] mBottomSheetLoc = new int[2];
    private String mPoiName;
    private TextView mTvLocation;
    private NearbySearchView nearby;
    private TextView slideUpInfo;

    private int mScreenHeight;
    private int mScreenWidth;
    private boolean isMinMap; //缩小显示地图
    private boolean onScrolling;//正在滑动地图
    private MyLocationStyle mLocationStyle;
    private boolean slideDown;//向下滑动
    private LinearLayout mShareContainer;


    private AMapLocation mAmapLocation;
    // 分享url到微信图片大小
    private static final int THUMB_SIZE = 150;
    private ImageButton mImgBtnBack;
    private TextView mTvLocTitle;
    // 当前是否正在处理POI点击
    private boolean isPoiClick;
    private TextView mTvRoute;
    private LinearLayout mLLSearchContainer;
    // 搜索结果存储
    private List<Tip> mSearchData = new ArrayList<>();

    /**
     * 当前地图模式
     */
    private MapMode mMapMode = MapMode.NORMAL;
    private RecyclerView mRecycleViewSearch;
    private ImageView mIvLeftSearch;
    private EditText mEtSearchTip;
    private SearchAdapter mSearchAdapter;
    private String mCity;
    private ProgressBar mSearchProgressBar;
    private LocationManager mLocMgr;
    private LatLng latLonPoint;


    /**
     * 数据
     */
    private GPSDataViewModel gpsDataViewModel;
    private FanSpeedViewModel fanSpeedViewModel;
    GeocodeSearch geocodeSearch;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    gpsDataViewModel.setData((GPSData) msg.obj);
                    break;
                case 2:
                    Log.d("handler","change");
                    fanSpeedViewModel.setSpeedValue((int[]) msg.obj);
                    break;
                case 3:
                    statusViewModel.setValue((Status) msg.obj);
                    break;
                case 4:
                    batteryViewModel.setValue((Battery) msg.obj);
                    break;
            }
        }
    };

    /**
     * 数据信息
     */
    private LineChart lineChart;
    CardView charge_cardview;
    CardView setArgs_cardView;
    CardView device_state_cardView;
    CardView remote_control_cardView;

    private StatusViewModel statusViewModel;
    private BatteryViewModel batteryViewModel;

    //dialog
    AlertDialog charge_dialog;
    AlertDialog args_dialog;
    AlertDialog status_dialog;

    Button emergency_stop;
    Button back_to_home;
    TextView charge_card_data;

    //status dialog控件
    TextView speed_data;
    TextView front_wheel_angle_data;
    TextView yaw_angle_data;
    TextView lat_data;
    TextView lng_data;

    //battery dialog控件
    TextView charge_data;
    TextView voltage_data;
    TextView capacity_data;

    //args dialog控件
    EditText speed_data_inputview;
    EditText angle_data_inputview;

    Button determine;
    //用来保存数据
    List<Entry> list = new ArrayList<>();

    //电池波纹
    WaveView charge_waveView;
    WaveView spray_waveView;

    //开关
    Switch aSwitch;

    //9个风机

    private final ImageView[] spray_large_heads = new ImageView[9];

    private ImageView large_fan;

    private final int[] speed = new int[9];

    //控制旋转文件
    Animation animation_0_speed;
    Animation animation_20_speed;
    Animation animation_40_speed;
    Animation animation_60_speed;
    Animation animation_80_speed;
    Animation animation_100_speed;
    LinearInterpolator linearInterpolator;

    AnimationDrawable[] animationDrawable = new AnimationDrawable[9];

    //ros通信
    ROSBridgeClient client;
    String ip = "192.168.1.103";   //ros的 IP
    String port = "9090";
    boolean isConn = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        EventBus.getDefault().register(this);
        initView(savedInstanceState);
        initData();
        setListener();
    }


    private void initView(Bundle savedInstanceState) {
        client = ((RCApplication) getApplication()).getRosClient();
        publish();
        mGpsView = (GPSView) findViewById(R.id.gps_view);

        //获取地图控件引用
        mMapView = (TextureMapView) findViewById(R.id.map);
        //交通流量状态控件
        mTrafficView = (TrafficView) findViewById(R.id.tv);
        aMap = mMapView.getMap();
        //显示实时交通
        aMap.setTrafficEnabled(true);

        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        mGpsView.setGpsState(mCurrentGpsState);

        //底部弹出BottomSheet
        mBottomSheet = findViewById(R.id.poi_detail_bottom);
        mBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheet.setVisibility(View.GONE);
        mPoiColseView = findViewById(R.id.iv_close);
        slideUpInfo = findViewById(R.id.slide_up_info);

        //数据显示控件
//        lineChart = findViewById(R.id.linechart);
        charge_cardview = findViewById(R.id.charge_cardview);
        charge_cardview.setOnClickListener(this);
        setArgs_cardView = findViewById(R.id.setArgs_cardView);
        setArgs_cardView.setOnClickListener(this);
        device_state_cardView = findViewById(R.id.device_state_cardView);
        device_state_cardView.setOnClickListener(this);
        remote_control_cardView = findViewById(R.id.remote_control_cardView);
        remote_control_cardView.setOnClickListener(this);


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
        spray_large_heads[0] = findViewById(R.id.spray_large_head_1);
        spray_large_heads[1] = findViewById(R.id.spray_large_head_2);
        spray_large_heads[2] = findViewById(R.id.spray_large_head_3);
        spray_large_heads[3] = findViewById(R.id.spray_large_head_4);
        spray_large_heads[4] = findViewById(R.id.spray_large_head_5);
        spray_large_heads[5] = findViewById(R.id.spray_large_head_6);
        spray_large_heads[6] = findViewById(R.id.spray_large_head_7);
        spray_large_heads[7] = findViewById(R.id.spray_large_head_8);
        spray_large_heads[8] = findViewById(R.id.spray_large_head_9);

        large_fan = findViewById(R.id.large_fan_1);

//        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
//            animationDrawable_20_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_20_round);
//        }
//
//        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
//            animationDrawable_40_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_40_round);
//        }
//
//        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
//            animationDrawable_60_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_60_round);
//        }
//
//        for (int i = 0; i < animationDrawable_0_speed.length; i++) {
//            animationDrawable_80_speed[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_80_round);
//        }
        animation_0_speed = AnimationUtils.loadAnimation(this, R.anim.image_0_rotation);
        animation_20_speed = AnimationUtils.loadAnimation(this, R.anim.image_20_rotation);
        animation_40_speed = AnimationUtils.loadAnimation(this, R.anim.image_40_rotation);
        animation_60_speed = AnimationUtils.loadAnimation(this, R.anim.image_60_rotation);
        animation_80_speed = AnimationUtils.loadAnimation(this, R.anim.image_80_rotation);
        animation_100_speed = AnimationUtils.loadAnimation(this, R.anim.image_100_rotation);
        linearInterpolator = new LinearInterpolator();
        setFan();
        large_fan.setImageDrawable(getResources().getDrawable(R.drawable.ic_large_fan_20));
        large_fan.startAnimation(animation_60_speed);
        mTvLocTitle = (TextView) findViewById(R.id.tv_title);

        mGspContainer = findViewById(R.id.gps_view_container);


        mTvLocation = (TextView) findViewById(R.id.tv_my_loc);

        // 分享组件
        mShareContainer = (LinearLayout) findViewById(R.id.rl_right);
        mImgBtnBack = (ImageButton) findViewById(R.id.ib_back);
        // 路线
        mTvRoute = (TextView) findViewById(R.id.tv_route);
        // 搜索区域
        mLLSearchContainer = (LinearLayout) findViewById(R.id.ll_search_container);
        mRecycleViewSearch = (RecyclerView) findViewById(R.id.rv_search);
        mIvLeftSearch = (ImageView) findViewById(R.id.iv_search_left);
        mEtSearchTip = (EditText) findViewById(R.id.et_search_tip);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycleViewSearch.setLayoutManager(layoutManager);
        mSearchProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        nearby = findViewById(R.id.nearby_view);
        nearby.setVisibility(View.GONE);

        setBottomSheet();
        setUpMap();
        setDialog();
        mPadding = getResources().getDimensionPixelSize(R.dimen.padding_size);
        /*int statusBarHeight = DeviceUtils.getStatusBarHeight(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.WHITE);
        log(String.format("statusBarHeight=%s", statusBarHeight));*/
//        SystemUIModes.setTranslucentStatus(this, true);
    }

    private void setSpeed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    int[] temp = new int[9];
                    for(int i = 0;i<fanSpeedViewModel.getSpeedValue().length;i++){
                        temp[i] = (fanSpeedViewModel.getSpeedValue()[i]+20)%100;
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

    private void setFan() {
        animation_0_speed.setInterpolator(linearInterpolator);
        animation_20_speed.setInterpolator(linearInterpolator);
        animation_40_speed.setInterpolator(linearInterpolator);
        animation_60_speed.setInterpolator(linearInterpolator);
        animation_80_speed.setInterpolator(linearInterpolator);
        animation_100_speed.setInterpolator(linearInterpolator);
    }

    private void setChart() {
        list.add(new Entry(0, 7));     //其中两个数字对应的分别是   X轴   Y轴
        list.add(new Entry(1, 10));
        list.add(new Entry(2, 12));
        list.add(new Entry(3, 6));
        list.add(new Entry(4, 3));

        //设置x轴宽度
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setAxisLineWidth(4);
        xAxis.setEnabled(false);
        lineChart.setBackgroundColor(0xffadc2d5);
        //设置左y轴宽度
        YAxis AxisLeft = lineChart.getAxisLeft();
        AxisLeft.setAxisLineWidth(4);
        //list是你这条线的数据  "语文" 是你对这条线的描述（也就是图例上的文字）
        LineDataSet lineDataSet = new LineDataSet(list, "喷洒量");

        //不显示描述
        lineChart.getDescription().setEnabled(false);
//        lineChart.setVisibleXRange(0,3);
        //设置折线图模式为CUBIC_BEZIER
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(ContextCompat.getColor(this, R.color.color_gray));
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        //隐藏右边的Y轴
        lineChart.getAxisRight().setEnabled(false);
        //这是x轴在底部
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.switch1) {

        }
    }

    //订阅topic
    private void publish() {
        String msg1 = "{\"op\":\"subscribe\",\"topic\":\"/status\"}";
        client.send(msg1);
        String msg2 = "{\"op\":\"subscribe\",\"topic\":\"/battery\"}";
        client.send(msg2);
        String msg3 = "{\"op\":\"subscribe\",\"topic\":\"/control\"}";
        client.send(msg3);
        String msg4 = "{\"op\":\"subscribe\",\"topic\":\"/pwm_control\"}";
        client.send(msg4);
    }

    private void SendDataToRos(String topic, String data) {
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/" + topic + "\", \"msg\": " + data + "}";
        //        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
        //                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        client.send(msg1);
    }

    //发送数据到ROS端
    private void sendData(String topic, String data) {
        String msg1 = "{ \"op\": \"publish\", \"topic\": \"/" + topic + "\", \"msg\": { \"data\": \"" + data + "\"}}";
        //        String msg2 = "{\"op\":\"publish\",\"topic\":\"/cmd_vel\",\"msg\":{\"linear\":{\"x\":" + 0 + ",\"y\":" +
        //                0 + ",\"z\":0},\"angular\":{\"x\":0,\"y\":0,\"z\":" + 0.5 + "}}}";
        client.send(msg1);
    }


    public void onEvent(@NonNull final PublishEvent event) {
        Log.d(TAG, "onEvent: "+event.name);
        if ("/status".equals(event.name)) {
            Log.i("chatter", event.msg);
            Status status = new Gson().fromJson(event.msg, Status.class);
            Message message = new Message();
            message.what = 3;
            message.obj = status;
            handler.sendMessage(message);
        } else if ("/battery".equals(event.name)) {
            Battery battery = new Gson().fromJson(event.msg, Battery.class);
            Message message = new Message();
            message.what = 4;
            message.obj = battery;
            handler.sendMessage(message);
            Log.i("battery", event.msg);
        }else if("/pwm_control".equals(event.name)){
            Spray spray = new Gson().fromJson(event.msg,Spray.class);
            Message message = new Message();
            message.what = 2;
            message.obj = spray.getDuc_array();
            handler.sendMessage(message);
            Log.i("mypath", event.msg);
        } else if ("/control".equals(event.name)) {
            Log.i("control", event.msg);
        }
    }


    private void parseChatterTopic(PublishEvent event) {
        Log.i("dwayne2", event.msg);
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
        charge_data = layout2.findViewById(R.id.charge_data);
        voltage_data = layout2.findViewById(R.id.voltage_data);
        capacity_data = layout2.findViewById(R.id.capacity_data);
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
        View layout3 = inflater3.inflate(R.layout.set_args_layout3, null);
        args_builder.setView(layout3);
        speed_data_inputview = layout3.findViewById(R.id.speed_data_inputview);
        angle_data_inputview = layout3.findViewById(R.id.angle_data_inputview);

        // 设置确认按钮
        args_builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String linearText = String.valueOf(speed_data_inputview.getText());
                String angularText = String.valueOf(angle_data_inputview.getText());
                if (linearText.equals("") || angularText.equals("")) {
                    showToast("请检查输入是否完整");
                } else {
                    final Linear linear = new Linear(Double.parseDouble(linearText));
                    final Angular angular = new Angular(Double.parseDouble(angularText));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Twist twist = new Twist(linear, angular);
                            SendDataToRos("cmd_vel", new Gson().toJson(twist));
                        }
                    }).start();
                }
                dialog.dismiss();      //取消显示(关闭)对话框
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();      //取消显示(关闭)对话框
            }
        });
        // 设置取消按钮
        args_dialog = args_builder.create();
    }


    @Override
    public void onClick(View v) {
        if (v == null) {
            return;
        }
        // 点击关闭POI detail
        else if (v == mPoiColseView) {
            mBehavior.setHideable(true);
            resetGpsButtonPosition();
            hidePoiDetail();
            return;
        }

        // 分享
        else if (v == mShareContainer) {
            if (mAmapLocation != null && mLatLng != null) {
                if (isPoiClick) {
                    // 分享点击poi的位置
//                    shareLocation(mPoiName, mClickPoiLatLng.latitude, mClickPoiLatLng.longitude);
                } else {
                    // 分享当前定位位置
//                    shareLocation(mPoiName, mLatLng.latitude, mLatLng.longitude);
                }
            }
            return;
        }

        // 头部返回ImageButton
        else if (v == mImgBtnBack) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        // 路线,进入导航页面
        else if (v == mTvRoute) {
            if (mLatLng == null) {
                showToast(getString(R.string.location_failed_hold_on));
                return;
            }
            if (mClickPoiLatLng == null) {
                showToast(getString(R.string.please_select_dest_loc));
                return;
            }

            return;
        }

        // 点击搜索左侧返回箭头
        else if (v == mIvLeftSearch) {
            hideSearchTipView();
            showMapView();
            return;
        }

        else if (v == aSwitch) {
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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendData("control", "stop");
                            }
                        }).start();
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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                sendData("control", "stop");
                            }
                        }).start();
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
        }else if(v == emergency_stop){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_prompt);   //设置图标
            builder.setTitle("确认吗");                //标题
            builder.setMessage("确定紧急停止吗？");       //设置内容
            builder.setCancelable(false);
            //确认按钮监听事件
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String json = new Gson().toJson(new Control("stop"));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendData("control", "stop");
                        }
                    }).start();
                    dialog.dismiss();      //取消显示(关闭)对话框
                }
            });

            //忽略按钮监听事件
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();      //取消显示(关闭)对话框
                }
            });
            builder.create().show();         //创建并显示对话框
        }else if (v == back_to_home) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_prompt);   //设置图标
            builder.setTitle("确认吗");                //标题
            builder.setMessage("确定返航吗？");       //设置内容
            builder.setCancelable(false);
            //确认按钮监听事件
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String json = new Gson().toJson(new Control("back"));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendData("control", "back");
                        }
                    }).start();
                    dialog.dismiss();      //取消显示(关闭)对话框
                }
            });

            //取消按钮监听事件
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();      //取消显示(关闭)对话框
                }
            });
            builder.create().show();         //创建并显示对话框
        }
        else if (v == charge_cardview) {
            charge_dialog.show();         //创建并显示对话框
        } else if (v == setArgs_cardView) {
            args_dialog.show();
        } else if (v == device_state_cardView) {
            status_dialog.show();
        } else if (v == remote_control_cardView) {
            Intent intent = new Intent(this, ControlActivity.class);
            startActivity(intent);
        }
    }



    private void initData() {
//        设置gps数据改变时的事件
        gpsDataViewModel = ViewModelProviders.of(this).get((GPSDataViewModel.class));
        gpsDataViewModel.getGpsData().observe(this, new Observer<GPSData>() {
            @Override
            public void onChanged(@Nullable GPSData gpsData) {
                changeFromGps(gpsData);
            }
        });
        fanSpeedViewModel = ViewModelProviders.of(this).get(FanSpeedViewModel.class);
        fanSpeedViewModel.getSpeed().observe(this, new Observer<int[]>() {
            @Override
            public void onChanged(@Nullable int[] ints) {
                changeFromSpeed(ints);
            }
        });
//        setSpeed();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                double lng = 119.509395;
                double lat = 32.20465;
                while (true) {
                    lng -= 0.00001;
                    GPSData gpsData = new GPSData(lat, lng);
//                    gpsDataViewModel.setData(new GPSData(lat, lng++));
                    Message message = new Message();
                    message.what = 1;
                    message.obj = gpsData;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);

        // 搜索结果RecyclerView
        mSearchAdapter = new SearchAdapter(mSearchData);
        mRecycleViewSearch.setAdapter(mSearchAdapter);
        mLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void changeFromSpeed(int[] ints) {
        System.out.println("changefronspeed===============");
        for (int i = 0; i < spray_large_heads.length; i++) {
            switch (ints[i]){
                case 20:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_20_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xFFFFFFFF);
                    animationDrawable[i].start();
                    break;
                case 40:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_40_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xff0000ff);
                    animationDrawable[i].start();
                    break;
                case 60:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_60_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xffFFFF00);
                    animationDrawable[i].start();
                    break;
                case 80:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_80_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xffFFA500);
                    animationDrawable[i].start();
                    break;
                case 100:
                    animationDrawable[i] = (AnimationDrawable) getResources().getDrawable(R.drawable.progress_100_round);
                    spray_large_heads[i].setImageDrawable(animationDrawable[i]);
                    spray_large_heads[i].setColorFilter(0xffff0000);
                    animationDrawable[i].start();
                    break;
                default:

                    spray_large_heads[i].setImageDrawable(getResources().getDrawable(R.drawable.ic_method_draw_image));
                    break;
            }
        }
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
        //屏幕高度3/5
        mScreenHeight = point.y;
        mScreenWidth = point.x;
        //设置bottomsheet高度为屏幕 5/7
        int height = mScreenHeight * 5 / 7;
        mMaxPeekHeight = height;
        ViewGroup.LayoutParams params = mBottomSheet.getLayoutParams();
        params.height = height;
    }

    /**
     * 事件处理
     */
    private void setListener() {
        mGpsView.setOnGPSViewClickListener(this);

        //地图手势事件
        aMap.setAMapGestureListener(this);
        mSensorHelper = new SensorEventHelper(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
        if (mTrafficView != null) {
            mTrafficView.setOnTrafficChangeListener(this);
        }
        if (null != mPoiColseView) {
            mPoiColseView.setOnClickListener(this);
        }
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
                        onPoiDetailCollapsed();
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
                    mPoiColseView.setVisibility(View.GONE);
                    showBackToMapState();
                    if (slideOffset < 1) {

                    }
                    mMoveToCenter = false;
                    if (currSlide - lastSlide > 0) {
                        log(">>>>>向上滑动");
                        slideDown = false;
                        onPoiDetailExpanded();
                        //smoothSlideUpMap(slideOffset);
                    } else if (currSlide - lastSlide < 0) {
                        log("<<<<<向下滑动");
                        //smoothSlideDownMap(slideOffset);
                        if (!slideDown) {
                            smoothSlideDownMap();
                        }
                    }
                } else if (slideOffset == 0) {
                    //滑动到COLLAPSED状态
                    mPoiColseView.setVisibility(View.VISIBLE);
                    showPoiDetailState();
                } else if (slideOffset < 0) {
                    //从COLLAPSED向HIDDEN状态滑动，此处禁止BottomSheet隐藏
                    //setHideable(false)禁止Behavior执行：可以实现禁止向下滑动消失
                    mBehavior.setHideable(false);
                }
                lastSlide = currSlide;
            }
        });


//        mShareContainer.setOnClickListener(this);

        // 头部返回
        mImgBtnBack.setOnClickListener(this);
        // 地图poi点击
        aMap.setOnPOIClickListener(this);
        // 点击路径进入导航页面
        mTvRoute.setOnClickListener(this);
        // 搜索布局左侧返回箭头图标
        mIvLeftSearch.setOnClickListener(this);
        // 搜索输入框
        mEtSearchTip.addTextChangedListener(this);
        mSearchAdapter.setOnItemClickListener(this);
    }

    private void setUpMap() {
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        aMap.setLocationSource(this);//设置定位监听
        //隐藏缩放控件
        aMap.getUiSettings().setZoomControlsEnabled(false);
        //设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        setLocationStyle();
    }

    public void changeFromGps(GPSData gpsData) {
//        AMapLocation location = new AMapLocation();
        System.out.println("经度：" + gpsData.getLng() + "纬度" + gpsData.getLat());
        latLonPoint = new LatLng(gpsData.getLat(), gpsData.getLng());
//        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 20,GeocodeSearch.AMAP);

        if (mFirstLocation) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLonPoint, mZoomLevel), new AMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mCurrentGpsState = STATE_LOCKED;
                    mGpsView.setGpsState(mCurrentGpsState);
                    mMapType = MyLocationStyle.LOCATION_TYPE_LOCATE;
                    addCircle(latLonPoint, 1);//添加定位精度圆
                    addMarker(latLonPoint);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    mFirstLocation = false;
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            mCircle.setCenter(latLonPoint);
            mCircle.setRadius(1);
            mLocMarker.setPosition(latLonPoint);
            if (mMoveToCenter) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLonPoint, mZoomLevel));
            }
        }
    }

    @Override
    public void onLocationChanged(final AMapLocation location) {

        if (null == mLocationListener || null == location || location.getErrorCode() != 0) {
            if (location != null) {
                LogUtil.d(TAG, "定位失败：errorCode=" + location.getErrorCode() + ",errorMsg=" + location.getErrorInfo());
            }
            return;
        }
        this.mAmapLocation = location;
        if (onScrolling) {
            LogUtil.e(TAG, "MapView is Scrolling by user,can not operate...");
            return;
        }

        //获取经纬度
        double lng = location.getLongitude();
        double lat = location.getLatitude();
        // 当前poiname和上次不相等才更新显示
        if (location.getPoiName() != null && !location.getPoiName().equals(mPoiName)) {
            if (!isPoiClick) {
                // 点击poi时,定位位置和点击位置不一定一样
                mPoiName = location.getPoiName();
                showPoiNameText(String.format("在%s附近", mPoiName));
            }
        }
        //LogUtil.d(TAG, "定位成功，onLocationChanged： lng" + lng + ",lat=" + lat + ",mLocMarker=" + mLocMarker + ",poiName=" + mPoiName+",getDescription="+location.getDescription()+", address="+location.getAddress()+",getLocationDetail"+location.getLocationDetail()+",street="+location.getStreet());

        //参数依次是：视角调整区域的中心点坐标、希望调整到的缩放级别、俯仰角0°~45°（垂直与地图时为0）、偏航角 0~360° (正北方为0)
//        latLonPoint = new LatLng(lat, lng);
        if (!location.getCity().equals(mCity)) {
            mCity = location.getCity();
        }

        //首次定位,选择移动到地图中心点并修改级别到15级
        //首次定位成功才修改地图中心点，并移动
        mAccuracy = location.getAccuracy();
        LogUtil.d(TAG, "accuracy=" + mAccuracy + ",mFirstLocation=" + mFirstLocation);
        /*
        if (mFirstLocation) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLonPoint, mZoomLevel), new AMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    mCurrentGpsState = STATE_LOCKED;
                    mGpsView.setGpsState(mCurrentGpsState);
                    mMapType = MyLocationStyle.LOCATION_TYPE_LOCATE;
                    addCircle(latLonPoint, mAccuracy);//添加定位精度圆
                    addMarker(latLonPoint);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    mFirstLocation = false;
                }

                @Override
                public void onCancel() {

                }
            });
        } else {
            //BottomSheet顶上显示,地图缩小显示
            mCircle.setCenter(latLonPoint);
            mCircle.setRadius(mAccuracy);
            mLocMarker.setPosition(latLonPoint);
            if (mMoveToCenter) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLonPoint, mZoomLevel));
            }

        }

         */
    }


    /**
     * 激活定位
     *
     * @param listener
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mLocationListener = listener;
        LogUtil.d(TAG, "activate: mLocationListener = " + mLocationListener + "");
        //设置定位回调监听
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mLocationOption.setInterval(2000);//定位时间间隔，默认2000ms
            mLocationOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
            mLocationOption.setLocationCacheEnable(true);//开启定位缓存
            mLocationClient.setLocationOption(mLocationOption);
//            mLocationClient.startLocation();
            if (null != mLocationClient) {
                mLocationClient.setLocationOption(mLocationOption);
                mLocationClient.startLocation();
                //运行时权限
                /*if (PermissionUtil.checkPermissions(this)) {
                    mLocationClient.startLocation();
                } else {
                    //未授予权限，动态申请
                    PermissionUtil.initPermissions(this, REQ_CODE_INIT);
                }*/
            }
        }
    }


    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mLocationListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocMarker = null;
        mLocationClient = null;
    }

    /**
     * 设置地图类型
     */
    private void setLocationStyle() {
        // 自定义系统定位蓝点
        if (null == mLocationStyle) {
            mLocationStyle = new MyLocationStyle();
            mLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));
            mLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));//圆圈的颜色,设为透明
        }
        //定位、且将视角移动到地图中心点，定位点依照设备方向旋转，  并且会跟随设备移动。
        aMap.setMyLocationStyle(mLocationStyle.myLocationType(mMapType));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*if (requestCode == REQ_CODE_INIT && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationClient.startLocation();
        }*/
    }


    /**
     * 地图手势事件回调：单指双击
     *
     * @param v
     * @param v1
     */
    @Override
    public void onDoubleTap(float v, float v1) {
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mCurrentGpsState = STATE_UNLOCKED;
        mGpsView.setGpsState(mCurrentGpsState);
        setLocationStyle();
        resetLocationMarker();
        mMoveToCenter = false;
    }

    /**
     * 地体手势事件回调：单指单击
     *
     * @param v
     * @param v1
     */
    @Override
    public void onSingleTap(float v, float v1) {

    }

    /**
     * 地体手势事件回调：单指惯性滑动
     *
     * @param v
     * @param v1
     */
    @Override
    public void onFling(float v, float v1) {
        LogUtil.d(TAG, "onFling,x=" + v + ",y=" + v1);
    }

    /**
     * 地体手势事件回调：单指滑动
     *
     * @param v
     * @param v1
     */
    @Override
    public void onScroll(float v, float v1) {
        //避免重复调用闪屏，当手指up才重置为false
        if (!onScrolling) {
            onScrolling = true;
            LogUtil.d(TAG, "onScroll,x=" + v + ",y=" + v1);
            //旋转不移动到中心点
            mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
            mCurrentGpsState = STATE_UNLOCKED;
            //当前没有正在定位才能修改状态
            if (!mFirstLocation) {
                mGpsView.setGpsState(mCurrentGpsState);
            }
            mMoveToCenter = false;
            setLocationStyle();
            resetLocationMarker();
        }
    }

    /**
     * 地体手势事件回调：长按
     *
     * @param v
     * @param v1
     */
    @Override
    public void onLongPress(float v, float v1) {

    }

    /**
     * 地体手势事件回调：单指按下
     *
     * @param v
     * @param v1
     */
    @Override
    public void onDown(float v, float v1) {
        LogUtil.d(TAG, "onDown");
    }

    /**
     * 地体手势事件回调：单指抬起
     *
     * @param v
     * @param v1
     */
    @Override
    public void onUp(float v, float v1) {
        LogUtil.d(TAG, "onUp");
        onScrolling = false;
    }

    /**
     * 地体手势事件回调：地图稳定下来会回到此接口
     */
    @Override
    public void onMapStable() {

    }


    @Override
    public void onGPSClick() {
        if (!isGpsOpen()) {
            showToast(getString(R.string.please_open_gps));
            return;
        }
        CameraUpdate cameraUpdate = null;
        mMoveToCenter = true;
        isPoiClick = false;
        //修改定位图标状态
        switch (mCurrentGpsState) {
            case STATE_LOCKED:
                mZoomLevel = 20;
                mAnimDuartion = 500;
                mCurrentGpsState = STATE_ROTATE;
//                setLocationStyle(MyLocationStyle.LOCATION_TYPE_MAP_ROTATE);
                mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLonPoint, mZoomLevel, 30, 0));
                break;
            case STATE_UNLOCKED:
            case STATE_ROTATE:
                mZoomLevel = 18;
                mAnimDuartion = 500;
                mCurrentGpsState = STATE_LOCKED;
                mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                cameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(latLonPoint, mZoomLevel, 0, 0));
                break;
        }
        //显示底部POI详情
        if (mBottomSheet.getVisibility() == View.GONE || mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            showPoiDetail("机器人的位置", String.format("在%s附近", mPoiName));
            moveGspButtonAbove();
        } else {
            mTvLocTitle.setText("机器人的位置");
            mTvLocation.setText(String.format("在%s附近", mPoiName));
        }

        aMap.setMyLocationEnabled(true);
        LogUtil.d(TAG, "onGPSClick:mCurrentGpsState=" + mCurrentGpsState + ",mMapType=" + mMapType);
        //改变定位图标状态
        mGpsView.setGpsState(mCurrentGpsState);
        //执行地图动效
        aMap.animateCamera(cameraUpdate, mAnimDuartion, new AMap.CancelableCallback() {
            @Override
            public void onFinish() {
            }

            @Override
            public void onCancel() {

            }
        });
        setLocationStyle();
        resetLocationMarker();
    }

    /**
     * 根据当前地图状态重置定位蓝点
     */
    private void resetLocationMarker() {
        aMap.clear();
        mLocMarker = null;
        if (mGpsView.getGpsState() == GPSView.STATE_ROTATE) {
            //ROTATE模式不需要方向传感器
            //mSensorHelper.unRegisterSensorListener();
            addRotateMarker(latLonPoint);
        } else {
            //mSensorHelper.registerSensorListener();
            addMarker(latLonPoint);
            if (null != latLonPoint) {
                mSensorHelper.setCurrentMarker(mLocMarker);
            }
        }

        addCircle(latLonPoint, mAccuracy);
    }

    @Override
    public void onNearbySearchClick() {
        Toast.makeText(this, "点击附近搜索", Toast.LENGTH_SHORT).show();
    }


    /**
     * 停止定位
     */
    private void stopLocation() {
        if (null != mLocationClient) {
            LogUtil.d(TAG, "stopLocation");
            mLocationClient.stopLocation();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        LogUtil.d(TAG, "onResume");
        mMapView.onResume();
        if (null == mSensorHelper) {
            aMap.clear();
            mSensorHelper = new SensorEventHelper(this);
            //重新注册
            if (mSensorHelper != null) {
                mSensorHelper.registerSensorListener();
                setUpMap();
            }
        }
        if (mLocationOption != null && mLocationClient != null) {
            mLocationOption.setInterval(2000);//定位时间间隔，默认2000ms
            mLocationClient.setLocationOption(mLocationOption);
            aMap.setMyLocationEnabled(true);
        }
        //registerWechatBroadcast();
    }

    /**
     * 注册微信广播
     */
    /*private void registerWechatBroadcast() {
        //建议动态监听微信启动广播进行注册到微信
        IntentFilter filter = new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP);
        // 将该app注册到微信
        mWechatBroadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // 将该app注册到微信
                api.registerApp(Constants.APP_ID);
            }
        };
        registerReceiver(mWechatBroadcast, filter);
    }*/
    @Override
    protected void onPause() {
        super.onPause();
        log("onPause");
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
        mLocationOption.setInterval(20000);//定位时间间隔，默认2000ms
        mLocationClient.setLocationOption(mLocationOption);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregisterWechatBroadcast();
    }

    /**
     * 反注册微信广播
     */
  /*  private void unregisterWechatBroadcast() {
        if(mWechatBroadcast != null){
            unregisterReceiver(mWechatBroadcast);
        }
        if(mWechatBroadcast != null){
            mWechatBroadcast = null;
        }

    }*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (((RCApplication) getApplication()).isConn()) {
            client.disconnect();
        }
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
        mFirstLocation = true;
        if (mSensorHelper != null) {
            mSensorHelper.unRegisterSensorListener();
            mSensorHelper.setCurrentMarker(null);
            mSensorHelper = null;
        }
        deactivate();
        if (null != mLocationClient) {
            mLocationClient.onDestroy();
        }
        if (mLocMarker != null) {
            mLocMarker.destroy();
        }
        // leakcanary检测
    }
    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    private void addMarker(LatLng latlng) {
        /*if (mLocMarker != null) {
            return;
        }*/
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_locked)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocMarker = aMap.addMarker(markerOptions);
    }

    private void addRotateMarker(LatLng latlng) {
       /* if (mLocMarker != null) {
            return;
        }*/
        MarkerOptions markerOptions = new MarkerOptions();
        //3D效果
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.navi_map_gps_3d)));
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.position(latlng);
        mLocMarker = aMap.addMarker(markerOptions);
    }

    /**
     * 跳转用户登录
     */
    private void userLogin() {
        startActivity(new Intent(Robot1Activity.this, UserActivity.class));
    }


    @Override
    public void onTrafficChanged(boolean selected) {
        aMap.setTrafficEnabled(selected);
    }

    private void log(String msg) {
        LogUtil.d(TAG, msg);
    }


    @Override
    public void hidePoiDetail() {
        mBottomSheet.setVisibility(View.GONE);
        //底部：打车、路线...
        mBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        //gsp控件回退到原来位置、并显示底部其他控件

    }

    /**
     * 显示底部POI详情
     *
     * @param locTitle 定位标题,比如当前所在位置名称
     * @param locInfo  定位信息,比如当前在什么附近/距离当前位置多少米
     */
    @Override
    public void showPoiDetail(String locTitle, String locInfo) {
        mGpsView.setVisibility(View.VISIBLE);
        mBottomSheet.setVisibility(View.VISIBLE);
        mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        //我的位置
        mTvLocTitle.setText(locTitle);
        mTvLocation.setText(locInfo);
//        showPoiNameText();

        //int poiTaxiHeight = mPoiDetailTaxi.getMeasuredHeight(); //为0
        int poiTaxiHeight = getResources().getDimensionPixelSize(R.dimen.setting_item_large_height);

        mBehavior.setHideable(true);
        mBehavior.setPeekHeight(mMinPeekHeight + poiTaxiHeight);

    }

    /**
     * 显示当前所在poi点信息
     */
    private void showPoiNameText(String locInfo) {
        mTvLocation.setText(locInfo);
    }

    /**
     * 将GpsButton移动到poi detail上面
     */
    private void moveGspButtonAbove() {

        mBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mGpsView.isAbovePoiDetail()) {
                    //已经在上面，不需要重复调用
                    return;
                }
                LogUtil.d(TAG, "moveGspButtonAbove");
                if (moveY == 0) {
                    //计算Y轴方向移动距离
                    moveY = mGspContainer.getTop() - mBottomSheet.getTop() + mGspContainer.getMeasuredHeight() + mPadding;
                    mBottomSheet.getLocationInWindow(mBottomSheetLoc);
                }
                if (moveY > 0) {
                    mGspContainer.setTranslationY(-moveY);
                    mGpsView.setAbovePoiDetail(true);
                }
            }
        });
    }

    /**
     * 将GpsButton移动到原来位置
     */
    private void resetGpsButtonPosition() {

        mBottomSheet.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (!mGpsView.isAbovePoiDetail()) {
                    //已经在下面，不需要重复调用
                    return;
                }
                //回到原来位置
                mGspContainer.setTranslationY(0);
                mGpsView.setAbovePoiDetail(false);
                LogUtil.d(TAG, "resetGpsButtonPosition");
            }
        });
    }


    @Override
    public void showBackToMapState() {
        //显示:查看详情

        //BottomSheet展开:这里不建议修改BottomSheet状态，backToMap方法可能在BottomSheet状态回调中调用，避免互相调用死循环
    }

    @Override
    public void showPoiDetailState() {

    }

    @Override
    public void minMapView() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mMapView.getLayoutParams();
        //避免重复设置LayoutParams
        if (lp.bottomMargin == mMaxPeekHeight) {
            return;
        }
        lp.bottomMargin = mMaxPeekHeight;
        mMapView.setLayoutParams(lp);
    }

    @Override
    public void maxMapView() {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mMapView.getLayoutParams();
        //避免重复设置LayoutParams
        if (lp.bottomMargin == 0) {
            return;
        }
        lp.bottomMargin = 0;
        mMapView.setLayoutParams(lp);
    }

    @Override
    public void onDetailClick() {

    }

    @Override
    public void onPoiDetailCollapsed() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_chevrons_up);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        slideUpInfo.setCompoundDrawables(drawable, null, drawable, null);
        slideUpInfo.setText("上滑查看详细信息");
        //BottomSheet折叠：显示头部搜索、隐藏反馈、显示右边侧边栏
        mPoiColseView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPoiDetailExpanded() {
        //BottomSheet展开：隐藏头部搜索、显示反馈、隐藏右边侧边栏

    }

    /**
     * 地图平滑上移，重置新的marker
     */
    private void slideUpMarker() {
        aMap.clear();
        mLocMarker = null;
        addRotateMarker(mLatLng);
        if (null != mLocMarker) {
            mSensorHelper.setCurrentMarker(mLocMarker);
        }
        addCircle(mLatLng, mAccuracy);

//        aMap.clear();
//        mLocMarker = null;
//        if (mGpsView.getGpsState() == GPSView.STATE_ROTATE) {
//            //ROTATE模式不需要方向传感器
//            //mSensorHelper.unRegisterSensorListener();
//
//        } else {
//            //mSensorHelper.registerSensorListener();
//            addMarker(mLatLng);
//            if (null != mLocMarker) {
//                mSensorHelper.setCurrentMarker(mLocMarker);
//            }
//        }
//
//        addCircle(mLatLng, mAccuracy);
    }

    @Override
    public void smoothSlideUpMap() {
        Drawable drawable = getResources().getDrawable(R.drawable.ic_chevrons_down);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        slideUpInfo.setCompoundDrawables(drawable, null, drawable, null);
        slideUpInfo.setText("下滑隐藏");
        switch (mGpsView.getGpsState()) {
            case GPSView.STATE_ROTATE:
                if (!isPoiClick) {
                    mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                }
                break;
            case GPSView.STATE_UNLOCKED:
            case GPSView.STATE_LOCKED:
                // 当前没D有操作poi点击
                if (!isPoiClick) {
                    mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                }
                break;
        }
        setLocationStyle();
        //禁用手势操作
//        aMap.getUiSettings().setAllGesturesEnabled(false);
        if (!isPoiClick) {
            mMoveToCenter = true;
        } else {
            mMoveToCenter = false;
        }
        ViewGroup.LayoutParams lp = mMapView.getLayoutParams();
        lp.height = mScreenHeight * 2 / 7;
        mMapView.setLayoutParams(lp);
    }

    @Override
    public void smoothSlideDownMap() {

        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mMoveToCenter = false;
        slideDown = true;
        ViewGroup.LayoutParams lp = mMapView.getLayoutParams();
        lp.height = mScreenHeight;
        mMapView.setLayoutParams(lp);
        //启用手势操作
        aMap.getUiSettings().setAllGesturesEnabled(true);
        switch (mGpsView.getGpsState()) {
            case GPSView.STATE_ROTATE:
                mMapType = MyLocationStyle.LOCATION_TYPE_MAP_ROTATE;
                break;
            case GPSView.STATE_UNLOCKED:
            case GPSView.STATE_LOCKED:
                mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
                break;
        }
        setLocationStyle();
//        resetLocationMarker();
        mMoveToCenter = false;
    }

    /**
     * 高德地图位置转短串分享
     * @param snippet 位置名称
     * @param lat 维度
     * @param lng 经度

    private void shareLocation(String snippet, double lat, double lng) {
    if(TextUtils.isEmpty(snippet)){
    return;
    }
    // addTestLocationMarker(snippet);
    LatLonSharePoint point = new LatLonSharePoint(lat,
    lng, snippet);
    // showProgressDialog();

    }
     */
    /**
     * 高德地图回调
     */
    @Override
    public void onCallTaxiClick() {

    }

    @Override
    public void onRouteClick() {

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 处理返回键
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            MapMode mode = mMapMode;
            if (mode == MapMode.NORMAL) {
                // BottomSheet展开,折叠BottomSheet不关闭Activity
                if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return true;
                } else {
                    return super.onKeyDown(keyCode, event);
                }
            } else if (mode == MapMode.SEARCH) {
                hideSearchTipView();
                showMapView();
                mMapMode = MapMode.NORMAL;

                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 地图POI点击
     *
     * @param poi
     */
    @Override
    public void onPOIClick(Poi poi) {
        LogUtil.d(TAG, "onPOIClick,poi=" + poi);
        if (poi == null || poi.getCoordinate() == null || TextUtils.isEmpty(poi.getName())) {
            return;
        }
        // 当前点击坐标
        mClickPoiLatLng = poi.getCoordinate();
        // 当前正在处理poi点击
        isPoiClick = true;
        addPOIMarderAndShowDetail(poi.getCoordinate(), poi.getName());
    }

    /**
     * 添加POImarker
     */
    private void addPOIMarderAndShowDetail(LatLng latLng, String poiName) {
        animMap(latLng);
        mMapType = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER;
        mCurrentGpsState = STATE_UNLOCKED;
        //当前没有正在定位才能修改状态
        if (!mFirstLocation) {
            mGpsView.setGpsState(mCurrentGpsState);
        }
        mMoveToCenter = false;
        // 添加marker标记
        addPOIMarker(latLng);
        showClickPoiDetail(latLng, poiName);
    }

    /**
     * 显示poi点击底部BottomSheet
     */
    private void showClickPoiDetail(LatLng latLng, String poiName) {
        mPoiName = poiName;
        mTvLocTitle.setText(poiName);
        String distanceStr = MyAMapUtils.calculateDistanceStr(mLatLng, latLng);
        if (mBottomSheet.getVisibility() == View.GONE || mBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            showPoiDetail(poiName, String.format("距离您%s", distanceStr));
            moveGspButtonAbove();
        } else {
            mTvLocTitle.setText(poiName);
            mTvLocation.setText(String.format("距离您%s", distanceStr));
        }
    }

    private void addPOIMarker(LatLng latLng) {
        aMap.clear();
        MarkerOptions markOptiopns = new MarkerOptions();
        markOptiopns.position(latLng);
        markOptiopns.icon(BitmapDescriptorFactory.fromResource(R.drawable.poi_mark));
        aMap.addMarker(markOptiopns);
    }

    /**
     * 移动地图中心点到指定位置
     *
     * @param latLng
     */
    private void animMap(LatLng latLng) {
        if (latLng != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mZoomLevel));
        }
    }

    /**
     * 隐藏地图图层
     */
    private void hideMapView() {
        mMapView.setVisibility(View.GONE);

    }

    /**
     * 显示地图图层
     */
    private void showMapView() {
        mMapView.setVisibility(View.VISIBLE);

    }

    /**
     * 隐藏搜索提示布局
     */
    private void hideSearchTipView() {
        InputMethodUtils.hideInput(this);
        mLLSearchContainer.setVisibility(View.GONE);
        mEtSearchTip.setVisibility(View.VISIBLE);
        mEtSearchTip.setFocusable(true);
        mEtSearchTip.setFocusableInTouchMode(true);
        mSearchData.clear();
        mSearchAdapter.notifyDataSetChanged();
        mEtSearchTip.setText("");
    }

    /**
     * 显示搜索提示布局
     */
    private void showSearchTipView() {
        mLLSearchContainer.setVisibility(View.VISIBLE);
        InputMethodUtils.showInput(this, mEtSearchTip);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    /**
     * EditText输入内容后回调
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (s == null || TextUtils.isEmpty(s.toString())) {
            mSearchProgressBar.setVisibility(View.GONE);
            return;
        }
        String content = s.toString();
        if (!TextUtils.isEmpty(content) && !TextUtils.isEmpty(mCity)) {
            // 调用高德地图搜索提示api
            InputtipsQuery inputquery = new InputtipsQuery(content, mCity);
            inputquery.setCityLimit(true);
            Inputtips inputTips = new Inputtips(this, inputquery);
            inputTips.setInputtipsListener(this);
            inputTips.requestInputtipsAsyn();
            mSearchProgressBar.setVisibility(View.VISIBLE);
        }

    }

    /**
     * 高德地图搜索提示回调
     *
     * @param list
     * @param i
     */
    @Override
    public void onGetInputtips(List<Tip> list, int i) {
        mSearchProgressBar.setVisibility(View.GONE);
        if (list == null || list.size() == 0) {
            return;
        }
        mSearchData.clear();
        mSearchData.addAll(list);
        // 刷新RecycleView
        mSearchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUserClick() {
        userLogin();
    }

    @Override
    public void onSearchClick() {
        // 显示搜索layout,隐藏地图图层,并设置当前地图操作模式
        showSearchTipView();
        hideMapView();
        mMapMode = MapMode.SEARCH;
    }

    @Override
    public void onVoiceClick() {

    }

    @Override
    public void onQrScanClick() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onItemClick(View v, int position) {
        if (mSearchData != null && mSearchData.size() > 0) {
            Tip tip = mSearchData.get(position);
            if (tip == null) {
                return;
            }
            hideSearchTipView();
            showMapView();
            mMoveToCenter = false;
            isPoiClick = true;
            LatLonPoint point = tip.getPoint();
            LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
            addPOIMarderAndShowDetail(latLng, tip.getName());
            showClickPoiDetail(latLng, tip.getName());
        }
    }


    /**
     * 地图模式
     */
    private enum MapMode {
        /**
         * 普通模式:显示地图图层
         */
        NORMAL,

        /**
         * 搜索模式:显示搜索提示和搜索结果
         */
        SEARCH
    }

    /**
     * 搜索Adapter
     */
    private static class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> implements View.OnClickListener {

        private List<Tip> mData;
        private OnItemClickListener mListener;

        public SearchAdapter(List<Tip> data) {
            this.mData = data;
        }

        /**
         * 设置RecycleView条目点击
         *
         * @param listener
         */
        public void setOnItemClickListener(OnItemClickListener listener) {
            this.mListener = listener;
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
            View itemView = ((LayoutInflater) viewGroup.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.search_tip_recycle_item, viewGroup, false);
            itemView.setTag(position);
            itemView.setOnClickListener(this);
            return new SearchViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            Tip tip = mData.get(position);
            holder.tvSearchTitle.setText(tip.getName());
            holder.tvSearchLoc.setText(tip.getAddress());
        }

        @Override
        public int getItemCount() {
            if (mData != null && mData.size() > 0) {
                return mData.size();
            }
            return 0;
        }

        @Override
        public void onClick(View v) {
            if (v != null && mListener != null) {
                int postion = (int) v.getTag();
                mListener.onItemClick(v, postion);
            }
        }
    }


    /**
     * 搜索ViewHolder
     */
    private static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tvSearchTitle;
        TextView tvSearchLoc;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSearchTitle = itemView.findViewById(R.id.tv_search_title);
            tvSearchLoc = itemView.findViewById(R.id.tv_search_loc);
        }
    }

    /**
     * 是否打开GPS
     *
     * @return
     */
    private boolean isGpsOpen() {
        return mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {

    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

    }

}
