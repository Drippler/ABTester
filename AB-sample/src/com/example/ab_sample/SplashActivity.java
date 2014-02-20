package com.example.ab_sample;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import ab.tester.ABTest;
import ab.tester.ABTesterSafe;
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
		ABTesterSafe.init(this, "public", "private");
		
		ExecutorService ex = Executors.newSingleThreadExecutor();
		ex.execute(new Runnable() {
			@Override
			public void run() {
				int msTimeout = (int)TimeUnit.SECONDS.toMillis(15);
				long start = SystemClock.uptimeMillis();
				try {
					// We will pull the vars TITLE, WANT_BTN and DONT_WANT_BTN with lock set to FALSE - thats mean that those vars can be modified 
					// from the exp dashboard
					ABTesterSafe.syncPreFetch(msTimeout, new ABTest("SampleProject", false, "TITLE", "WANT_BTN", "DONT_WANT_BTN"));
					Log.v("ABTesterSample", "synced within " + (SystemClock.uptimeMillis() - start) + "ms");
				} catch (TimeoutException e) {
					Log.v("ABTesterSample", "timed out with " + msTimeout);
				}
				
				startActivity(new Intent(SplashActivity.this, MainActivity.class));
				finish();
			}
		});
		ex.shutdown();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		ABTesterSafe.submitEvents();
	}
}
