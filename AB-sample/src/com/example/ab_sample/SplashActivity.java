package com.example.ab_sample;

import java.util.concurrent.TimeoutException;

import ab.tester.ABTest;
import ab.tester.ABTesterSafe;
import ab.tester.DefualtLogger;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class SplashActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		ABTesterSafe.init(this, "public", "private", new DefualtLogger());
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				int msTimeout = 15000;
				long start = SystemClock.uptimeMillis();
				try {
					ABTesterSafe.syncPreFetch(msTimeout, new ABTest("SampleProject", false, "TITLE","WANT_BTN","DONT_WANT_BTN"));
					Log.v("ABTesterSample", "synced within " + (SystemClock.uptimeMillis() - start) + "ms");
				} catch (TimeoutException e) {
					Log.v("ABTesterSample", "timed out with " + msTimeout);
				}
				
				startActivity(new Intent(SplashActivity.this, MainActivity.class));
				finish();
			}
		}).start();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		ABTesterSafe.submitEvents();
	}

}
