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
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
	protected static final String MAIN = "main";
	protected static final String IS_ASLEEP = "is_asleep";
	private static final String LAST_LAUNCH = "last_launch";
	private static final String TIP_POSITION = "tip_position";
	private static final String RECENT_SLEEP_TIME = "recent_sleep_time";
	private static final String IS_NAP = "is_nap";
	
	private SharedPreferences mPreferences;
	private LinearLayout mMainLinearLayout;
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
		mMainLinearLayout = (LinearLayout) findViewById(R.id.main_linear_layout);
		mSleepWakeButton = (Button) findViewById(R.id.sleep_wake_button);
		if (!mPreferences.getBoolean(IS_ASLEEP, false)) {
			mMainLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color_awake));
			mSleepWakeButton.setText(getResources().getString(R.string.go_to_sleep));
		} else {
			mMainLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color));
			mSleepWakeButton.setText(getResources().getString(R.string.wake_up));
		}
		mSleepLogHelper = new SleepLogHelper(this);
		mContext = this;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		String[] tips = getResources().getString(R.string.tips).split(":");
		TextView tip = (TextView) findViewById(R.id.tip);
		tip.setText(getTip(tips));
	}
	
	/*
	 * Takes in an array of tips and returns which one to display.  A different tip is displayed each day.  
	 * So if the last date recorded is not the current date, the next tip in the array should be returned.
	 * Otherwise, the tip at the saved position of the array should be returned.
	 */
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
	
	/*
	 * Method called when the large button is clicked to indicate going to sleep or waking up.
	 * Method changes background color and button text.
	 * When going to sleep, it also records the time and calls a method to prompt dialogs.
	 * When waking up, it also inserts a new sleep log into the local database, kills the podcast,
	 * and starts the LogActivity so that the user can rate and comment on his/her sleep.
	 */
	public void onClickSleepOrWake(View view) {
		if (!mPreferences.getBoolean(IS_ASLEEP, false)) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(IS_ASLEEP, true);
			editor.putLong(RECENT_SLEEP_TIME, System.currentTimeMillis());
			editor.commit();
			mMainLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color));
			mSleepWakeButton.setText(getResources().getString(R.string.wake_up));
			displayDialogs();
		} else {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(IS_ASLEEP, false);
			editor.commit();
			mMainLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color_awake));
			mSleepWakeButton.setText(getResources().getString(R.string.go_to_sleep));
			mSleepLogHelper.insertLog(mPreferences.getLong(RECENT_SLEEP_TIME, 0),
					System.currentTimeMillis(), mPreferences.getBoolean(IS_NAP, false));
			if (mPodcastPlayer != null) {
				mPodcastPlayer.stop();
				mPodcastPlayer.release();
			}
			Intent intent = new Intent(mContext, LogActivity.class);
			intent.putExtra(DataActivity.ITEM_ASLEEP_TIME_LONG, mPreferences.getLong(RECENT_SLEEP_TIME, 0));
			startActivity(intent);
		}
	}
	
	/*
	 * Method displays two consecutive dialogs.  The first prompts the user to pick whether he/she is
	 * going to sleep for the night or taking a nap.  The second asks the user whether he/she wants
	 * to listen to the podcast while falling asleep.
	 */
	private void displayDialogs() {
		AlertDialog.Builder podcastDialogBuilder = new AlertDialog.Builder(mContext);
		podcastDialogBuilder.setTitle(getResources().getString(R.string.podcast_dialog_title));
		podcastDialogBuilder.setMessage(getResources().getString(R.string.podcast_dialog_message));
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
		napDialogBuilder.setTitle(getResources().getString(R.string.nap_dialog_title));
		napDialogBuilder.setMessage(getResources().getString(R.string.nap_dialog_message));
		napDialogBuilder.setPositiveButton("Sleep", new DialogInterface.OnClickListener() {
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
		((TextView)v.findViewById(R.id.title)).setText("");
		activity.getActionBar().setCustomView(v);
	}
}
