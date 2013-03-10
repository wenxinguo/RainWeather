package com.dddpeter.app.rainweather;

import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.annotation.view.ViewInject;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.view.Menu;
import android.widget.TextView;

public class AboutActivity extends FinalActivity {
	@ViewInject(id = R.id.info) TextView textAbout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		String html="<center><h1>知雨天气</h1></center>" +
				"<p> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 本软件为个人作品，主要功能是通过GPS或者A-GPS定位从网络获取天气信息，" +
				"以及部分城市的PM2.5信息(有些城市暂未发布PM2.5)，个人交流使用，不用于商业用途。" +
				"<p><h5>作者：烈焰之雨</h5>" +
				"<p><h5>电子邮件：dddpeter@126.com</h5>";
		textAbout.setText(Html.fromHtml(html));
	}

}
