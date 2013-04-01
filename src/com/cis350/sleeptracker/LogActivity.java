package com.cis350.sleeptracker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class LogActivity extends Activity {
	private static final int[] EXCUSE_RESOURCES = {R.string.excuse1, R.string.excuse2, R.string.excuse3,
		R.string.excuse4, R.string.excuse5, R.string.excuse6}; 
	
	private SharedPreferences mPreferences;
	private LinearLayout mLinearLayout;
	private List<Excuse> mExcusesList;
	private ListView mExcusesListView;
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
		MainActivity.customizeActionBar(this);
		
		mPreferences = getSharedPreferences(MainActivity.MAIN, MODE_PRIVATE);
		mLinearLayout = (LinearLayout) findViewById(R.id.linear_layout);
		if (!mPreferences.getBoolean(MainActivity.IS_ASLEEP, false)) {
			mLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color_awake));
		} else {
			mLinearLayout.setBackgroundColor(getResources().getColor(R.color.background_color));
		}
		mExcusesList = new ArrayList<Excuse>();
		mExcusesListView = (ListView) findViewById(R.id.excuses_list);
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
			for (int i = 0; i < EXCUSE_RESOURCES.length; i++) {
				String col = SleepLogHelper.EXCUSES[i];
				String str = getResources().getString(EXCUSE_RESOURCES[i]);
				mExcusesList.add(new Excuse(cursor.getInt(cursor.getColumnIndex(col)) > 0, str));
			}
			mExcusesListView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					CheckBox checkBox = (CheckBox) view.findViewById(R.id.excuse_checkbox);
					checkBox.toggle();
					mExcusesList.get(position).setChecked(checkBox.isChecked());
				}
			});
			mExcusesListView.setAdapter(new ExcusesAdapter(this, R.layout.excuse_item, mExcusesList));
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
		long elapsedTime = TimeUnit.MILLISECONDS.toMinutes(mAwakeTime) -
				TimeUnit.MILLISECONDS.toMinutes(mAsleepTime);
		String totalSleep = String.format(Locale.US, "%d hours, %d minutes",
				TimeUnit.MINUTES.toHours(elapsedTime),
				elapsedTime - TimeUnit.HOURS.toMinutes(TimeUnit.MINUTES.toHours(elapsedTime)));
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
		boolean[] excuses = new boolean[mExcusesList.size()];
		for (int i = 0; i < mExcusesList.size(); i++) {
			excuses[i] = mExcusesList.get(i).isChecked();
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK) {
			mAsleepTime = data.getLongExtra(SleepLogHelper.ASLEEP_TIME, mAsleepTime);
		} else if (requestCode == 2 && resultCode == RESULT_OK) {
			mAwakeTime = data.getLongExtra(SleepLogHelper.AWAKE_TIME, mAwakeTime);
		}
	}
	
	private class Excuse {
		private boolean mChecked;
		private String mName;
		
		public Excuse(boolean checked, String name) {
			mChecked = checked;
			mName = name;
		}
		
		public boolean isChecked() {
			return mChecked;
		}
		
		public void setChecked(boolean b) {
			mChecked = b;
		}
		
		public String getName() {
			return mName;
		}
	}
	
	private class ExcusesAdapter extends ArrayAdapter<Excuse> {	
		private LayoutInflater mInflater;
		
		public ExcusesAdapter(Context context, int textViewResourceId, List<Excuse> excusesList) {
			super(context, textViewResourceId, excusesList);
			mInflater = LayoutInflater.from(context);
		}
		
		class ViewHolder {
	        protected TextView text;
	        protected CheckBox checkbox;
	    }
		
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.excuse_item, null);
				viewHolder = new ViewHolder();
				viewHolder.checkbox = (CheckBox) convertView.findViewById(R.id.excuse_checkbox);
				viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						mExcusesList.get((Integer)buttonView.getTag()).setChecked(buttonView.isChecked());
					}
				});
				viewHolder.text = (TextView) convertView.findViewById(R.id.excuse);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			viewHolder.checkbox.setTag(position);
			viewHolder.checkbox.setChecked(mExcusesList.get(position).isChecked());
			viewHolder.text.setText(mExcusesList.get(position).getName());
			return convertView;
		}
	}
}
