package com.cis350.sleeptracker;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.TimePicker;

public class ModifyTimeActivity extends Activity {
	private TimePicker mTimePicker;
	private DatePicker mDatePicker;
	private SleepLogHelper mSleepLogHelper;
	private boolean mIsSleepTime;
	private long mAsleepTime;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_modify_time);
		
		mTimePicker = (TimePicker) findViewById(R.id.time_picker);
		mDatePicker = (DatePicker) findViewById(R.id.date_picker);
		mSleepLogHelper = new SleepLogHelper(this);
		mIsSleepTime = false;
		mAsleepTime = getIntent().getLongExtra(SleepLogHelper.ASLEEP_TIME, 0);
		long originalTime = getIntent().getLongExtra(SleepLogHelper.AWAKE_TIME, -1);
		if (originalTime == -1) {
			mIsSleepTime = true;
			originalTime = mAsleepTime;
		}
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(originalTime);
		mTimePicker.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
		mTimePicker.setCurrentMinute(cal.get(Calendar.MINUTE));
		mDatePicker.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), null);
	}
	
	public void onClickSave(View view) {
		GregorianCalendar cal = new GregorianCalendar(mDatePicker.getYear(), mDatePicker.getMonth(),
				mDatePicker.getDayOfMonth(), mTimePicker.getCurrentHour(), mTimePicker.getCurrentMinute());
		long time = cal.getTimeInMillis();
		if (mIsSleepTime) {
			mSleepLogHelper.updateAsleepTime(mAsleepTime, time);
			Intent intent = new Intent();
			intent.putExtra(SleepLogHelper.ASLEEP_TIME, time);
			setResult(RESULT_OK, intent);
		} else {
			mSleepLogHelper.updateAwakeTime(mAsleepTime, time);
			Intent intent = new Intent();
			intent.putExtra(SleepLogHelper.AWAKE_TIME, time);
			setResult(RESULT_OK, intent);
		}
		finish();
	}
	
	// For testing purposes
	public void setIsSleepTime(boolean b) {
		mIsSleepTime = b;
	}
}
