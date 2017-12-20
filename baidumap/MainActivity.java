package com.zyy.baidumap;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNCommonSettingParam;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.zyy.baidumap.R;
import com.zyy.baidumap.MyOrientationListener.OnOrientationListener;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.WindowInsetsCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener{

	private MapView mMapView;
	private BaiduMap mBaiduMap;

	private Button mBtnLocation;
	private Button mBtnMockNav;
	private Button mBtnRealNav;
	private Context context;
	
	//定位相关
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;//定位监听器
	private boolean isFirstIn = true;
	private double mLatitude;
	private double mLongtitude;
	
	private LatLng mLastLocationData;//起点
	private LatLng mDestLocationData;//终点
	
	private boolean hasInitSuccess = false;
	private boolean hasRequestComAuth = false;
	
	public static List<Activity> activityList = new LinkedList<Activity>();
	private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo-zyy-maptest";
    private String mSDCardPath = null;
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
	//自定义定位图标
	private BitmapDescriptor mIconLocation;
	private MyOrientationListener myOrientationListener;
	private float mCurrentX;
	private LocationMode mLocationMode;
	
	//覆盖物相关
	private BitmapDescriptor mMarker;
	private RelativeLayout mMarkerLy;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 在使用SDK各组件之前初始化context信息，传入ApplicationContext
		// 注意该方法要再setContentView方法之前实现
		try {  
		    getWindow().addFlags(WindowManager.LayoutParams.class.getField("FLAG_NEEDS_MENU_KEY").getInt(null));  
		}catch (NoSuchFieldException e) {  
		    // Ignore since this field won't exist in most versions of Android  
		}catch (IllegalAccessException e) {  
		    Log.w("feelyou.info", "Could not access FLAG_NEEDS_MENU_KEY in addLegacyOverflowButton()", e);  
		} 
		SDKInitializer.initialize(getApplicationContext());
		
		
		Bundle bundle = new Bundle();
        // 必须设置APPID，否则会静音
        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "9354030");
        BNaviSettingManager.setNaviSdkParam(bundle);
		
		
		setContentView(R.layout.activity_main);
		mBtnLocation = (Button) findViewById(R.id.id_btn_location);
		mBtnMockNav = (Button) findViewById(R.id.id_btn_mocknav); 
		mBtnRealNav = (Button) findViewById(R.id.id_btn_realnav);
		
		mBtnLocation.setOnClickListener(this);
		mBtnMockNav.setOnClickListener(this);
		mBtnRealNav.setOnClickListener(this);
		
		this.context=this;
		initView();
		
		//初始化定位
		initLocation();
		
		initMarker();
		
		mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			
			@Override
			public boolean onMarkerClick(Marker marker) {
				// TODO Auto-generated method stub
				
				Bundle extraInfo = marker.getExtraInfo();
				Info info = (Info) extraInfo.getSerializable("info");
				ImageView iv = (ImageView) mMarkerLy.findViewById(R.id.id_info_img);
				TextView distance = (TextView) mMarkerLy.findViewById(R.id.id_info_distance);
				TextView name = (TextView) mMarkerLy.findViewById(R.id.id_info_name);
				TextView zan = (TextView) mMarkerLy.findViewById(R.id.id_info_zan);
				
				
				iv.setImageResource(info.getImgId());
				distance.setText(info.getDistance());
				name.setText(info.getName());
				zan.setText(info.getZan()+"");
				
				InfoWindow infoWindow;
				TextView tv = new TextView(context);
				tv.setBackgroundResource(R.drawable.location_tips);
				tv.setPadding(30, 20, 30, 50);
				tv.setText(info.getName());
				tv.setTextColor(Color.parseColor("#ffffff"));
				
				final LatLng latLng = marker.getPosition();
				Point p = mBaiduMap.getProjection().toScreenLocation(latLng);
				p.y -= 47;
				LatLng ll = mBaiduMap.getProjection().fromScreenLocation(p);
				infoWindow = new InfoWindow(tv,ll,CONTEXT_IGNORE_SECURITY);
			
			
				mBaiduMap.showInfoWindow(infoWindow);
			
				
				mMarkerLy.setVisibility(View.VISIBLE);  //显示布局
				
				return true;
			}
		});

        //当我们点击图标其它位置时将 布局消失
		mBaiduMap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public boolean onMapPoiClick(MapPoi arg0) {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void onMapClick(LatLng arg0) {
				// TODO Auto-generated method stub
				mMarkerLy.setVisibility(View.GONE);
				mBaiduMap.hideInfoWindow();
			}
		});
		
		
		mBaiduMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng arg0) 
			{
				Toast.makeText(MainActivity.this, "设置目的地成功 ",Toast.LENGTH_SHORT).show();
				mDestLocationData = arg0;
				addDestInfoOverlay(arg0);
				
			}

		});
		
	
		//初始化导航相关
		 if (initDirs()) {
	            initNavi();
	        }
		
	}
	private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    String authinfo = null;
    
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    // showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    // showToastMsg("Handler : TTS play end");
                    break;
                }
                default:
                    break;
            }
        }
    };

    @SuppressWarnings("deprecation")
	private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        if (android.os.Build.VERSION.SDK_INT >= 23) {

           
        }

        
        
        BaiduNaviManager.getInstance().init(this,mSDCardPath, APP_FOLDER_NAME,new NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败， " + msg;
                }
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
                Toast.makeText(MainActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                hasInitSuccess = true;
                initSetting();
           
            }

            public void initStart() {
                Toast.makeText(MainActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(MainActivity.this, "百度导航引擎初始化结束", Toast.LENGTH_SHORT).show();
            }

        }, null);

    }
    
    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }
	
		
		private void addDestInfoOverlay(LatLng destInfo) 
		{
		    mBaiduMap.clear();
		    OverlayOptions options = new MarkerOptions().position(destInfo)
		    		.icon(BitmapDescriptorFactory.fromResource(R.drawable.myloc))
		    		.zIndex(5);
		    mBaiduMap.addOverlay(options);
			
		}
	
		 private void initSetting() {
		        // BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
		        BNaviSettingManager
		                .setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
		        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
		        // BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
		        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
		        BNaviSettingManager.setIsAutoQuitWhenArrived(true);
		        Bundle bundle = new Bundle();
		        // 蹇呴』璁剧疆APPID锛屽惁鍒欎細闈欓煶
		        bundle.putString(BNCommonSettingParam.TTS_APP_ID, "9354030");
		        BNaviSettingManager.setNaviSdkParam(bundle);
		    }
		
		


	private void initMarker() {
		// TODO Auto-generated method stub
		mMarker = BitmapDescriptorFactory.fromResource(R.drawable.maker);
		mMarkerLy = (RelativeLayout)findViewById(R.id.id_maker_ly);
		
	}

	private void initLocation() {
		// TODO Auto-generated method stub
		mLocationMode = LocationMode.NORMAL;
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();  //new 一个实例
		mLocationClient.registerLocationListener(mLocationListener);//进行注册
		
		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");//坐标类型
		option.setIsNeedAddress(true);//返回当前位置
		option.setOpenGps(true);
		option.setScanSpan(1000);
		mLocationClient.setLocOption(option);
		
		//初始化图标
		mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
		
		myOrientationListener = new MyOrientationListener(context);
		
		myOrientationListener.setOnOrientationListener(new OnOrientationListener(){
			
			public void onOrientationChanged(float x){
				mCurrentX =x;
			}
			
		});
		
		
	}

	private void initView() {

		mMapView = (MapView) findViewById(R.id.id_bmapView);
		mBaiduMap = mMapView.getMap();

		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f); // 显示标尺
																	// 约500米
		mBaiduMap.setMapStatus(msu);

	}

	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//开启定位
		mBaiduMap.setMyLocationEnabled(true);
		if(!mLocationClient.isStarted()){
			mLocationClient.start();
			//开启方向传感器
			myOrientationListener.start();
		}
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//停止定位
		mBaiduMap.setMyLocationEnabled(false);
		mLocationClient.stop();
		//停止方向传感器
		myOrientationListener.stop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
        //点击菜单项  可以切换 地图模式哦
		switch (item.getItemId()) {
		case R.id.id_map_common:
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);//普通模式
			break;
		case R.id.id_map_site:
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);//卫星模式
			break;
		case R.id.id_map_traffic:                  //实时交通
            if(mBaiduMap.isTrafficEnabled()){      //判断TrafficEnabled是否显示
            	mBaiduMap.setTrafficEnabled(false);
            	item.setTitle("实时交通(off)");
            }else{
            	mBaiduMap.setTrafficEnabled(true);
            	item.setTitle("实时交通(on)");
            }
			break;
		case R.id.id_map_location:
			centerToMyLocation();
			break;
		case R.id.id_map_mode_common:
			mLocationMode = LocationMode.NORMAL;
			break;
		case R.id.id_map_mode_following:
			mLocationMode = LocationMode.FOLLOWING;
			break;
		case R.id.id_map_mode_compass:
			mLocationMode = LocationMode.COMPASS;
			break;
		case R.id.id_add_overlay:
			addOverlays(Info.infos);
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 *添加覆盖物 
	 */
	private void addOverlays(List<Info> infos) {
		// TODO Auto-generated method stub
		mBaiduMap.clear();
		LatLng latLng = null;
		Marker marker = null;
		OverlayOptions options;
		for(Info info:infos)
		{
			//经纬度
			latLng = new LatLng(info.getLatitude(),info.getLongitude());
			//图标
			options = new MarkerOptions().position(latLng).icon(mMarker).zIndex(5);
			
			marker = (Marker)mBaiduMap.addOverlay(options);
			Bundle arg0 = new Bundle();
			arg0.putSerializable("info", info);
			marker.setExtraInfo(arg0);
			
			MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
			mBaiduMap.setMapStatus(msu);
			
		}
		
		
	}

	/**
	 *定位到我的位置 
	 */
	private void centerToMyLocation(){
		LatLng latLng = new LatLng(mLatitude,mLongtitude);
		MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
		mBaiduMap.animateMapStatus(msu);
		
	}


	private class MyLocationListener implements BDLocationListener{

		@Override
		public void onReceiveLocation(BDLocation location) {   //定位成功后的回调
			// TODO Auto-generated method stub
			MyLocationData data = new MyLocationData.Builder()  //build的模式
					.direction(mCurrentX)
					.accuracy(location.getRadius())            //获取定位精准度  
					.latitude(location.getLatitude())        //获取经纬度
					.longitude(location.getLongitude())
					.build();
			
			
			mBaiduMap.setMyLocationData(data);
			//定位的小图标 箭头 自定义
			MyLocationConfiguration config = new MyLocationConfiguration(mLocationMode,true,mIconLocation);
			
			mBaiduMap.setMyLocationConfiguration(config);
			
			//更新经纬度
			mLatitude = location.getLatitude();
			mLongtitude = location.getLongitude();
			if(isFirstIn){ //判读是否第一次进入
				
				LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
				mBaiduMap.animateMapStatus(msu);
				isFirstIn = false;
				
				
				Toast.makeText(context, location.getAddrStr(),Toast.LENGTH_LONG).show();
				
			}
			
			
			
		}
		
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) 
		{
		case R.id.id_btn_location:
			
			Toast.makeText(MainActivity.this,"我在海里",Toast.LENGTH_SHORT).show();
			if (mLastLocationData != null) 
			{
				
				MapStatusUpdate u = MapStatusUpdateFactory
						.newLatLng(mLastLocationData);
				mBaiduMap.animateMapStatus(u);
			}
			break;
		case R.id.id_btn_mocknav:
			if(mDestLocationData == null)
			{
				Toast.makeText(MainActivity.this,"长按地图设置目标地点",Toast.LENGTH_SHORT).show();
				return;
			}
			routeplanToNavi(false);
			
			break;
		case R.id.id_btn_realnav:
			if(mDestLocationData == null)
			{
				Toast.makeText(MainActivity.this,"长按地图设置目标地点",Toast.LENGTH_SHORT).show();
				return;
			}
			routeplanToNavi(true);
			
			break;
		}
	}
	  private boolean hasCompletePhoneAuth() {
	        // TODO Auto-generated method stub

	        PackageManager pm = this.getPackageManager();
	        
	        
	        return true;
	  }


	    private void routeplanToNavi(boolean mock) {
	   
	        if (!hasInitSuccess) {
	            Toast.makeText(MainActivity.this, "杩樻湭鍒濆鍖�!", Toast.LENGTH_SHORT).show();
	        }
	        // 鏉冮檺鐢宠
	     
	    	 Toast.makeText(MainActivity.this, "有没有到这一步!", Toast.LENGTH_SHORT).show();
	    	CoordinateType coType = CoordinateType.GCJ02;
	        BNRoutePlanNode sNode = null;
	        BNRoutePlanNode eNode = null;
	                sNode = new BNRoutePlanNode(mLastLocationData.longitude,
	                		mLastLocationData.latitude, "我的地点", null, coType);
	                eNode = new BNRoutePlanNode(mLastLocationData.longitude,
	                		mLastLocationData.latitude, "目标地点", null, coType);
	          
	         
	        if (sNode != null && eNode != null) {
	            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
	            list.add(sNode);
	            list.add(eNode);
	            
	            // 寮�鍙戣�呭彲浠ヤ娇鐢ㄦ棫鐨勭畻璺帴鍙ｏ紝涔熷彲浠ヤ娇鐢ㄦ柊鐨勭畻璺帴鍙�,鍙互鎺ユ敹璇卞淇℃伅绛�
	            // BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new DemoRoutePlanListener(sNode));
	            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, mock, new DemoRoutePlanListener(sNode)
	                    );
	        }
	    }
	    
	
	    
	    public class DemoRoutePlanListener implements RoutePlanListener {

	        private BNRoutePlanNode mBNRoutePlanNode = null;

	        public DemoRoutePlanListener(BNRoutePlanNode node) {
	            mBNRoutePlanNode = node;
	        }

	        @Override
	        public void onJumpToNavigator() {
	            /*
	             * 璁剧疆閫斿緞鐐逛互鍙妑esetEndNode浼氬洖璋冭鎺ュ彛
	             */

	           /* for (Activity ac : activityList) {

	                if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

	                    return;
	                }
	            }*/
	            Intent intent = new Intent(MainActivity.this, BNDemoGuideActivity.class);
	            Bundle bundle = new Bundle();
	            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
	            intent.putExtras(bundle);
	            startActivity(intent);

	        }

	        @Override
	        public void onRoutePlanFailed() {
	            // TODO Auto-generated method stub
	            Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
	        }
	    }

	
	
}
