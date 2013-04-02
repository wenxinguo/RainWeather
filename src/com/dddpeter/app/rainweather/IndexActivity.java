package com.dddpeter.app.rainweather;



import java.io.File;
import java.util.Iterator;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.dddpeter.app.rainweather.object.ParamApplication;
import com.dddpeter.app.rainweather.util.FileOperator;

@SuppressWarnings("deprecation")
public class IndexActivity extends FinalActivity {
	@Override
	protected void onResume() {
		super.onResume();
	}

	@ViewInject(id = R.id.radioGroup1)
	RadioGroup rg;
	@ViewInject(id = R.id.radio0)
	RadioButton rb1;
	@ViewInject(id = R.id.radio1)
	RadioButton rb2;
	@ViewInject(id = R.id.radio2)
	RadioButton rb3;
	@ViewInject(id = R.id.radio3)
	RadioButton rb4;
	@ViewInject(id =android.R.id.tabhost )
	TabHost tabHost;
	// 内容Intent
	private Intent todayIntent;
	private Intent recentIntent;
	private Intent airIntent;
	private Intent aboutIntent;
	 LocalActivityManager activityGroup;
	 private LocationClient mLocClient;
		private BDLocation myLocation;
		private final String DATA_PATH=Environment.getExternalStorageDirectory().getPath()+"/tmp/";
		private final String DATA_NAME="weather.json";
		private final String DATA_DETAIL_NAME="weather_detail.json";
		private final String DATA_AIR="air.txt";
		ProgressDialog mDialog;
	
		BDLocationListener myListener = new BDLocationListener() {
			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null)
					return;
				myLocation=location;
				 mLocClient.stop();
				 getInfos(myLocation.getDistrict(),myLocation.getCity());
				 //更新面板
				 ParamApplication application=(ParamApplication) IndexActivity.this.getApplicationContext();
				application.setRefreshed(true);
				
				 activityGroup.dispatchResume();
				
			}

			@Override
			public void onReceivePoi(BDLocation poiLocation) {
				System.out.println("poiLocation");
				if (poiLocation == null) {
					return;
				}
				myLocation=poiLocation;
				 mLocClient.stop();
				getInfos(myLocation.getDistrict(),myLocation.getCity());
				ParamApplication application=(ParamApplication) IndexActivity.this.getApplicationContext();
				application.setRefreshed(true);
			
				 //更新面板
				activityGroup.dispatchResume();
			}

		};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_index);
		String strVer=android.os.Build.VERSION.RELEASE;
		strVer=strVer.substring(0,3).trim();
		float fv=Float.valueOf(strVer);
		if(fv>2.3)
		{
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads()
		.detectDiskWrites()
		.detectNetwork() // 这里可以替换为detectAll() 就包括了磁盘读写和网络I/O
		.penaltyLog() //打印logcat，当然也可以定位到dropbox，通过文件保存相应的log
		.build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects() //探测SQLite数据库操作
		.penaltyLog() //打印logcat
		.penaltyDeath()
		.build()); 
		}
		
		activityGroup = new LocalActivityManager(this,
				    true);
		 activityGroup.dispatchCreate(savedInstanceState);
		 
		  this.tabHost.setup(activityGroup);
		  prepareIntent();
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup rg, int id) {
				ParamApplication application=(ParamApplication) IndexActivity.this.getApplicationContext();
				if (id == rb1.getId()) {
					tabHost.setCurrentTabByTag(application.getTAB_TAG_TODAY());
				}
				else if (id == rb2.getId()) {
					tabHost.setCurrentTabByTag(application.getTAB_TAG_RECENT());
				}
				else if (id == rb3.getId()) {
					tabHost.setCurrentTabByTag(application.getTAB_TAG_AIR());
				}
				else if (id == rb4.getId()) {
					tabHost.setCurrentTabByTag(application.getTAB_TAG_ABOUT());
				}

			}

		});
		
	}
