package com.dddpeter.app.rainweather;


import java.util.HashMap;

import com.dddpeter.app.rainweather.object.ParamApplication;
import com.dddpeter.app.rainweather.util.FileOperator;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SplashScreenActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
 
		setContentView(R.layout.activity_splash_screen);
	   //启动动画持续1秒钟
			  new Handler().postDelayed(new Runnable() {
				   @Override
			  public void run() {
				  Intent intent = new Intent(SplashScreenActivity.this,IndexActivity.class);  //从启动动画ui跳转到主ui
			    startActivity(intent);
			    SplashScreenActivity.this.finish();    // 结束启动动画界面
			   }
			  }, 2000);    //启动动画持续2秒钟
			  
		 }
}

