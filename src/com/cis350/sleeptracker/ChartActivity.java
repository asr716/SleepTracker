package com.cis350.sleeptracker;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.os.Bundle;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Menu;
import android.widget.LinearLayout;

public class ChartActivity extends Activity {
	private static final long DAY_IN_MILLISECONDS = 86400000;
	private static final int WEEK = 7;
	private static final int MONTH = 30;
	
	private long today;
	private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    
    private SleepLogHelper mSleepLogHelper;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mRenderer.setApplyBackgroundColor(true);
	    mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
	    mRenderer.setAxisTitleTextSize(16);
	    mRenderer.setChartTitleTextSize(20);
	    mRenderer.setLabelsTextSize(50);
	    mRenderer.setLegendTextSize(50);
	    mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
	    mRenderer.setZoomButtonsVisible(true);
	    mRenderer.setPointSize(5);
	    
		today = System.currentTimeMillis()/DAY_IN_MILLISECONDS*DAY_IN_MILLISECONDS;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chart);
		MainActivity.customizeActionBar(this);
		mSleepLogHelper = new SleepLogHelper(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chart, menu);
		return true;
	}
	
	protected void onResume() {
		super.onResume();
        LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
        if (mChart == null) {
            initChart();
            addData();
            mChart = ChartFactory.getBarChartView(this, mDataset, mRenderer, Type.DEFAULT);
            layout.addView(mChart);
        } else {
        	addData();
            mChart.repaint();
        }
    }
	
	private void initChart() {
        mCurrentSeries = new XYSeries("Past Week");
        mDataset.addSeries(mCurrentSeries);
        mCurrentRenderer = new XYSeriesRenderer();
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }
	
	private void addData() {
	/*	Adds data starting yesterday
	 */
		long startDay = today;
		long endDay = today+DAY_IN_MILLISECONDS;
		for (int i=2; i>0; i--){
			startDay = startDay - DAY_IN_MILLISECONDS;
			endDay = endDay - DAY_IN_MILLISECONDS;
			long hoursSlept = 0;
			Cursor cursor = mSleepLogHelper.queryLogDay(startDay, endDay);
			if (cursor != null){
				cursor.moveToFirst();
				for (int j=0; j<cursor.getCount(); j++) {
					long startSleep = cursor.getLong(cursor.getColumnIndex(SleepLogHelper.ASLEEP_TIME));
					long endSleep = cursor.getLong(cursor.getColumnIndex(SleepLogHelper.AWAKE_TIME));
					hoursSlept += (endSleep - startSleep)/3600000;
				}
				mCurrentSeries.add(i, hoursSlept);
			}
		}
	        mCurrentSeries.add(3, 2);
	        mCurrentSeries.add(4, 5);
	        mCurrentSeries.add(5, 4);
		//}
    }
}
