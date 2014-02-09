package com.example.ab_sample;

import ab.tester.ABTesterSafe;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// send event that we are part of the test, it will be send only if the data for this test is synced
		ABTesterSafe.recordEvent("SampleProject", "SampleProject_I_AM_IN", true);
		
		
		TextView title = (TextView) findViewById(R.id.title);
		String titleT = ABTesterSafe.getString("SampleProject", "TITLE", "title_def");
		title.setText(titleT);
		
		Button btna = (Button) findViewById(R.id.btn_a);
		btna.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ABTesterSafe.recordEvent("SampleProject", "WANT_BTN", false);
			}
		});
		
		Button btnb = (Button) findViewById(R.id.btn_b);
		btnb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ABTesterSafe.recordEvent("SampleProject", "DONT_WANT_BTN", false);
			}
		});
		
		String texta = ABTesterSafe.getString("SampleProject", "WANT_BTN", "btn_a_def");
		btna.setText(texta);
		String textb = ABTesterSafe.getString("SampleProject", "DONT_WANT_BTN", "btn_b_def");
		btnb.setText(textb);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		ABTesterSafe.submitEvents();
	}
	
}