package com.mogu.wifilocation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class SplashAcyivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		Handler handler = new Handler();
        handler.postDelayed(r, 3000);
	}
	
	Runnable r = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent();
			intent.setClass(SplashAcyivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	};

}
