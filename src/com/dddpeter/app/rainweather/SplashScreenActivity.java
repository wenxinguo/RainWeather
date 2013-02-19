package com.dddpeter.app.rainweather;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Properties;

import com.dddpeter.app.rainweather.util.FileOperator;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class SplashScreenActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
	     WindowManager.LayoutParams.FLAG_FULLSCREEN);  
		setContentView(R.layout.activity_splash_screen);
		 new Handler().postDelayed(new Runnable() {
			   @Override
			   public void run() {
			    Intent intent = new Intent(SplashScreenActivity.this,MainActivity.class);  //从启动动画ui跳转到主ui
			    startActivity(intent);
			    SplashScreenActivity.this.finish();    // 结束启动动画界面
			   }
			  }, 500);    //启动动画持续1.5秒钟
		 }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}

}
