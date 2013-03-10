package com.dddpeter.app.rainweather;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;

import com.dddpeter.app.rainweather.object.ParamApplication;
import com.dddpeter.app.rainweather.util.FileOperator;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
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
public class TodayActivity extends FinalActivity{

	




	@ViewInject(id = R.id.imageView1)
	ImageView image;
	@ViewInject(id = R.id.content)
	TextView content;
	

	private JSONObject weatherObject=new JSONObject();
	private JSONObject weatherObjectDetail=new JSONObject();
	private final String DATA_PATH="/sdcard/tmp/";
	private final String DATA_NAME="weather.json";
	private final String DATA_DETAIL_NAME="weather_detail.json";
	
	final  Handler refreshHandler=new Handler(){
		public void handleMessage(Message msg){
			if(msg.what==REFRESH_MSG){
				try {
					updateContent(weatherObject,weatherObjectDetail);
					ParamApplication application=(ParamApplication) TodayActivity.this.getApplicationContext();
					application.setRefreshed(false);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("refreshHandler", "加载天气信息失败");
				}
			}
		}
	};
	final Runnable updateRunner=new Runnable(){
		
		File f=new File(DATA_PATH+DATA_NAME);
		File f0=new File(DATA_PATH+DATA_DETAIL_NAME);
		String json="";
		String json0="";
		@Override
		public void run() {
			if(f.exists()){
				json=FileOperator.readFile(DATA_PATH+DATA_NAME);
				if(f0.exists()){
				     json0=FileOperator.readFile(DATA_PATH+DATA_DETAIL_NAME);
				     try {
				    		ParamApplication application=(ParamApplication) TodayActivity.this.getApplicationContext();
				
				    	    weatherObject=new JSONObject(json);
				    	    weatherObjectDetail=new JSONObject(json0);
				    	    application.setWeatherObject(weatherObject);
				    	    application.setWeatherObjectDetail(weatherObjectDetail);
							Message msg=new Message();
							msg.what=REFRESH_MSG;
							refreshHandler.sendMessage(msg);
							
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(TodayActivity.this, "尚未获得天气信息，请定位并刷新天气信息", Toast.LENGTH_SHORT).show();
						}
				}
				
				
			}
			
		}};
	final int REFRESH_MSG=0x1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_today);
		//Toast.makeText(TodayActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
		
		updateRunner.run();		
		
	}
	
	@Override
	protected void onResume() {
		ParamApplication application=(ParamApplication) TodayActivity.this.getApplicationContext();
		if(application.isRefreshed()){
			Log.v("知雨天气", "开始刷新天气面板");
			Toast.makeText(TodayActivity.this, "刷新天气面板", Toast.LENGTH_SHORT).show();
			updateRunner.run();	
		}
		ActivityManager mActivityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE); 
		List<RunningServiceInfo> list=mActivityManager.getRunningServices(50);
		/*for(int i=0;i<list.size();i++){
			if(list.get(i).service.getClassName().contains("com.baidu.location.f")){
				Intent  intent = new Intent();  
				//设置部件  
				intent.setComponent(list.get(i).service);  
				System.out.println("停止定位服务："+stopService(intent));
			}
		}*/
		super.onResume();
	}




	@SuppressWarnings("deprecation")
	public void updateContent(JSONObject weatherObject,JSONObject weatherObjectDetail) throws JSONException, IOException{
		JSONObject temp=weatherObject.getJSONObject("weatherinfo");
		JSONObject temp0=weatherObjectDetail.getJSONObject("weatherinfo");
		String result="";
		Date time=new Date();
		String isNight="day_picture";

		try{
			if(time.getHours()<6 || time.getHours()>=18){
				isNight="night_picture";
			}
			if(!temp0.getString("weather1").contains("转")){

				
				result=temp0.getString("weather1");
			}
			else
			{
				
				result=temp0.getString("weather1").split("转")[0];
				
			}
		StringBuffer sb=new StringBuffer();
		sb.append("<h2>"+temp.getString("city")+"</h2>");
		sb.append(temp.getString("temp")+"℃<br/>");
		sb.append(temp0.getString("date_y")+" "+temp0.getString("week")
				+"<br/>");
		
		sb.append("天气："+result+"<br/>");
		sb.append("温度："+temp0.getString("temp1")+"<br/>");
		sb.append("风向："+temp.getString("WD")+"<br/>");
		sb.append("风力："+temp0.getString("fl1")+"<br/>");
		sb.append("湿度："+temp.getString("SD")+"<br/>");
		sb.append("更新时间："+temp.getString("time"));
		content.setText(Html.fromHtml(sb.toString()));
		
		
		
			
			 SharedPreferences preferences = getSharedPreferences(isNight, MODE_PRIVATE);
			 System.out.println(result);
			String path=preferences.getString(result,null);
			if(path==null){
				path="notclear.png";
			}
			Bitmap bit=readBitMap(this,path);
			image.setImageBitmap(bit);
			
		
			
		}catch(Exception e){
			e.printStackTrace();
			Bitmap bit=readBitMap(this,"notclear.png");
			image.setImageBitmap(bit);
			
		}finally{
			System.gc();
		}
	
		Toast.makeText(TodayActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
	}
	public static Bitmap readBitMap(Context context, int resId){  
	      BitmapFactory.Options opt = new BitmapFactory.Options();  
	      opt.inPreferredConfig = Bitmap.Config.RGB_565;   
	      opt.inPurgeable = true;  
	      opt.inInputShareable = true;  
	//获取资源图片  
	      InputStream is = context.getResources().openRawResource(resId);  
	      return BitmapFactory.decodeStream(is,null,opt);  
	 }
	public static Bitmap readBitMap(Context context, String path) throws IOException{  
	      BitmapFactory.Options opt = new BitmapFactory.Options();  
	      opt.inPreferredConfig = Bitmap.Config.RGB_565;   
	      opt.inPurgeable = true;  
	      opt.inInputShareable = true;  
	//获取资源图片  
	      InputStream is = context.getResources().getAssets().open(path);  
	      return BitmapFactory.decodeStream(is,null,opt);  
	 }
	
	
}
