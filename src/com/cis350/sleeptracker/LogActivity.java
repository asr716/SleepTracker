package com.cis350.sleeptracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

public class LogActivity extends Activity {
	private static final int[] EXCUSE_CHECKBOXES = {R.id.excuse_checkbox1, R.id.excuse_checkbox2,
		R.id.excuse_checkbox3, R.id.excuse_checkbox4, R.id.excuse_checkbox5, R.id.excuse_checkbox6};
	private static final int[] EXCUSE_IDS = {R.id.excuse1, R.id.excuse2, R.id.excuse3, R.id.excuse4,
		R.id.excuse5, R.id.excuse6};
	
	private SharedPreferences mPreferences;
	private LinearLayout mLinearLayout;
	private long mAsleepTime;
	private long mAwakeTime;
	private SleepLogHelper mSleepLogHelper;
	private SimpleDateFormat mSimpleDateFormat;
	private RatingBar mRatingBar;
	private EditText mCommentBox;
	private String mTypeOfSleep;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		MainActivity.customizeActionBar(this);
		
		mPreferences = getSharedPreferences(MainActivity.MAIN, MODE_PRIVATE);
		mLinearLayout = (LinearLayout) findViewById(R.id.linear_layout);
		if (!mPreferences.getBoolean(MainActivity.IS_ASLEEP, false)) {
			mLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color_awake));
		} else {
			mLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color));
		}
		mAsleepTime = getIntent().getLongExtra(DataActivity.ITEM_ASLEEP_TIME_LONG, 0);
		mAwakeTime = 0;
		mSleepLogHelper = new SleepLogHelper(this);
		mSimpleDateFormat = new SimpleDateFormat("MMM dd hh:mm a", Locale.US);
		mRatingBar = (RatingBar) findViewById(R.id.rating_bar);
		mCommentBox = (EditText) findViewById(R.id.comment_box);
		
		Cursor cursor = mSleepLogHelper.queryLog(mAsleepTime);
		if (cursor != null) {
			cursor.moveToFirst();
			mAwakeTime = cursor.getLong(cursor.getColumnIndex(SleepLogHelper.AWAKE_TIME));
			int rating = cursor.getInt(cursor.getColumnIndex(SleepLogHelper.RATING));
			String comments = cursor.getString(cursor.getColumnIndex(SleepLogHelper.COMMENTS));
			boolean wasNap = cursor.getInt(cursor.getColumnIndex(SleepLogHelper.NAP)) > 0;
			mRatingBar.setRating(rating);
			mCommentBox.setText(comments);
			if (wasNap) {
				mTypeOfSleep = getResources().getString(R.string.nap);
			} else {
				mTypeOfSleep = getResources().getString(R.string.night_sleep);
			}
			for (int i = 0; i < EXCUSE_CHECKBOXES.length; i++) {
				boolean checked = cursor.getInt(cursor.getColumnIndex(SleepLogHelper.EXCUSES[i])) > 0;
				CheckBox checkBox = (CheckBox) findViewById(EXCUSE_CHECKBOXES[i]);
				checkBox.setChecked(checked);
			}
		}
	}
	
	/*
	 * Method formats the asleep time, the awake time, and the total time slept.
	 * It sets this information to be displayed by the corresponding TextViews.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		String fAsleepTime = mSimpleDateFormat.format(new Date(mAsleepTime));
		String fAwakeTime = "-";
		if (mAwakeTime != 0) {
			fAwakeTime = mSimpleDateFormat.format(new Date(mAwakeTime));
		}
		long elapsedTime = TimeUnit.MILLISECONDS.toMinutes(mAwakeTime) -
				TimeUnit.MILLISECONDS.toMinutes(mAsleepTime);
		String totalSleep = String.format(Locale.US, "%d hours, %d minutes",
				TimeUnit.MINUTES.toHours(elapsedTime),
				elapsedTime - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(elapsedTime)));
		if (elapsedTime < 0) {
			totalSleep = getResources().getString(R.string.pending);
		}
		
		TextView totalSleepText = (TextView) findViewById(R.id.total_sleep);
		totalSleepText.setText(mTypeOfSleep + ": " + totalSleep);
		TextView asleepText = (TextView) findViewById(R.id.asleep_time);
		asleepText.setText(fAsleepTime);
		TextView awakeText = (TextView) findViewById(R.id.awake_time);
		awakeText.setText(fAwakeTime);
	}
	
	public void onClickSave(View view) {
		mSleepLogHelper.updateRating(mAsleepTime, (int) mRatingBar.getRating());
		mSleepLogHelper.updateComments(mAsleepTime, mCommentBox.getText().toString());
		boolean[] excuses = new boolean[EXCUSE_CHECKBOXES.length];
		for (int i = 0; i < EXCUSE_CHECKBOXES.length; i++) {
			CheckBox checkBox = (CheckBox) findViewById(EXCUSE_CHECKBOXES[i]);
			excuses[i] = checkBox.isChecked();
		}
		mSleepLogHelper.updateExcuses(mAsleepTime, excuses);
		finish();
	}
	
	public void onClickEditSleep(View view) {
		Intent intent = new Intent(this, ModifyTimeActivity.class);
		intent.putExtra(SleepLogHelper.ASLEEP_TIME, mAsleepTime);
		startActivityForResult(intent, 1);
	}
	
	public void onClickEditWake(View view) {
		Intent intent = new Intent(this, ModifyTimeActivity.class);
		intent.putExtra(SleepLogHelper.ASLEEP_TIME, mAsleepTime);
		intent.putExtra(SleepLogHelper.AWAKE_TIME, mAwakeTime);
		startActivityForResult(intent, 2);
	}
	
	public void onClickExcuse(View view) {
		int id = view.getId();
		for (int i = 0; i < EXCUSE_IDS.length; i++) {
			if (EXCUSE_IDS[i] == id) {
				CheckBox checkBox = (CheckBox) findViewById(EXCUSE_CHECKBOXES[i]);
				checkBox.toggle();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {
			mAsleepTime = data.getLongExtra(SleepLogHelper.ASLEEP_TIME, mAsleepTime);
		} else if (requestCode == 2 && resultCode == RESULT_OK) {
			mAwakeTime = data.getLongExtra(SleepLogHelper.AWAKE_TIME, mAwakeTime);
		}
	}
}
