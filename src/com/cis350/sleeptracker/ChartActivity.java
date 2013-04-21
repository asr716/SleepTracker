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
import android.view.View;
import android.widget.TabHost;


public class ChartActivity extends Activity{
	private static final long DAY_IN_MILLISECONDS = 86400000;
	private static final int HOUR_IN_MILLISECONDS = 3600000;
	private static final int WEEK = 7;
	private static final int MONTH = 30;

	private long today;
	private GraphicalView wChart, mChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesDataset wDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYMultipleSeriesRenderer wRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mTotalSleepSeries, mNapSeries, wTotalSleepSeries, wNapSeries;
    private XYSeriesRenderer totalRenderer, napRenderer;
    
    private SleepLogHelper mSleepLogHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initChart(mRenderer, mTotalSleepSeries, mNapSeries, MONTH);
		initChart(wRenderer, wTotalSleepSeries, wNapSeries, WEEK);
		
		today = (System.currentTimeMillis()/DAY_IN_MILLISECONDS*DAY_IN_MILLISECONDS) + (8*HOUR_IN_MILLISECONDS);
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
        TabHost tabs = (TabHost)findViewById(R.id.tabHost);
        tabs.setup();
        if (wChart == null) {
            addData(WEEK, wNapSeries, wTotalSleepSeries, wDataset);
	        TabHost.TabSpec spec1 = tabs.newTabSpec("tag1");
	        spec1.setIndicator("Weekly");
	        spec1.setContent(new TabHost.TabContentFactory(){
				public View createTabContent(String tag) {
					wChart = ChartFactory.getBarChartView(ChartActivity.this, wDataset, wRenderer, Type.STACKED);
					return wChart;
				}
	        });
	        tabs.addTab(spec1);
	        
	        addData(MONTH, mNapSeries, mTotalSleepSeries, mDataset);
	        TabHost.TabSpec spec2 = tabs.newTabSpec("tag2");
	        spec2.setIndicator("Monthly");
	        spec2.setContent(new TabHost.TabContentFactory(){
				public View createTabContent(String tag) {
					mChart = ChartFactory.getBarChartView(ChartActivity.this, mDataset, mRenderer, Type.STACKED);
					return mChart;
				}
	        });
	        tabs.addTab(spec2);   	
        } else {
        	wChart.repaint();
        	mChart.repaint();
        }
    }
	
	private void initChart(XYMultipleSeriesRenderer renderer, XYSeries totalSleepSeries, XYSeries napSeries,
			int numEntries) {
		renderer.setMarginsColor(Color.BLUE);
		renderer.setXTitle("Days"); 
		renderer.setYTitle("Hours");
        renderer.setLegendTextSize(24);
        renderer.setZoomRate(0.2f); 
        renderer.setZoomEnabled(false, false); 
        renderer.setBarSpacing(0.3f); 
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(24);
        renderer.setAxisTitleTextSize(50); 
        renderer.setAxesColor(Color.BLACK); 
        renderer.setGridColor(Color.GRAY); 
        renderer.setShowGridX(true);
        renderer.setLabelsColor(Color.BLACK); 
        renderer.setLabelsTextSize(20);
        renderer.setXLabelsColor(Color.BLACK); 
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setYLabelsColor(0, Color.BLACK);
        
        renderer.setXAxisMin(0); 
        renderer.setXAxisMax(numEntries + 1);
        
        totalRenderer = new XYSeriesRenderer();
        totalRenderer.setColor(Color.rgb(220, 80, 80));
        totalRenderer.setFillPoints(true);
        totalRenderer.setLineWidth(2);
        totalRenderer.setChartValuesTextAlign(Align.CENTER);
        totalRenderer.setChartValuesTextSize(18);
        totalRenderer.setDisplayChartValues(true);
        
        napRenderer = new XYSeriesRenderer();
        napRenderer.setColor(Color.rgb(130, 130, 230));
        napRenderer.setFillPoints(true);
        napRenderer.setLineWidth(2);
        napRenderer.setChartValuesTextAlign(Align.CENTER);
        napRenderer.setChartValuesTextSize(18);
        napRenderer.setDisplayChartValues(true);
        
        renderer.addSeriesRenderer(totalRenderer);
        renderer.addSeriesRenderer(napRenderer);
        
    }
	
	
	private void addData(int numOfPoints, XYSeries nap, XYSeries total, XYMultipleSeriesDataset dataset) {
	/*	Adds data starting yesterday
	 */
		total = new XYSeries("Total Sleep");
	    nap = new XYSeries("Nightime Sleep");
	    dataset.addSeries(total);
	    dataset.addSeries(nap);
	    
		DecimalFormat df = new DecimalFormat("0.00");
		long startDay = today - (numOfPoints+2)*DAY_IN_MILLISECONDS;
		long endDay = today - (numOfPoints+1)*DAY_IN_MILLISECONDS;
		int count = 1;
		while (count<numOfPoints+1){
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
				total.add(count, finalValue);
				formate = df.format(napHoursSlept);
				finalValue = Double.parseDouble(formate);
				nap.add(count, finalValue);
			}
			count++;
		}
    }
	
	public XYSeries getTotalSeries() {
		return mTotalSleepSeries;
	}
	
	public XYSeries getNapSeries() {
		return mNapSeries;
	}

	public void add(double x, double y, boolean isNap) {
		if (isNap)
			mNapSeries.add(x,y);
		else
			mTotalSleepSeries.add(x,y);
	}
}