private void prepareIntent() {
	 todayIntent=new Intent(this, TodayActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 recentIntent=new Intent(this, RecentActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 airIntent=new Intent(this, AirActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 aboutIntent=new Intent(this, AboutActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	 ParamApplication application=(ParamApplication) IndexActivity.this.getApplicationContext();
		TabHost localTabHost=this.tabHost;
		localTabHost.addTab(buildTabSpec(application.getTAB_TAG_TODAY(), R.string.tab1, R.drawable.home, todayIntent));
		localTabHost.addTab(buildTabSpec(application.getTAB_TAG_RECENT(), R.string.tab2, R.drawable.recent, recentIntent));
		localTabHost.addTab(buildTabSpec(application.getTAB_TAG_AIR(), R.string.tab3, R.drawable.air, airIntent));
		localTabHost.addTab(buildTabSpec(application.getTAB_TAG_ABOUT(), R.string.tab4, R.drawable.about, aboutIntent));
		
	}

	

	private TabSpec buildTabSpec(String tabTag, int titleResourceID, int iconResourceID,
		Intent intent) {
		TabHost.TabSpec spec = this.tabHost.newTabSpec(tabTag);
		  spec.setContent(intent);
		  spec.setIndicator(getResources().getString(titleResourceID),
		    getResources().getDrawable(iconResourceID));
	return spec;
}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
				getMenuInflater().inflate(R.menu.index, menu);
				
		
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item){
		
	 switch(item.getItemId()){
		case R.id.refresh_menu:	
			mDialog=ProgressDialog.show(IndexActivity.this, "请等待...", "正在刷新天气",true);
			new Thread(){
				public void run(){
					try {
			            Thread.sleep(500);
					}catch(Exception e){
						
					}finally{
						mDialog.dismiss();
					}
				}
			}.start();
			 getLocation();
			break;
		case R.id.reset_menu:
			File file1=new File(this.DATA_PATH+this.DATA_DETAIL_NAME);
			File file2=new File(this.DATA_PATH+this.DATA_NAME);
			File file3=new File(this.DATA_PATH+this.DATA_AIR);
			SharedPreferences preferences = getSharedPreferences("night_picture", MODE_PRIVATE);
			preferences.edit().clear();
			preferences.edit().commit(); 
			preferences = getSharedPreferences("day_picture", MODE_PRIVATE);
			preferences.edit().clear();
			preferences.edit().commit();
			if(file1.exists()){
				file1.delete();
			}
			if(file2.exists()){
				file2.delete();
			}
			if(file3.exists()){
				file3.delete();
			}
			file1=file2=file3=null;
			
			
			
			
			System.gc();
		case R.id.exit_menu:
			System.exit(0);
		}
		return false;	
	}
   
   public void getLocation(){
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);
		Log.v("知雨天气定位", "开始进行定位:" + (mLocClient != null));
		LocationManager alm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		LocationClientOption option = new LocationClientOption();
		Log.v("知雨天气定位",
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
		Log.v("检查是否启动百度定位", "" + mLocClient.isStarted());
		if (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			mLocClient.requestLocation();
		} else {
			mLocClient.requestPoi();
		}
		
		
		//mLocClient.unRegisterLocationListener(myListener);
		//System.gc();
	}
	public void getInfos(String district,String city){
		if(district==null || district.trim().equals("")){
			Toast.makeText(this, "定位失败，请打开网络重试", Toast.LENGTH_SHORT).show();
			setNetworkMethod(this);
			return;
		}
		System.out.println(district);
		String cityStr =getValidDistrictName(district);
		String cityEn=this.getVaildCityEnglishName(city);
		String resultJSON = "";
		String resultJSON0 = "";
		String resultJSON1 = "";
		try {
			
			 SharedPreferences preferences = getSharedPreferences("districts", MODE_PRIVATE);
			Log.v("知雨天气", "获取" + cityStr + "天气");
			final String link0="http://m.weather.com.cn/data/"+preferences.getString(cityStr,"101010100")+".html"; //详细信息
			final String link = "http://www.weather.com.cn/data/sk/"+preferences.getString(cityStr,"101010100")+".html"; //简要信息
		    final String link1="http://m.pm2d5.com/pm/"+cityEn+".html";//空气指数
			resultJSON0=getJSONHttp(link0,"UTF-8");
			resultJSON=getJSONHttp(link,"UTF-8");
			if(!"".equals(cityEn.trim())){
			resultJSON1=getAirHttp(link1,"UTF-8");
			if(resultJSON1==null || "<h6>暂时没有此地区的PM2.5信息或者获取信息失败</h6>".equals(resultJSON1.trim())){
				resultJSON1="<h6>暂时没有此地区的PM2.5信息或者获取信息失败</h6>";
			}
			else
			{
				org.jsoup.nodes.Document doc =Jsoup.parse(resultJSON1);
				Elements currentStatus=doc.select("div.top_info");
				Elements adviceInfos=doc.select("div.top_info1");
				System.out.println(adviceInfos.get(0).text());
				 Iterator<Element> advices=adviceInfos.iterator();
				resultJSON1=currentStatus.first().text()+"\r\n";
				while(advices.hasNext()){
					String item=advices.next().text();
					if(item.contains("切换城市")){
						item=item.replace("[切换城市]", "");
					}
					resultJSON1=resultJSON1+item+"\r\n";
				}
			}
			}
			else
			{
				resultJSON1="<h6>暂时没有此地区的PM2.5信息或者获取信息失败</h6>";
			}
			ParamApplication application=(ParamApplication) this.getApplicationContext();
			application.setAirInfo(resultJSON1);
			FileOperator.saveFile(link0+"\n"+link+"\n"+link1, this.DATA_PATH,"temp.txt");
			FileOperator.saveFile(resultJSON, this.DATA_PATH,this.DATA_NAME);
			FileOperator.saveFile(resultJSON0, this.DATA_PATH,this.DATA_DETAIL_NAME);
			FileOperator.saveFile(resultJSON1, this.DATA_PATH,this.DATA_AIR);
		}catch(Exception e){
			e.printStackTrace();
			String temp="";
			StackTraceElement[] test=e.getStackTrace();
			for(int i=0;i<test.length;i++){
				temp=temp+test[i];
			}
			Toast.makeText(this,"获取天气信息失败"+temp, Toast.LENGTH_LONG).show();
		}
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
	private String getAirHttp(String link,String charSet){
		HttpGet httpRequest = new HttpGet(link);
		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse httpResponse;
		try {
			httpResponse = httpclient.execute(httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return  EntityUtils.toString(httpResponse.getEntity(),
						"UTF-8");
			}
			else{
				
				return "<h6>暂时没有此地区的PM2.5信息或者获取信息失败</h6>";
				
			}	
		} catch (Exception e) {
			e.printStackTrace();
			return "<h6>暂时没有此地区的PM2.5信息或者获取信息失败</h6>";
		} 

		
	}
	private String getValidDistrictName(String district) {
		String result;
		int i=2;
		 SharedPreferences preferences = getSharedPreferences("districts", MODE_PRIVATE);
		while(true){
			try{
			if(preferences.contains(result=district.substring(0, i))){
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
	private String getVaildCityEnglishName(String city){
		String result;
		String temp;
		int i=2;
		 SharedPreferences preferences = getSharedPreferences("cities", MODE_PRIVATE);
		while(true){
			try{
				
			if(preferences.contains(temp=city.substring(0, i))){
				result=preferences.getString(temp,"beijing");
				break;
			}
			else{
				i++;
			}
			}catch(Exception e){
				result="";
			}
			
		}
		return result;
		
	}
	 /*
     * 打开设置网络界面
     * */
    public void setNetworkMethod(final Context context){
        //提示对话框
        AlertDialog.Builder builder=new Builder(context);
        builder.setTitle("网络设置提示").setMessage("网络连接不可用,是否进行设置?").setPositiveButton("无线设置", new DialogInterface.OnClickListener() {
            
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
        }).setNeutralButton("Wifi设置", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Intent intent=null;
                //判断手机系统的版本  即API大于10 就是3.0或以上版本 
                if(android.os.Build.VERSION.SDK_INT>10){
                    intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                }else{
                    intent = new Intent();
                    ComponentName component = new ComponentName("com.android.settings","com.android.settings.wifi.WifiSettings");
                    intent.setComponent(component);
                    intent.setAction("android.intent.action.VIEW");
                }
                context.startActivity(intent);
			}}).show();
    }
    
    
	
}
