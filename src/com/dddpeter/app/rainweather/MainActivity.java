package com.dddpeter.app.rainweather;

import java.io.File;
import java.io.IOException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dddpeter.app.rainweather.object.DistrictMapClass;
import com.dddpeter.app.rainweather.util.FileOperator;
import com.dddpeter.app.rainweather.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("DefaultLocale")
public class MainActivity extends FinalActivity {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;
	@ViewInject(id = R.id.imageView1)
	ImageView image;
	@ViewInject(id = R.id.setting_button, click = "btnRefreshClick")
	Button buttonRefresh;
	@ViewInject(id = R.id.exit_button, click = "btnExitClick")
	Button buttonExit;
	@ViewInject(id = R.id.content)
	TextView content;
	ProgressDialog mDialog;
	private LocationClient mLocClient;
	private JSONObject weatherObject=new JSONObject();
	private JSONObject weatherObjectDetail=new JSONObject();
	private final String DATA_PATH="/sdcard/tmp/";
	private final String DATA_NAME="weather.json";
	private final String DATA_DETAIL_NAME="weather_detail.json";
	private Map<String, Integer> pictrueMap1;
	private Map<String, Integer> pictrueMap2;
	private Map<String, String> districtMap;
	private BDLocation myLocation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		final View contentView = content;
		init();
		
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

	}

	

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	public void btnExitClick(View v) {
		this.finish();
	}

	public void btnRefreshClick(View v) {
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		Log.v("知雨天气", "开始进行定位:" + (mLocClient != null));
		LocationManager alm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		LocationClientOption option = new LocationClientOption();
		Log.v("知雨天气",
				"是否使用GPS:"
						+ (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)));
		option.setOpenGps(alm
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)); // 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setAddrType("all"); // 设置地址信息，仅设置为"all”时有地址信息，默认无地址信息
		option.setScanSpan(500); // 设置定位模式，小于1秒则一次定位;大于等于1秒则定时定位
		option.setProdName("知雨天气");
		mLocClient.setLocOption(option);
		mLocClient.start();
		Log.v("V", "" + mLocClient.isStarted());
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			mLocClient.requestLocation();
		} else {
			mLocClient.requestPoi();
		}
	}

	BDLocationListener myListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;
			myLocation=location;
			updateWeather(location.getDistrict());

		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			System.out.println("poiLocation");
			if (poiLocation == null) {
				return;
			}
			myLocation=poiLocation;
			updateWeather(poiLocation.getDistrict());

		}

	};

	private final void updateWeather(String district) {
		if(district==null || district.trim().equals("")){
			Toast.makeText(MainActivity.this, "定位失败，请打开网络重试", Toast.LENGTH_SHORT).show();
			setNetworkMethod(MainActivity.this);
			return;
		}
		System.out.println(district);
		String cityStr =getValidDistrictName(district);
		
		String resultJSON = "";
		String resultJSON0 = "";
		try {
			Log.v("知雨天气", "获取" + cityStr + "天气");
			final String link0="http://m.weather.com.cn/data/"+districtMap.get(cityStr)+".html";
			final String link = "http://www.weather.com.cn/data/sk/"+districtMap.get(cityStr)+".html";
			resultJSON0=getJSONHttp(link0,"UTF-8");
			resultJSON=getJSONHttp(link,"UTF-8");
			FileOperator.saveFile(resultJSON, this.DATA_PATH,this.DATA_NAME);
			FileOperator.saveFile(resultJSON0, this.DATA_PATH,this.DATA_DETAIL_NAME);
			parseJASONObject(resultJSON,resultJSON0);
			updateContent();
		}catch(Exception e){
			e.printStackTrace();
			Toast.makeText(MainActivity.this,"获取天气信息失败", Toast.LENGTH_SHORT).show();
		}
			
		
		
		

	}
	
	private String getValidDistrictName(String district) {
		String result;
		int i=2;
		while(true){
			try{
			if(districtMap.containsKey(result=district.substring(0, i))){
				break;
			}
			else{
				i++;
			}
			}catch(Exception e){
				getValidDistrictName(myLocation.getCity());
			}
			
		}
		return result;
	}
