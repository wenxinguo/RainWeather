package com.dddpeter.app.rainweather;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

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

