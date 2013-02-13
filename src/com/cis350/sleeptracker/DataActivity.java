package com.cis350.sleeptracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class DataActivity extends Activity {
	private final static String ITEM_ASLEEP_TIME = "asleep_time";
	private final static String ITEM_AWAKE_TIME = "awake_time";
	private final static String[] ITEMS = {ITEM_ASLEEP_TIME, ITEM_AWAKE_TIME};
	private final static int[] ITEM_IDS = {R.id.asleep_time, R.id.awake_time};
	
	private ListView mDataListView;
	private SleepLogHelper mSleepLogHelper;
	private List<Map<String, ?>> mDataList;
	private SimpleDateFormat mSimpleDateFormat;
	
	private Map<String, ?> createItem(String asleepTime, String awakeTime) {
		Map<String, String> item = new HashMap<String, String>();
		item.put(ITEM_ASLEEP_TIME, asleepTime);
		item.put(ITEM_AWAKE_TIME, awakeTime);
		return item;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_data);
		
		mDataListView = (ListView) findViewById(R.id.data_list);
		mSleepLogHelper = new SleepLogHelper(this);
		mDataList = new ArrayList<Map<String, ?>>();
		mSimpleDateFormat = new SimpleDateFormat("MMM dd hh:mm a", Locale.US);
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
				mDataList.add(createItem(fAsleepTime, fAwakeTime));
			}
		}
		mDataListView.setAdapter(new SimpleAdapter(this, mDataList, R.layout.data_item, ITEMS, ITEM_IDS));
	}
}