private String getJSONHttp(String link,String charSet) throws Exception{
	HttpGet httpRequest = new HttpGet(link);
	HttpClient httpclient = new DefaultHttpClient();
	HttpResponse httpResponse;
	httpResponse = httpclient.execute(httpRequest);

	if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		return  EntityUtils.toString(httpResponse.getEntity(),
				"UTF-8");
	}
	else{
		throw new Exception("连接获取天气信息失败");
		
	}
	
	
	
}


	public void updateContent() throws JSONException{
		JSONObject temp=weatherObject.getJSONObject("weatherinfo");
		JSONObject temp0=weatherObjectDetail.getJSONObject("weatherinfo");
		StringBuffer sb=new StringBuffer();
		sb.append("<h2>"+temp.getString("city")+"</h2>");
		sb.append(temp.getString("temp")+"℃<br/>");
		sb.append(temp0.getString("date_y")+" "+temp0.getString("week")
				+"<br/>");
		sb.append("天气："+temp0.getString("weather1")+"<br/>");
		sb.append("温度："+temp0.getString("temp1")+"<br/>");
		sb.append("风向："+temp.getString("WD")+"<br/>");
		sb.append("风力："+temp0.getString("fl1")+"<br/>");
		sb.append("湿度："+temp.getString("SD")+"<br/>");
		sb.append("更新时间："+temp.getString("time"));
		content.setText(Html.fromHtml(sb.toString()));
		Date time=new Date();
		int resid=R.drawable.notclear;
		try{
		
		if(time.getHours()>=6 && time.getHours()<18 ){
			resid=pictrueMap1.get(temp0.getString("weather1"));
		}
		else 
		{
			resid=pictrueMap2.get(temp0.getString("weather1"));
		}
		}catch(Exception e){
			e.printStackTrace();
			resid=R.drawable.notclear;
		}
		image.setImageResource(resid);
	}

	public void parseJASONObject(String JASON,String JASON1) throws Exception {
		
		weatherObject=new JSONObject(JASON);
		weatherObjectDetail=new JSONObject(JASON1);
		
	}
	private void init() {
		pictrueMap1=new HashMap<String, Integer>();
		pictrueMap2=new HashMap<String, Integer>();
		districtMap=new HashMap<String, String>();
		pictrueMap1.put("大雨", R.drawable.dby);
		pictrueMap1.put("暴雨", R.drawable.dby);
		pictrueMap1.put("冻雨", R.drawable.dy);
		pictrueMap1.put("大雪", R.drawable.dx);
		pictrueMap1.put("暴雪", R.drawable.bx);
		pictrueMap1.put("多云", R.drawable.dy);
		pictrueMap1.put("多云转晴",R.drawable.dyq);
		pictrueMap1.put("晴转多云",R.drawable.dyq);
		pictrueMap1.put("雷阵雨", R.drawable.lzy);
		pictrueMap1.put("沙尘暴", R.drawable.scb);
		pictrueMap1.put("雾", R.drawable.w);
		pictrueMap1.put("小雪", R.drawable.xx);
		pictrueMap1.put("小雨", R.drawable.xy);
		pictrueMap1.put("阴", R.drawable.y);
		pictrueMap1.put("晴", R.drawable.q);
		pictrueMap1.put("雨夹雪", R.drawable.yjx);
		pictrueMap1.put("中雨", R.drawable.zhy);
		pictrueMap1.put("中雪", R.drawable.zx);
		pictrueMap1.put("阵雨", R.drawable.zy);
		
		
		pictrueMap2.put("大雨", R.drawable.dby);
		pictrueMap2.put("暴雨", R.drawable.dby);
		pictrueMap2.put("冻雨", R.drawable.dy0);
		pictrueMap2.put("大雪", R.drawable.dx0);
		pictrueMap2.put("暴雪", R.drawable.bx);
		pictrueMap2.put("多云", R.drawable.dy0);
		pictrueMap2.put("雷阵雨", R.drawable.lzy0);
		pictrueMap2.put("沙尘暴", R.drawable.scb);
		pictrueMap2.put("多云转晴",R.drawable.dyq0);
		pictrueMap2.put("晴转多云",R.drawable.dyq0);
		pictrueMap2.put("雾", R.drawable.w);
		pictrueMap2.put("小雪", R.drawable.xx);
		pictrueMap2.put("小雨", R.drawable.xy);
		pictrueMap2.put("阴", R.drawable.y);
		pictrueMap2.put("晴", R.drawable.q0);
		pictrueMap2.put("雨夹雪", R.drawable.yjx);
		pictrueMap2.put("中雨", R.drawable.zhy);
		pictrueMap2.put("中雪", R.drawable.zx);
		pictrueMap2.put("阵雨", R.drawable.zy0);
		districtMap.putAll((new DistrictMapClass()).getDistric());
			
		
		
		String json="";
		String json0="";
		File f=new File(DATA_PATH+this.DATA_NAME);
		File f0=new File(DATA_PATH+this.DATA_DETAIL_NAME);
		System.out.print("-----------------"+(f.exists() && f0.exists()));
		if(f.exists()){
			json=FileOperator.readFile(DATA_PATH+this.DATA_NAME);
			if(f0.exists()){
			     json0=FileOperator.readFile(DATA_PATH+this.DATA_DETAIL_NAME);
			     try {
						parseJASONObject(json,json0);
						updateContent();
						
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(MainActivity.this, "尚未获得天气信息，请定位并刷新天气信息", Toast.LENGTH_SHORT).show();
					}
			}
			
		}		
	}
	/*
     * 打开设置网络界面
     * */
    public static void setNetworkMethod(final Context context){
        //提示对话框
        AlertDialog.Builder builder=new Builder(context);
        builder.setTitle("网络设置提示").setMessage("网络连接不可用,是否进行设置?").setPositiveButton("设置", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent intent=null;
                //判断手机系统的版本  即API大于10 就是3.0或以上版本 
                if(android.os.Build.VERSION.SDK_INT>10){
                    intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }else{
                    intent = new Intent();
                    ComponentName component = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                }
                context.startActivity(intent);
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        }).show();
    }

}
