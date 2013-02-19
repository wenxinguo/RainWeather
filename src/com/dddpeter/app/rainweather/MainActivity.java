package com.dddpeter.app.rainweather;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dddpeter.app.rainweather.object.SinaWeather;
import com.dddpeter.app.rainweather.util.FileOperator;
import com.dddpeter.app.rainweather.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
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
	private SinaWeather weatherObject=new SinaWeather();
	private final String DATA_PATH="/sdcard/tmp/";
	private final String XML_NAME="sina_weather.xml";
	private Map<String, Integer> pictrueMap1;
	private Map<String, Integer> pictrueMap2;

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

			updateWeather(location.getCity());

		}

		@Override
		public void onReceivePoi(BDLocation poiLocation) {
			System.out.println("poiLocation");
			if (poiLocation == null) {
				return;
			}

			updateWeather(poiLocation.getCity());

		}

	};

	private final void updateWeather(String city) {
		String cityStr = city.split("市")[0];
		
		String resultXml = "";
		
		try {
			Log.v("知雨天气", "获取" + cityStr + "天气");
			final String link = "http://php.weather.sina.com.cn/xml.php?city="
					+ java.net.URLEncoder.encode(cityStr, "gb2312")
					+ "&password=DJOYnieT8234jlsK&day=0";
			HttpGet httpRequest = new HttpGet(link);
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse;

			httpResponse = httpclient.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				resultXml = EntityUtils.toString(httpResponse.getEntity(),
						"UTF-8");
				FileOperator.saveFile(resultXml, this.DATA_PATH,this.XML_NAME);
				System.out.println(link);
				parseXml(resultXml);
				updateContent();
				Toast.makeText(MainActivity.this, "天气信息同步成功", Toast.LENGTH_SHORT).show();
			} else {
				Log.e("知雨天气", "获取" + cityStr + "天气失败："
						+ httpResponse.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("知雨天气", "获取" + cityStr + "天气失败：" + e.getLocalizedMessage());
		}
		
		
		

	}
	public void updateContent(){
		final StringBuffer sb = new StringBuffer();
		sb.append(weatherObject.getCity());
		sb.append("<br/>日期：" + weatherObject.getCurrentDate());
		sb.append("<br/>白天：" + weatherObject.getDayStatus());
		sb.append("<br/>夜间："+weatherObject.getNightStatus());
		sb.append("<br/>低温：" + weatherObject.getLowTemprature() + "℃");
		sb.append("<br/>高温：" + weatherObject.getHighTemprature() + "℃");
		content.setText(Html.fromHtml(sb.toString()));
		Date date=new Date();
		int hour=date.getHours();
		int rsid=R.drawable.notclear;
		if(hour>=6 && hour<18){
			String weather=weatherObject.getDayStatus();
			if(pictrueMap1.containsKey(weather)){
				rsid=pictrueMap1.get(weather);
			}
		}
		else
		{
			String weather=weatherObject.getNightStatus();
			if(pictrueMap2.containsKey(weather)){
				rsid=pictrueMap2.get(weather);
			}
		}
		image.setImageResource(rsid);
	}

	public void parseXml(String xml) throws Exception {

		DocumentBuilderFactory factory = null;
		DocumentBuilder builder = null;
		Document document = null;
		InputStream inputStream = null;
		factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		inputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		document = builder.parse(inputStream);
		// 找到根Element
		Element root = document.getDocumentElement();
		NodeList nodes = root.getElementsByTagName("Weather");
			Node node = nodes.item(0);
			NodeList childs = node.getChildNodes();
			for (int j = 0; j < childs.getLength(); j++) {
				Node element = childs.item(j);
				if ("city".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setCity(element.getTextContent());
				}
				if ("status1".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setDayStatus(element.getTextContent());
				}
				if ("status2".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setNightStatus(element.getTextContent());
				}
				if ("direction1".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setWindDayDrection(element.getTextContent());
				}
				if ("direction2".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject
							.setWindNightDrection(element.getTextContent());
				}
				if ("power1".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setWindDayPower(element.getTextContent());
				}
				if ("power2".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setWindNightPower(element.getTextContent());
				}
				if ("temperature1".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setHighTemprature(Integer.parseInt(element
							.getTextContent()));
				}
				if ("temperature2".equals(element.getNodeName().toString()
						.toLowerCase())) {
					weatherObject.setLowTemprature(Integer.parseInt(element
							.getTextContent()));
				}
				if ("savedate_weather".equals(element.getNodeName().toString()
						.toLowerCase())) {
					String dates[]=element.getTextContent().split("-");
					weatherObject.setCurrentDate(dates[0]+"年"+dates[1]+"月"+dates[2]+"日");
				}
			}
		
	}
	private void init() {
		pictrueMap1=new HashMap<String, Integer>();
		pictrueMap2=new HashMap<String, Integer>();
		
		pictrueMap1.put("大雨", R.drawable.dby);
		pictrueMap1.put("暴雨", R.drawable.dby);
		pictrueMap1.put("冻雨", R.drawable.dy);
		pictrueMap1.put("大雪", R.drawable.dx);
		pictrueMap1.put("暴雪", R.drawable.bx);
		pictrueMap1.put("多云", R.drawable.dy);
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
		pictrueMap2.put("雾", R.drawable.w);
		pictrueMap2.put("小雪", R.drawable.xx);
		pictrueMap2.put("小雨", R.drawable.xy);
		pictrueMap2.put("阴", R.drawable.y);
		pictrueMap2.put("晴", R.drawable.q0);
		pictrueMap2.put("雨夹雪", R.drawable.yjx);
		pictrueMap2.put("中雨", R.drawable.zhy);
		pictrueMap2.put("中雪", R.drawable.zx);
		pictrueMap2.put("阵雨", R.drawable.zy0);
		
		String xml="";
		File f=new File(DATA_PATH);
		if(f.exists()){
			xml=FileOperator.readFile(DATA_PATH+this.XML_NAME);
			try {
				System.out.println(xml);
				parseXml(xml);
				updateContent();
				
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(MainActivity.this, "尚未获得天气信息，请定位并刷新天气信息", Toast.LENGTH_SHORT).show();
			}
		}		
	}

}
