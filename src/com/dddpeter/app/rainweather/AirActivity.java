package com.dddpeter.app.rainweather;


import java.util.ArrayList;
import java.util.HashMap;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.dddpeter.app.rainweather.object.ParamApplication;

public class AirActivity extends FinalActivity {
	@ViewInject(id = R.id.listViewAir)
	ListView content;

	final int REFRESH_MSG=0x1000;
	 String airDetail; 
	final  Handler refreshHandler=new Handler(){
		public void handleMessage(Message msg){
			if(msg.what==REFRESH_MSG){
				try {
					ParamApplication application=(ParamApplication) AirActivity.this.getApplicationContext();
					
					updateContent(application.getAirInfo());
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("refreshHandler", "加载PM2.5信息失败");
				}
			}
		}

		
	};
final Runnable updateRunner=new Runnable(){

	@Override
	public void run() {
		Message msg=new Message();
		msg.what=REFRESH_MSG;
		refreshHandler.sendMessage(msg);
				
	}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_air);
		updateRunner.run();
	}
	@Override
	protected void onResume() {
			Log.v("知雨天气", "开始刷新空气质量面板");
			//Toast.makeText(AirActivity.this, "刷新空气质量面板", Toast.LENGTH_SHORT).show();
			updateRunner.run();	
		super.onResume();
	}
	private void updateContent(String airDetail) {
		String[] lines=(airDetail).split("\r\n");
		  //生成动态数组，加入数据  
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(); 
        for(String line:lines){
        	HashMap<String, Object> map = new HashMap<String, Object>();
        	map.put("info", line);
        	listItem.add(map);  
        }
        //生成适配器的Item和动态数组对应的元素  
        SimpleAdapter listItemAdapter = new SimpleAdapter(this,listItem,//数据源   
            R.layout.listview_item,//ListItem的XML实现  
            //动态数组与ImageItem对应的子项          
            new String[] {"info"},   
            new int[] {R.id.textViewItemcontent}  
        );  
         
        //添加并且显示  
        content.setAdapter(listItemAdapter);  
        
		
	}

}
