package com.cis350.sleeptracker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String AWAKE = "AWAKE";
	private static final String ASLEEP = "ASLEEP";
	private static final String MAIN = "main";
	private static final String IS_ASLEEP = "is_asleep";
	private static final String LAST_LAUNCH = "last_launch";
	private static final String TIP_POSITION = "tip_position";
	private static final String RECENT_SLEEP_TIME = "recent_sleep_time";
	private static final String IS_NAP = "is_nap";
	
	private SharedPreferences mPreferences;
	private TextView mStatus;
	private Button mSleepWakeButton;
	private SleepLogHelper mSleepLogHelper;
	private Context mContext;
	
	private static MediaPlayer mPodcastPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		customizeActionBar(this);
		
		mPreferences = getSharedPreferences(MAIN, MODE_PRIVATE);
		mSleepWakeButton = (Button) findViewById(R.id.sleep_wake_button);
		mStatus = (TextView) findViewById(R.id.status);
		if (!mPreferences.getBoolean(IS_ASLEEP, false)) {
			mSleepWakeButton.setText(getResources().getString(R.string.go_to_sleep));
			mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
		} else {
			mSleepWakeButton.setText(getResources().getString(R.string.wake_up));
			mStatus.setText(getResources().getString(R.string.status) + " " + ASLEEP);
		}
		mSleepLogHelper = new SleepLogHelper(this);
		mContext = this;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TextView tip = (TextView) findViewById(R.id.tip);
		String[] tips = getResources().getString(R.string.tips).split(":");
		tip.setText(getResources().getString(R.string.tip_of_day) + " " + getTip(tips));
	}
	
	private String getTip(String[] tips) {
		SharedPreferences.Editor editor = mPreferences.edit();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(System.currentTimeMillis());
		int date = cal.get(Calendar.DAY_OF_MONTH);
		int lastDate = mPreferences.getInt(LAST_LAUNCH, -1);
		int position = mPreferences.getInt(TIP_POSITION, -1);
		if (lastDate == -1) {
			editor.putInt(LAST_LAUNCH, date);
			editor.putInt(TIP_POSITION, 0);
			editor.commit();
			return tips[0];
		} else if (date != lastDate) {
			int newPosition = ++position;
			if (newPosition >= tips.length) {
				newPosition = 0;
			}
			editor.putInt(LAST_LAUNCH, date);
			editor.putInt(TIP_POSITION, newPosition);
			editor.commit();
			return tips[newPosition];
		} else {
			return tips[position];
		}
	}
	
	public void onClickSleepOrWake(View view) {
		if (!mPreferences.getBoolean(IS_ASLEEP, false)) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(IS_ASLEEP, true);
			editor.putLong(RECENT_SLEEP_TIME, System.currentTimeMillis());
			editor.commit();
			mSleepWakeButton.setText(getResources().getString(R.string.wake_up));
			mStatus.setText(getResources().getString(R.string.status) + " " + ASLEEP);
			displayDialogs();
		} else {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(IS_ASLEEP, false);
			editor.commit();
			mSleepWakeButton.setText(getResources().getString(R.string.go_to_sleep));
			mStatus.setText(getResources().getString(R.string.status) + " " + AWAKE);
			mSleepLogHelper.insertLog(mPreferences.getLong(RECENT_SLEEP_TIME, 0),
					System.currentTimeMillis(), mPreferences.getBoolean(IS_NAP, false));
			if (mPodcastPlayer != null) {
				mPodcastPlayer.stop();
				mPodcastPlayer.release();
			}
		}
	}
	
	private void displayDialogs() {
		AlertDialog.Builder podcastDialogBuilder = new AlertDialog.Builder(mContext);
		podcastDialogBuilder.setTitle("Podcast");
		podcastDialogBuilder.setMessage("Would you like to listen to the Podcast?");
		podcastDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mPodcastPlayer = MediaPlayer.create(mContext, R.raw.shs_podcast);
				mPodcastPlayer.start();
				dialog.dismiss();
			}
		});
		podcastDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
		final AlertDialog podcastAlertDialog = podcastDialogBuilder.create();
		
		AlertDialog.Builder napDialogBuilder = new AlertDialog.Builder(mContext);
		napDialogBuilder.setTitle("Nap or Nighttime Sleep");
		napDialogBuilder.setMessage("Are you taking a nap or going to sleep for the night?");
		napDialogBuilder.setPositiveButton("Nighttime Sleep", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(IS_NAP, false);
				editor.commit();
				podcastAlertDialog.show();
			}
		});
		napDialogBuilder.setNegativeButton("Nap",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,int id) {
				SharedPreferences.Editor editor = mPreferences.edit();
				editor.putBoolean(IS_NAP, true);
				editor.commit();
				podcastAlertDialog.show();
			}
		});
		AlertDialog napAlertDialog = napDialogBuilder.create();
		napAlertDialog.show();
	}
	
	public void onClickData(View view) {
		Intent intent = new Intent(this, DataActivity.class);
		startActivity(intent);
	}
	public void onClickGraph(View view){
		Intent intent = new Intent(this, ChartActivity.class);
		startActivity(intent);
	}
	
	public static void customizeActionBar(Activity activity) {
		// Customize Action Bar
		activity.getActionBar().setDisplayShowCustomEnabled(true);
		activity.getActionBar().setDisplayShowTitleEnabled(false);
		LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.view_action_bar, null);
		((TextView)v.findViewById(R.id.title)).setText(activity.getResources().getString(R.string.action_bar_title));
		activity.getActionBar().setCustomView(v);
	}
}
