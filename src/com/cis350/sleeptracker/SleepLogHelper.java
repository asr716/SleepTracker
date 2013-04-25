package com.cis350.sleeptracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SleepLogHelper {
	private static final String TAG = "SleepLogHelper";
	private static final int TABLE_VERSION = 4;
	private static final String TABLE_NAME = "sleep_log";
	
	// COLUMNS
	protected static final String ASLEEP_TIME = "asleep_time";
	protected static final String AWAKE_TIME = "awake_time";
	protected static final String TIME_SLEPT = "time_slept";
	protected static final String NAP = "nap";
	protected static final String RATING = "rating";
	protected static final String CAFFEINE = "caffeine";
	protected static final String ALCOHOL = "alcohol";
	protected static final String NICOTINE = "nicotine";
	protected static final String SUGAR = "sugar";
	protected static final String SCREEN_TIME = "screen_time";
	protected static final String EXERCISE = "exercise";
	protected static final String COMMENTS = "comments";

	protected static final String[] COLUMNS = {ASLEEP_TIME, AWAKE_TIME, TIME_SLEPT, NAP, RATING, CAFFEINE, ALCOHOL,
		NICOTINE, SUGAR, SCREEN_TIME, EXERCISE, COMMENTS};
	protected static final String[] EXCUSES = {CAFFEINE, ALCOHOL, NICOTINE, SUGAR, SCREEN_TIME, EXERCISE};
	
	private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
			ASLEEP_TIME + " LONG PRIMARY KEY, " +
			AWAKE_TIME + " LONG, " +
			TIME_SLEPT + " LONG, " +
			NAP + " INT, " +
			RATING + " INT, " +
			CAFFEINE + " INT, " +
			ALCOHOL + " INT, " +
			NICOTINE + " INT, " +
			SUGAR + " INT, " +
			SCREEN_TIME + " INT, " +
			EXERCISE + " INT, " +
			COMMENTS + " VARCHAR(255));";
	
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
	}
	
	public SleepLogHelper(Context context) {
		mDbHelper = new DatabaseHelper(context, TABLE_NAME, null, TABLE_VERSION);
		mDb = mDbHelper.getWritableDatabase();
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	public boolean insertLog(long asleepTime, long awakeTime, boolean isNap) {
		ContentValues values = new ContentValues();
		values.put(ASLEEP_TIME, asleepTime);
		values.put(AWAKE_TIME, awakeTime);
		values.put(TIME_SLEPT, (awakeTime - asleepTime));
		if (isNap) {
			values.put(NAP, 1);
		} else {
			values.put(NAP, 0);
		}
		values.put(CAFFEINE, 0);
		values.put(ALCOHOL, 0);
		values.put(NICOTINE, 0);
		values.put(SUGAR, 0);
		values.put(SCREEN_TIME, 0);
		values.put(EXERCISE, 0);
		return (mDb.insert(TABLE_NAME, null, values) > 0);
	}
	
	public boolean updateAsleepTime(long asleepTime, long newAsleepTime) {
		ContentValues values = new ContentValues();
		values.put(ASLEEP_TIME, newAsleepTime);
		Cursor temp = queryLog(asleepTime);
		temp.moveToFirst();
		long awakeTime = temp.getLong(temp.getColumnIndex(SleepLogHelper.AWAKE_TIME));
		values.put(TIME_SLEPT, awakeTime - newAsleepTime);
		String whereClause = ASLEEP_TIME + "=" + asleepTime;
		return (mDb.update(TABLE_NAME, values, whereClause, null) > 0);
	}
	
	public boolean updateAwakeTime(long asleepTime, long awakeTime) {
		ContentValues values = new ContentValues();
		values.put(AWAKE_TIME, awakeTime);
		values.put(TIME_SLEPT, awakeTime - asleepTime);
		String whereClause = ASLEEP_TIME + "=" + asleepTime;
		return (mDb.update(TABLE_NAME, values, whereClause, null) > 0);
	}
	
	public boolean updateRating(long asleepTime, int rating) {
		ContentValues values = new ContentValues();
		values.put(RATING, rating);
		String whereClause = ASLEEP_TIME + "=" + asleepTime;
		return (mDb.update(TABLE_NAME, values, whereClause, null) > 0);
	}
	
	public boolean updateExcuses(long asleepTime, boolean[] excuses) {
		ContentValues values = new ContentValues();
		for (int i = 0; i < EXCUSES.length; i++) {
			if (excuses[i]) {
				values.put(EXCUSES[i], 1);
			} else {
				values.put(EXCUSES[i], 0);
			}
		}
		String whereClause = ASLEEP_TIME + "=" + asleepTime;
		return (mDb.update(TABLE_NAME, values, whereClause, null) > 0);
	}
	
	public boolean updateComments(long asleepTime, String comments) {
		ContentValues values = new ContentValues();
		values.put(COMMENTS, comments);
		String whereClause = ASLEEP_TIME + "=" + asleepTime;
		return (mDb.update(TABLE_NAME, values, whereClause, null) > 0);
	}
	
	public Cursor queryAll() {
		String orderBy = ASLEEP_TIME + " DESC";
		return mDb.query(TABLE_NAME, COLUMNS, null, null, null, null, orderBy);
	}
	
	public Cursor queryLog(long asleepTime) {
		String selection = ASLEEP_TIME + "=" + asleepTime;
		return mDb.query(TABLE_NAME, COLUMNS, selection, null, null, null, null);
	}
	
	public Cursor queryLogDay(long startDay, long endDay) {
		String selection = ASLEEP_TIME + ">" + startDay + " AND " + ASLEEP_TIME + "<" + endDay;
		return mDb.query(TABLE_NAME, COLUMNS, selection, null, null, null, null);
	}
	
	public Cursor queryLogAvgMonth(long startDay, long endDay){
		String rawSelection = "SELECT AVG(" + TIME_SLEPT + ") FROM " + TABLE_NAME + " WHERE " + NAP + "=0 AND (" + ASLEEP_TIME + " BETWEEN " + startDay + " AND " + endDay + ")";
		return mDb.rawQuery(rawSelection, null);
	}
	
	public Cursor queryLogExcusesTime(String excuse){
		String rawSelection = "SELECT AVG(" + TIME_SLEPT + ") FROM " + TABLE_NAME + " WHERE " + excuse + ">0 AND " + NAP + "=0";
		return mDb.rawQuery(rawSelection, null);
	}
	public Cursor queryLogExcusesQuality(String excuse){
		String rawSelection = "SELECT AVG(" + RATING + "*1.0) FROM " + TABLE_NAME + " WHERE " + excuse + "=1";
		return mDb.rawQuery(rawSelection, null);
	}
	public int numEntries(){
		String selection = NAP + "=0";
		Cursor temp = mDb.query(TABLE_NAME, COLUMNS, selection, null, null, null, null);
		return temp.getCount();
	}
	
	public boolean deleteAllEntries() {
		return (mDb.delete(TABLE_NAME, null, null) > 0);
	}
	
	public boolean deleteEntry(long asleepTime) {
		String whereClause = ASLEEP_TIME + "=" + asleepTime;
		return (mDb.delete(TABLE_NAME, whereClause, null) > 0);
	}
}
