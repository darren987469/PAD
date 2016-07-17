package com.darren.pad;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class App implements Serializable {
	private static final String TAG = App.class.getSimpleName();
	private static final long serialVersionUID = 1L;
	public String appName;
	public String packageName;
	public ArrayList<String> times;
	public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
			Locale.TAIWAN);

	public App(String name, String packageName) {
		this.appName = name;
		this.packageName = packageName;
		this.times = new ArrayList<>();

		String today = sdf.format(new Date());
		// add time to first index
		this.times.add(0, today + "," + 0);
	}

	public App(String name, String packageName, ArrayList<String> times) {
		this.appName = name;
		this.packageName = packageName;
		this.times = times;
	}

	/**
	 * Add time into app.
	 * 
	 * @param time
	 *            in seconds
	 */
	public void addCount(int time) {
		String today = sdf.format(new Date());
		String[] strArr = this.times.get(0).split(",");
		// first record is today, sum the time and update first record
		if (strArr[0].equals(today)) {
			int totalTime = Integer.parseInt(strArr[1]) + time;
			this.times.remove(0);
			this.times.add(0, today + "," + Integer.toString(totalTime));
		}
		// first record is not today, add today's record
		else {
			this.times.add(0, today + "," + time);
		}
	}

	/**
	 * Get today's time count (in seconds).
	 * 
	 * @return
	 */
	public int getTimeCount() {
		String today = sdf.format(new Date());
		// first record is today, otherwise return 0
		String[] strArr = this.times.get(0).split(",");
		if (strArr[0].equals(today)) {
			return Integer.parseInt(strArr[1]);
		}
		return 0;
	}

	@Override
	public String toString() {
		return "app:" + appName + ", package:" + packageName + ", times:"
				+ times.toString();
	}

	public String getAppName() {
		return appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public ArrayList<String> getTimes() {
		return times;
	}
}
