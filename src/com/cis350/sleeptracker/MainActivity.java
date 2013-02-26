package com.cis350.sleeptracker;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String AWAKE = "AWAKE";
	private static final String ASLEEP = "ASLEEP";
	private TextView mStatus;
	private SleepLogHelper mSleepLogHelper;
	private long mRecentSleepTime;
	private Context context;
	private MediaPlayer podcastPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mStatus = (TextView) findViewById(R.id.status);
		mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
		mSleepLogHelper = new SleepLogHelper(this);
		context = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
		
	}

	public void onClickWake(View view) {
		mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
		mSleepLogHelper.updateAwakeTime(mRecentSleepTime, System.currentTimeMillis());
		podcastPlayer.release();
	}
	
	public void onClickSleep(View view) {
		mStatus.setText(getResources().getString(R.string.status) + " " + ASLEEP);
		mRecentSleepTime = System.currentTimeMillis();
		mSleepLogHelper.insertLog(mRecentSleepTime);
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		
		alertDialogBuilder.setTitle("Podcast");
		alertDialogBuilder.setMessage("Would you like to listen to the Podcast?");
		alertDialogBuilder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked play podcast
				podcastPlayer = MediaPlayer.create(context, R.raw.podcast_file);
				podcastPlayer.start();
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				// if this button is clicked, just close
				// the dialog box and do nothing
				dialog.cancel();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	
	public void onClickData(View view) {
		Intent intent = new Intent(this, DataActivity.class);
		startActivity(intent);
	}
}
