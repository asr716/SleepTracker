package com.cis350.sleeptracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class DataActivity extends Activity {
	public final static String ITEM_ASLEEP_TIME_LONG = "asleep_time_long";
	public final static String ITEM_ASLEEP_TIME = "asleep_time";
	public final static String ITEM_AWAKE_TIME = "awake_time";
	public final static String ITEM_TYPE_OF_SLEEP = "type_of_sleep";
	public final static String ITEM_TOTAL_SLEEP = "total_sleep";
	private final static String[] ITEMS = {ITEM_ASLEEP_TIME, ITEM_AWAKE_TIME, ITEM_TYPE_OF_SLEEP, ITEM_TOTAL_SLEEP};
	private final static int[] ITEM_IDS = {R.id.asleep_time, R.id.awake_time, R.id.type_of_sleep, R.id.total_sleep};
	
	private ListView mDataListView;
	private SleepLogHelper mSleepLogHelper;
	private List<Map<String, ?>> mDataList;
	private SimpleDateFormat mSimpleDateFormat;
	
	private Map<String, ?> createItem(long longAsleepTime, String asleepTime, String awakeTime, String typeOfSleep, String totalSleep) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_ASLEEP_TIME_LONG, String.valueOf(longAsleepTime));
		item.put(ITEM_ASLEEP_TIME, asleepTime);
		item.put(ITEM_AWAKE_TIME, awakeTime);
		item.put(ITEM_TYPE_OF_SLEEP, typeOfSleep);
		item.put(ITEM_TOTAL_SLEEP, totalSleep);
		return item;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data);
		
		// Customize Action Bar
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(false);
		LayoutInflater inflator = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.view_action_bar, null);
		((TextView)v.findViewById(R.id.title)).setText(getResources().getString(R.string.action_bar_title));
		getActionBar().setCustomView(v);
		
		mDataListView = (ListView) findViewById(R.id.data_list);
		mSleepLogHelper = new SleepLogHelper(this);
		mSimpleDateFormat = new SimpleDateFormat("MMM dd hh:mm a", Locale.US);
		mDataListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				long asleepTime = Long.valueOf((String) mDataList.get(position).get(ITEM_ASLEEP_TIME_LONG));
				Intent intent = new Intent(view.getContext(), LogActivity.class);
				intent.putExtra(ITEM_ASLEEP_TIME_LONG, asleepTime);
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mDataList = new ArrayList<Map<String, ?>>();
		Cursor cursor = mSleepLogHelper.queryAll();
		if (cursor != null) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				long asleepTime = cursor.getLong(cursor.getColumnIndex(SleepLogHelper.ASLEEP_TIME));
				long awakeTime = cursor.getLong(cursor.getColumnIndex(SleepLogHelper.AWAKE_TIME));
				String fAsleepTime = mSimpleDateFormat.format(new Date(asleepTime));
				String fAwakeTime = "-";
				if (awakeTime != 0) {
					fAwakeTime = mSimpleDateFormat.format(new Date(awakeTime));
				}
				long elapsedTime = awakeTime - asleepTime;
				String totalSleep = String.format(Locale.US, "%d hours, %d minutes",
						TimeUnit.MILLISECONDS.toHours(elapsedTime),
						TimeUnit.MILLISECONDS.toMinutes(elapsedTime) -
						TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedTime)));
				if (elapsedTime < 0) {
					totalSleep = getResources().getString(R.string.pending);
				}
				boolean wasNap = cursor.getInt(cursor.getColumnIndex(SleepLogHelper.NAP)) > 0;
				String typeOfSleep = getResources().getString(R.string.night_sleep);
				if (wasNap) {
					typeOfSleep = getResources().getString(R.string.nap);
				}
				mDataList.add(createItem(asleepTime, fAsleepTime, fAwakeTime, typeOfSleep, totalSleep));
			}
		}
		mDataListView.setAdapter(new SimpleAdapter(this, mDataList, R.layout.data_item, ITEMS, ITEM_IDS));
	}
}
