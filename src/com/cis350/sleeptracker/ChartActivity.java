package com.cis350.sleeptracker;

import java.text.DecimalFormat;

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
import android.graphics.Paint.Align;
import android.view.Menu;
import android.widget.LinearLayout;

public class ChartActivity extends Activity {
	private static final long DAY_IN_MILLISECONDS = 86400000;
	private static final int HOUR_IN_MILLISECONDS = 3600000;
	private static final int WEEK = 7;
	
	
	private long today;
	private GraphicalView mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mTotalSleepSeries, mNapSeries;
    private XYSeriesRenderer mTotalRenderer, mNapRenderer;
    
    private SleepLogHelper mSleepLogHelper;

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mRenderer.setMargins(new int[]{30,50,60,0});
        mRenderer.setLegendTextSize(24);
        mRenderer.setZoomRate(0.2f); 
        mRenderer.setZoomEnabled(false, false); 
        mRenderer.setBarSpacing(0.3f); 
        mRenderer.setXAxisMin(0); 
        mRenderer.setXAxisMax(8);
        mRenderer.setYAxisMin(0);
        mRenderer.setYAxisMax(24);
        mRenderer.setAxisTitleTextSize(20); 
        mRenderer.setAxesColor(Color.BLACK); 
        mRenderer.setGridColor(Color.GRAY); 
        mRenderer.setShowGridX(true); 
        mRenderer.setXLabels(0);
        mRenderer.setLabelsColor(Color.BLACK); 
        mRenderer.setLabelsTextSize(20);
        mRenderer.setXLabelsColor(Color.BLACK); 
        mRenderer.setXTitle("Days"); 
        mRenderer.setYLabelsAlign(Align.RIGHT);
        mRenderer.setYLabelsColor(0, Color.BLACK);
        mRenderer.setYTitle("Hours");
        
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
            mChart = ChartFactory.getBarChartView(this, mDataset, mRenderer, Type.STACKED);
            layout.addView(mChart);
        } else {
        	addData();
            mChart.repaint();
        }
    }
	
	private void initChart() {
        mTotalSleepSeries = new XYSeries("Hours Slept in the Past Week");
        mNapSeries = new XYSeries("Naps in the Past Week");
        mDataset.addSeries(mTotalSleepSeries);
        mDataset.addSeries(mNapSeries);
        
        mTotalRenderer = new XYSeriesRenderer();
        mTotalRenderer.setColor(Color.rgb(220, 80, 80));
        mTotalRenderer.setFillPoints(true);
        mTotalRenderer.setLineWidth(2);
        mTotalRenderer.setChartValuesTextAlign(Align.CENTER);
        mTotalRenderer.setChartValuesTextSize(18);
        mTotalRenderer.setDisplayChartValues(true);
        
        mNapRenderer = new XYSeriesRenderer();
        mNapRenderer.setColor(Color.rgb(130, 130, 230));
        mNapRenderer.setFillPoints(true);
        mNapRenderer.setLineWidth(2);
        mNapRenderer.setChartValuesTextAlign(Align.CENTER);
        mNapRenderer.setChartValuesTextSize(18);
        mNapRenderer.setDisplayChartValues(true);
        
        mRenderer.addSeriesRenderer(mTotalRenderer);
        mRenderer.addSeriesRenderer(mNapRenderer);
        
    }
	private void addData() {
	/*	Adds data starting yesterday
	 */
		DecimalFormat df = new DecimalFormat("0.00");
		long startDay = today - 8*DAY_IN_MILLISECONDS;
		long endDay = today - 7*DAY_IN_MILLISECONDS;
		int numEntries = mSleepLogHelper.numEntries();
		int count = 1;
		while (count<WEEK+1 && numEntries > 0){
			startDay = startDay + DAY_IN_MILLISECONDS;
			endDay = endDay + DAY_IN_MILLISECONDS;
			double totalHoursSlept = 0;
			double napHoursSlept = 0;
			Cursor cursor = mSleepLogHelper.queryLogDay(startDay, endDay);
			if (cursor != null){
				cursor.moveToFirst();
				for (int j=0; j<cursor.getCount(); j++) {
					long startSleep = cursor.getLong(cursor.getColumnIndex(SleepLogHelper.ASLEEP_TIME));
					long endSleep = cursor.getLong(cursor.getColumnIndex(SleepLogHelper.AWAKE_TIME));
					double totalSleep = endSleep - startSleep;
					if (cursor.getInt(cursor.getColumnIndex(SleepLogHelper.NAP)) == 0){
						totalHoursSlept += totalSleep /HOUR_IN_MILLISECONDS;
						napHoursSlept += totalSleep / HOUR_IN_MILLISECONDS;
					}
					else {
						totalHoursSlept += totalSleep /HOUR_IN_MILLISECONDS;
					}
					
					cursor.moveToNext();
				}
				String formate = df.format(totalHoursSlept);
				double finalValue = Double.parseDouble(formate);
				mTotalSleepSeries.add(count, finalValue);
				formate = df.format(napHoursSlept);
				finalValue = Double.parseDouble(formate);
				mNapSeries.add(count, finalValue);
			}
			count++;
			numEntries--;
		}
    }
}
