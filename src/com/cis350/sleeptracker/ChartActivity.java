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
import android.view.ViewGroup.LayoutParams;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class ChartActivity extends Activity{
	private static final long DAY_IN_MILLISECONDS = 86400000;
	private static final long HOUR_IN_MILLISECONDS = 3600000;
	private static final long MONTH_IN_MILLISECONDS = 30 * DAY_IN_MILLISECONDS;
	private static final int WEEK = 7;
	private static final int MONTH = 30;
	private static final int YEAR = 12;

	private long today, thisMonth;
	private GraphicalView wChart, mChart, yChart;
    private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesDataset wDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesDataset yDataset = new XYMultipleSeriesDataset();
    private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private XYMultipleSeriesRenderer wRenderer = new XYMultipleSeriesRenderer();
    private XYMultipleSeriesRenderer yRenderer = new XYMultipleSeriesRenderer();
    private XYSeries mTotalSleepSeries, mNapSeries, wTotalSleepSeries, wNapSeries, yTotalSleepSeries;
    private XYSeriesRenderer totalRenderer, nightTimeRenderer;
    
    private SleepLogHelper mSleepLogHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		initChart(mRenderer, MONTH, "Days", false);
		initChart(wRenderer, WEEK, "Days", false);
		initChart(yRenderer, YEAR, "Months", true);
		
		today = (System.currentTimeMillis()/DAY_IN_MILLISECONDS*DAY_IN_MILLISECONDS) + (8*HOUR_IN_MILLISECONDS);
		thisMonth = (System.currentTimeMillis()/MONTH_IN_MILLISECONDS*MONTH_IN_MILLISECONDS);
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
        tabs.setBackgroundColor(getResources().getColor(R.color.background_color_awake));
        tabs.setup();

        if (wChart == null) {
        	addData(WEEK, wNapSeries, wTotalSleepSeries, wDataset);
            wChart = ChartFactory.getBarChartView(ChartActivity.this, wDataset, wRenderer, Type.STACKED);
        } else 
        	wChart.repaint();
        
        if (mChart == null){
        	addData(MONTH, mNapSeries, mTotalSleepSeries, mDataset);
        	mChart = ChartFactory.getBarChartView(ChartActivity.this, mDataset, mRenderer, Type.STACKED);
        } else
        	mChart.repaint();
        
        if (yChart == null){
        	addYearlyData(yTotalSleepSeries, yDataset);
        	yChart = ChartFactory.getBarChartView(ChartActivity.this, yDataset, yRenderer, Type.STACKED);
        } else
        	yChart.repaint();
        
        tabs.clearAllTabs();
        TabHost.TabSpec spec1 = tabs.newTabSpec("weekly");
        spec1.setIndicator("Weekly");
        spec1.setContent(new TabHost.TabContentFactory(){
			public View createTabContent(String tag) {
				return wChart;
			}
        });
        tabs.addTab(spec1);
        
        TabHost.TabSpec spec2 = tabs.newTabSpec("monthly");
        spec2.setIndicator("Monthly");
        spec2.setContent(new TabHost.TabContentFactory(){
			public View createTabContent(String tag) {
				return mChart;
			}
        });
        tabs.addTab(spec2); 
        
        TabHost.TabSpec spec3 = tabs.newTabSpec("yearly");
        spec3.setIndicator("Yearly");
        spec3.setContent(new TabHost.TabContentFactory(){
			public View createTabContent(String tag) {
				return yChart;
			}
        });
        tabs.addTab(spec3);
        
        TabHost.TabSpec spec4 = tabs.newTabSpec("excuses");
        spec4.setIndicator("Excuses");
        spec4.setContent(new TabHost.TabContentFactory(){
			public View createTabContent(String tag) {
				return createExcusesTable();
			}
        });
        tabs.addTab(spec4);
        setTabColor(tabs);
    }
	public void setTabColor(TabHost tabhost) {
		TextView tv;
        for(int i=0;i<tabhost.getTabWidget().getChildCount();i++){
            tv = (TextView) tabhost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            if (tv != null)
            	tv.setTextColor(getResources().getColor(R.color.off_white));
        }
        tv = (TextView) tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).findViewById(android.R.id.title);
        if (tv != null)
        	tv.setTextColor(getResources().getColor(R.color.off_white));
	}
	
	private View createExcusesTable(){
		TableLayout excusesTable = new TableLayout(this);
		TableRow columnLabels = new TableRow(this);
		
		TextView excuseLabel = new TextView(this);
		excuseLabel.setTextColor(getResources().getColor(R.color.off_white));
		excuseLabel.setTextSize(12);
		TextView avgTimeLabel = new TextView(this);
		avgTimeLabel.setTextColor(getResources().getColor(R.color.off_white));
		avgTimeLabel.setTextSize(12);
		TextView avgQtyLabel = new TextView(this);
		avgQtyLabel.setTextColor(getResources().getColor(R.color.off_white));
		avgQtyLabel.setTextSize(12);
		
		excuseLabel.setText("Excuse");
		avgTimeLabel.setText("Avg hours slept");
		avgQtyLabel.setText("Avg sleep quality");
		columnLabels.addView(excuseLabel);
		columnLabels.addView(avgTimeLabel);
		columnLabels.addView(avgQtyLabel);
		
		excusesTable.addView(columnLabels);
		
		DecimalFormat df = new DecimalFormat("0.00");
		double avgTimeSlept, avgQuality;
		String[] excuses = SleepLogHelper.EXCUSES;
		String formate = "";
		for (int i=0; i<excuses.length; i++){
			TableRow tr = new TableRow(this);
			tr.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
			TextView excuse = new TextView(this);
			excuse.setTextColor(getResources().getColor(R.color.off_white));
			excuse.setText(excuses[i]);
			tr.addView(excuse);
			
			Cursor timeSleptCursor = mSleepLogHelper.queryLogExcusesTime(excuses[i]);
			if (timeSleptCursor.moveToFirst()) {
				avgTimeSlept = timeSleptCursor.getLong(0)/HOUR_IN_MILLISECONDS;
				formate = df.format(avgTimeSlept);
				TextView avgTime = new TextView(this);
				avgTime.setTextColor(getResources().getColor(R.color.off_white));
				avgTime.setText(formate);
				tr.addView(avgTime);
			}
			
			Cursor qualityCursor = mSleepLogHelper.queryLogExcusesQuality(excuses[i]);
			if (qualityCursor.moveToFirst()) {
				avgQuality = qualityCursor.getFloat(0);
				formate = df.format(avgQuality);
				TextView avgQual = new TextView(this);
				avgQual.setTextColor(getResources().getColor(R.color.off_white));
				avgQual.setText(formate);
				tr.addView(avgQual);
			}
			excusesTable.addView(tr);
			
			
			

		}
		return excusesTable;
	}
	private void initChart(XYMultipleSeriesRenderer renderer, int numEntries, String title, boolean ifYear) {
		renderer.setMarginsColor(getResources().getColor(R.color.background_color_awake));
		renderer.setMargins(new int[] {30, 60, 90, 30});
		renderer.setXTitle(title); 
		renderer.setYTitle("Hours");
        renderer.setLegendTextSize(24);
        renderer.setZoomRate(0.2f); 
        renderer.setZoomEnabled(false, false); 
        renderer.setPanEnabled(false, false);
        renderer.setBarSpacing(0.3f); 
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(24);
        renderer.setAxisTitleTextSize(35); 
        renderer.setAxesColor(getResources().getColor(R.color.off_white)); 
        renderer.setGridColor(Color.GRAY); 
        renderer.setShowGridX(true);
        renderer.setLabelsTextSize(20);
        renderer.setXLabelsColor(getResources().getColor(R.color.off_white)); 
        renderer.setYLabelsAlign(Align.RIGHT);
        renderer.setYLabelsColor(0, getResources().getColor(R.color.off_white));
        
        renderer.setXAxisMin(0); 
        renderer.setXAxisMax(numEntries + 1);
        
        if (!ifYear) {
	        totalRenderer = new XYSeriesRenderer();
	        totalRenderer.setColor(getResources().getColor(R.color.background_color_awake_green));
	        totalRenderer.setFillPoints(true);
	        totalRenderer.setLineWidth(2);
	        totalRenderer.setChartValuesTextAlign(Align.CENTER);
	        totalRenderer.setChartValuesTextSize(18);
	        totalRenderer.setDisplayChartValues(true);
	        renderer.addSeriesRenderer(totalRenderer);
        }
        
        nightTimeRenderer = new XYSeriesRenderer();
        nightTimeRenderer.setColor(getResources().getColor(R.color.off_white));
        nightTimeRenderer.setFillPoints(true);
        nightTimeRenderer.setLineWidth(2);
        nightTimeRenderer.setChartValuesTextAlign(Align.CENTER);
        nightTimeRenderer.setChartValuesTextSize(18);
        nightTimeRenderer.setDisplayChartValues(true);
        renderer.addSeriesRenderer(nightTimeRenderer);
        
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
					else 
						totalHoursSlept += totalSleep /HOUR_IN_MILLISECONDS;
					
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
	private void addYearlyData(XYSeries total, XYMultipleSeriesDataset dataset){
		total = new XYSeries("Nighttime Sleep");
		dataset.addSeries(total);
		DecimalFormat df = new DecimalFormat("0.00");
		long startMonth = thisMonth - 12 * MONTH_IN_MILLISECONDS;
		long endMonth = thisMonth - 11 * MONTH_IN_MILLISECONDS;
		for (int i=1; i<13; i++){
			Cursor cursor = mSleepLogHelper.queryLogAvgMonth(startMonth, endMonth);
			if (cursor.moveToFirst()){
				double temp = cursor.getLong(0);
				temp = temp/HOUR_IN_MILLISECONDS;
				String formate = df.format(temp);
				double finalValue = Double.parseDouble(formate);
				total.add(i, finalValue);
			}
			startMonth = startMonth + MONTH_IN_MILLISECONDS;
			endMonth = endMonth + MONTH_IN_MILLISECONDS;
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
