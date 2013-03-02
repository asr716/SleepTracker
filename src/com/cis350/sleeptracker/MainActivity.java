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
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String AWAKE = "AWAKE";
	private static final String ASLEEP = "ASLEEP";
	private boolean mIsAsleep;
	private TextView mStatus;
	private Button mSleepWakeButton;
	private SleepLogHelper mSleepLogHelper;
	private long mRecentSleepTime;
	private Context context;
	private MediaPlayer podcastPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mIsAsleep = false;
		mSleepWakeButton = (Button) findViewById(R.id.sleep_wake_button);
		mSleepWakeButton.setText(getResources().getString(R.string.go_to_sleep));
		mStatus = (TextView) findViewById(R.id.status);
		mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
		mSleepLogHelper = new SleepLogHelper(this);
		//mSleepLogHelper.deleteAllEntries();
		context = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
		
	}
	
	public void onClickSleepOrWake(View view) {
		if (!mIsAsleep) {
			mIsAsleep = true;
			mSleepWakeButton.setText(getResources().getString(R.string.wake_up));
			mStatus.setText(getResources().getString(R.string.status) + " " + ASLEEP);
			mRecentSleepTime = System.currentTimeMillis();
			
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			alertDialogBuilder.setTitle("Podcast");
			alertDialogBuilder.setMessage("Would you like to listen to the Podcast?");
			alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// if this button is clicked play podcast
					podcastPlayer = MediaPlayer.create(context, R.raw.shs_podcast);
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
		} else {
			mIsAsleep = false;
			mSleepWakeButton.setText(getResources().getString(R.string.go_to_sleep));
			mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
			mSleepLogHelper.insertLog(mRecentSleepTime, System.currentTimeMillis());
			if (podcastPlayer != null) {
				podcastPlayer.release();
			}
		}
	}
	
	public void onClickData(View view) {
		Intent intent = new Intent(this, DataActivity.class);
		startActivity(intent);
	}
}
