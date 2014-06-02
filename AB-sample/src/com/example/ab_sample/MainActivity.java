package com.example.ab_sample;

import ab.tester.ABTester;
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
		// this event will be sent only once, several calls will do nothing
		ABTester.recordEvent("SampleProject_I_AM_IN", true);
		
		TextView title = (TextView) findViewById(R.id.title);
		String titleT = ABTester.getString("SampleProject", "TITLE", "title_def");
		title.setText(titleT);
		
		Button btna = (Button) findViewById(R.id.btn_a);
		btna.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ABTester.recordEvent("WANT_BTN", false);
			}
		});
		
		Button btnb = (Button) findViewById(R.id.btn_b);
		btnb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ABTester.recordEvent("DONT_WANT_BTN", false);
			}
		});
		
		String texta = ABTester.getString("SampleProject", "WANT_BTN", "btn_a_def");
		btna.setText(texta);
		String textb = ABTester.getString("SampleProject", "DONT_WANT_BTN", "btn_b_def");
		btnb.setText(textb);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		ABTester.submitEvents();
	}
}