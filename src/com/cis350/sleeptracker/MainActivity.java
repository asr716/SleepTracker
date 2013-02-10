package com.cis350.sleeptracker;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String AWAKE = "AWAKE";
	private static final String ASLEEP = "ASLEEP";
	private TextView mStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mStatus = (TextView) findViewById(R.id.status);
		mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onClickWake(View view) {
		mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
	}
	
	public void onClickSleep(View view) {
		mStatus.setText(getResources().getString(R.string.status) + " " + ASLEEP);
	}
}
