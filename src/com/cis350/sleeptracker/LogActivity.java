package com.cis350.sleeptracker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

public class LogActivity extends Activity {
	private long mAsleepTime;
	private long mAwakeTime;
	private SleepLogHelper mSleepLogHelper;
	private SimpleDateFormat mSimpleDateFormat;
	private RatingBar mRatingBar;
	private EditText mCommentBox;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_log);
		
		// Customize Action Bar
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.view_action_bar, null);
		((TextView)v.findViewById(R.id.title)).setText(getResources().getString(R.string.action_bar_title));
		getActionBar().setCustomView(v);
		
		mAsleepTime = getIntent().getLongExtra(DataActivity.ITEM_ASLEEP_TIME_LONG, 0);
		mAwakeTime = 0;
		mSleepLogHelper = new SleepLogHelper(this);
		mSimpleDateFormat = new SimpleDateFormat("MMM dd hh:mm a", Locale.US);
		mRatingBar = (RatingBar) findViewById(R.id.rating_bar);
		mCommentBox = (EditText) findViewById(R.id.comment_box);
		TextView typeOfSleep = (TextView) findViewById(R.id.type_of_sleep);
		
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
				typeOfSleep.setText(getResources().getString(R.string.nap));
			} else {
				typeOfSleep.setText(getResources().getString(R.string.night_sleep));
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		String fAsleepTime = mSimpleDateFormat.format(new Date(mAsleepTime));
		String fAwakeTime = "-";
		if (mAwakeTime != 0) {
			fAwakeTime = mSimpleDateFormat.format(new Date(mAwakeTime));
		}
		long elapsedTime = mAwakeTime - mAsleepTime;
		String totalSleep = String.format(Locale.US, "%d hours, %d minutes",
				TimeUnit.MILLISECONDS.toHours(elapsedTime),
				TimeUnit.MILLISECONDS.toMinutes(elapsedTime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTime)));
		if (elapsedTime < 0) {
			totalSleep = getResources().getString(R.string.pending);
		}
		
		TextView totalSleepText = (TextView) findViewById(R.id.total_sleep);
		totalSleepText.setText(totalSleep);
		TextView asleepText = (TextView) findViewById(R.id.asleep_time);
		asleepText.setText(fAsleepTime);
		TextView awakeText = (TextView) findViewById(R.id.awake_time);
		awakeText.setText(fAwakeTime);
	}
	
	public void onClickSave(View view) {
		mSleepLogHelper.updateRating(mAsleepTime, (int) mRatingBar.getRating());
		mSleepLogHelper.updateComments(mAsleepTime, mCommentBox.getText().toString());
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {
			mAsleepTime = data.getLongExtra(SleepLogHelper.ASLEEP_TIME, mAsleepTime);
		} else if (requestCode == 2 && resultCode == RESULT_OK) {
			mAwakeTime = data.getLongExtra(SleepLogHelper.AWAKE_TIME, mAwakeTime);
		}
	}
}
